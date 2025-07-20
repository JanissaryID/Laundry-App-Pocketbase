package com.aluma.laundry.store

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val storeName: String? = null,
    val address: String? = null,
    val city: String? = null,
    val user: String? = null,
) : Record()