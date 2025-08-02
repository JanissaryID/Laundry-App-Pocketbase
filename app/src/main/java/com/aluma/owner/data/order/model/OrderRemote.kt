package com.aluma.owner.data.order.model

import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable

@Serializable
data class OrderRemote(
    val customerName: String? = null,
    val serviceName: String? = null,
    val sizeMachine: Boolean = false,
    val typeMachineService: Int = 0,
    val price: String? = null,
    val typePayment: String? = null,
    val user: String? = null,
    val store: String? = null,
) : Record()