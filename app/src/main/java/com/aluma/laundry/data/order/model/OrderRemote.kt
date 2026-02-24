package com.aluma.laundry.data.order.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderRemote(
    val id: String = "",
    val customerName: String? = null,
    val serviceName: String? = null,
    val sizeMachine: Boolean = false,
    val typeMachineService: Int = 0,
    val price: String? = null,
    val typePayment: String? = null,
    val user: String? = null,
    val store: String? = null,
    val date: String? = null,
)