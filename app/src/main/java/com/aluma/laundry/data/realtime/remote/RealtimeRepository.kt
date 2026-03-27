package com.aluma.laundry.data.realtime.remote

import com.aluma.laundry.data.realtime.model.RealtimeSse

interface RealtimeRepository {
    suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit)
    suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean
}
