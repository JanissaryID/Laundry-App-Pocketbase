package com.aluma.owner.data.store.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class StoreRemote(
    val storeName: String? = null,
    val address: String? = null,
    val city: String? = null,
    val user: String? = null,
) : Record()