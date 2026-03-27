package com.aluma.owner.data.realtime

import com.aluma.owner.data.realtime.model.RealtimeSse

interface RealtimeRepository {
    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit)
    suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean
}
