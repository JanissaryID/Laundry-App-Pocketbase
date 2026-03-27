package com.aluma.laundry.data.realtime.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String? = null,
    val record: kotlinx.serialization.json.JsonObject? = null
)

data class RealtimeSse(
    val id: String? = null,
    val data: String? = null
)
