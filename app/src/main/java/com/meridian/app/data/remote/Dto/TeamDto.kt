package com.meridian.app.data.remote.Dto

data class TeamDto(
    val id: String,
    val name: String,
    val office: OfficeDto?,
    val fixedDays: List<String>,
    val members: List<MemberDto>,
)