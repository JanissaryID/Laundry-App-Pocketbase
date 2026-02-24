package com.aluma.laundry.data.order.remote

import android.util.Log
import com.aluma.laundry.data.order.model.OrderRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import kotlinx.coroutines.CancellationException
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.json.Json

class OrderRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : OrderRemoteRepository {

    private val collection = "Order"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createOrder(order: OrderRemote) {
        try {
            client.records.create<Record>(
                collection,
                json.encodeToString(order)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Create Order failed", e)
            throw e
        }
    }
}