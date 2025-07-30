package com.aluma.laundry.data.service.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class ServiceRemote(
    val store: String? = null,
    val user: String? = null,
    val nameService: String? = null,
    val priceService: String? = null,
    val sizeMachine: Boolean = false,
    val wash: Boolean = false,
    val dry: Boolean = false,
    val service: Boolean = false,
) : Record()