package com.aluma.owner.data.attendance.model

import kotlinx.serialization.Serializable
import io.github.agrevster.pocketbaseKotlin.models.Record

@Serializable
data class LaundryAttendance(
    val employee: String? = null,
    val date: String? = null,
    val cekIn: String? = null,
    val cekOut: String? = null,
    val status: Int? = null
) : Record()
