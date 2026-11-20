package com.meridian.app.data.remote

import com.meridian.app.BuildConfig
import com.meridian.app.data.security.KeystoreTokenStore
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/** Frontera con el backend. Lanza [UnauthorizedException], [WrongDomainException] o [IOException]. */
interface RemoteDataSource {
    suspend fun signIn(idToken: String): SessionDto
    suspend fun linkGoogle(idToken: String)
    suspend fun getTeam(teamId: String): TeamDto
    suspend fun pushClockEvent(event: ClockEventDto): ClockPushResponse
    suspend fun voidClockEvent(id: String)
    suspend fun getCalendar(date: String): List<CalendarSlotDto>
    suspend fun getPlans(from: String, to: String): List<PlanDto>
    suspend fun pushPlan(plan: PlanDto)
    suspend fun getHolidays(year: Int): List<HolidayDto>
}

class RetrofitRemoteDataSource(private val api: MeridianApi) : RemoteDataSource {

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> throw UnauthorizedException()
            403 -> throw WrongDomainException()
            424 -> throw GoogleTokenExpiredException() // el servidor no pudo usar el token de Google
            else -> throw IOException("HTTP ${e.code()}", e)
        }
    }

    override suspend fun signIn(idToken: String) = call { api.signIn(SignInRequest(idToken)) }
    override suspend fun linkGoogle(idToken: String) = call { api.linkGoogle(LinkGoogleRequest(idToken)) }
    override suspend fun getTeam(teamId: String) = call { api.team(teamId) }
    override suspend fun pushClockEvent(event: ClockEventDto) = call { api.pushClockEvent(event) }
    override suspend fun voidClockEvent(id: String) = call { api.voidClockEvent(id) }
    override suspend fun getCalendar(date: String) = call { api.calendar(date) }
    override suspend fun getPlans(from: String, to: String) = call { api.plans(from, to) }
    override suspend fun pushPlan(plan: PlanDto) = call { api.pushPlan(plan) }
    override suspend fun getHolidays(year: Int) = call { api.holidays(year) }
}

object RetrofitFactory {
    fun createClient(tokenStore: KeystoreTokenStore): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            tokenStore.read()?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }
        .build()

    fun createApi(client: OkHttpClient): MeridianApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MeridianApi::class.java)
}
