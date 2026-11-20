package com.meridian.app.data.remote.Dto

data class MemberDto(
    val userId: String,
    val name: String,
    val country: String,
    val zoneId: String,
    val status: String,
    val busyUntil: String?,
    val onsiteToday: Boolean,
    val updatedAt: String,
)