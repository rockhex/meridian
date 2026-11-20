package com.meridian.app.data.remote

import com.google.gson.Gson
import com.meridian.app.BuildConfig
import com.meridian.app.data.security.KeystoreTokenStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/** Cambios de estado de compañeros mientras la app está abierta (WebSocket). */
interface TeamRealtimeSource {
    fun updates(teamId: String): Flow<MemberDto>
}

class NoOpTeamRealtimeSource : TeamRealtimeSource {
    override fun updates(teamId: String): Flow<MemberDto> = emptyFlow()
}

class OkHttpTeamRealtimeSource(
    private val client: OkHttpClient,
    private val tokenStore: KeystoreTokenStore,
) : TeamRealtimeSource {
    private val gson = Gson()

    override fun updates(teamId: String): Flow<MemberDto> = callbackFlow<MemberDto> {
        val request = Request.Builder()
            .url("${BuildConfig.WS_URL}teams/$teamId")
            .apply { tokenStore.read()?.let { header("Authorization", "Bearer $it") } }
            .build()
        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { gson.fromJson(text, MemberDto::class.java) }.getOrNull()?.let { trySend(it) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })
        awaitClose { socket.cancel() }
    }.retryWhen { _, attempt ->
        delay(minOf(30_000L, 2_000L * (attempt + 1)))
        true
    }
}
