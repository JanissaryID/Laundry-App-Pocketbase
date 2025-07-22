package com.aluma.laundry.data.api.order.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String,
    val record: Order
)