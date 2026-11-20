package com.meridian.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MeridianApi {
    @POST("auth/google") suspend fun signIn(@Body request: SignInRequest): SessionDto
    @POST("auth/google/link") suspend fun linkGoogle(@Body request: LinkGoogleRequest)
    @GET("teams/{id}") suspend fun team(@Path("id") id: String): TeamDto
    @POST("clock-events") suspend fun pushClockEvent(@Body event: ClockEventDto): ClockPushResponse
    /** Debe ser idempotente: un id desconocido no es error. */
    @DELETE("clock-events/{id}") suspend fun voidClockEvent(@Path("id") id: String)
    @GET("calendar/day") suspend fun calendar(@Query("date") date: String): List<CalendarSlotDto>
    @GET("office-plan") suspend fun plans(@Query("from") from: String, @Query("to") to: String): List<PlanDto>
    @PUT("office-plan") suspend fun pushPlan(@Body plan: PlanDto)
    @GET("holidays/{year}") suspend fun holidays(@Path("year") year: Int): List<HolidayDto>
}
