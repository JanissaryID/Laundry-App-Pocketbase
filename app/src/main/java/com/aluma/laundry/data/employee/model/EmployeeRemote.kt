package com.aluma.laundry.data.employee.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeRemote(
    val name: String? = null,
    val user: String? = null,
    val store: String? = null
) : Record()
