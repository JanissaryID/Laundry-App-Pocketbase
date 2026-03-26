package com.aluma.owner.data.employee.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class LaundryEmployee(
    val name: String? = null,
    val user: String? = null
) : Record()
