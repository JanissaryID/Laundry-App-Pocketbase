package com.aluma.owner.data.realtime

import android.util.Log
import com.aluma.owner.data.realtime.model.RealtimeSse
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class RealtimeRepositoryImpl(
    private val client: PocketbaseClient
) : RealtimeRepository {

    override suspend fun withRealtimeEvents(onEvent: suspend (RealtimeSse) -> Unit) {
        try {
            client.httpClient.sse(path = "/api/realtime") {
                incoming.collect { evt ->
                    onEvent(
                        RealtimeSse(
                            id = evt.id,
                            data = evt.data
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SSE_ERROR", "SSE connection error: ${e.message}")
        }
    }

    override suspend fun subscribeRealtime(clientId: String, collections: List<String>): Boolean {
        return try {
            val body: JsonObject = buildJsonObject {
                put("clientId", JsonPrimitive(clientId))
                put(
                    "subscriptions",
                    JsonArray(collections.map { JsonPrimitive(it) })
                )
            }

            val response = client.httpClient.post {
                url { path("/api/realtime") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("REALTIME_ERROR", "Subscribe realtime failed: ${e.message}")
            false
        }
    }
}
