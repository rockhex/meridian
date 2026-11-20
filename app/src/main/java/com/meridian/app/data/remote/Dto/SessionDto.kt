package com.meridian.app.data.remote.Dto

data class SessionDto(
    val token: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val teamId: String,
    val country: String,
    val zoneId: String,
)
