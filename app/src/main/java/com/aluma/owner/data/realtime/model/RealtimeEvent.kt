package com.aluma.owner.data.realtime.model

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEvent(
    val action: String? = null,
    val record: kotlinx.serialization.json.JsonObject? = null
)
