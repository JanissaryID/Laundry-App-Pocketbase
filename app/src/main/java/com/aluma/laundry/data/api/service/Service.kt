package com.aluma.laundry.data.api.service

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val store: String? = null,
    val user: String? = null,
    val nameService: String? = null,
    val priceService: String? = null,
    val typeMachine: Int = 0,
    val sizeMachine: Boolean = false,
) : Record()