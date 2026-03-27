package com.aluma.laundry.data.attendance.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRemote(
    val employee: String? = null,
    val date: String? = null,
    val cekIn: String? = null,
    val cekOut: String? = null,
    val status: Int? = null,
    val store: String? = null
) : Record()
