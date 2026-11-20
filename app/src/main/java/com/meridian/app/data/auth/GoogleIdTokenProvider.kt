package com.meridian.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Obtiene el ID token de Google. El servidor lo valida y verifica el dominio corporativo (RNF09). */
interface GoogleIdTokenProvider {
    suspend fun requestIdToken(context: Context): String
}

/** Credential Manager + "Sign in with Google". Requiere un OAuth client ID real (ver README). */
class CredentialManagerGoogleIdTokenProvider(private val serverClientId: String) : GoogleIdTokenProvider {
    override suspend fun requestIdToken(context: Context): String {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(context).getCredential(context, request)
        return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    }
}

/** Para el modo demo (backend simulado): devuelve una cuenta del dominio de prueba. */
class DemoGoogleIdTokenProvider : GoogleIdTokenProvider {
    override suspend fun requestIdToken(context: Context): String = "demo:carla.ruiz@meridian.example"
}
