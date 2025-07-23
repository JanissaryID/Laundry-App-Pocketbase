package com.aluma.laundry.data.order.utils

import com.aluma.laundry.data.order.model.OrderRemote
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String,
    val record: OrderRemote
)