package com.aluma.laundry.data.order.remote

import android.util.Log
import com.aluma.laundry.data.order.model.OrderRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class OrderRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : OrderRemoteRepository {

    private val collection = "LaundryOrder"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createOrder(order: OrderRemote) {
        val body = json.encodeToString(order)
        try {
            client.records.create<Record>(collection, body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Create Order failed, attempting update", e)
            try {
                if (order.id.isNotEmpty()) {
                    client.records.update<Record>(
                        sub = collection,
                        id = order.id,
                        body = body
                    )
                    Log.d("OrderRepository", "✅ Successfully updated existing order ${order.id}")
                } else {
                    throw e
                }
            } catch (updateE: Exception) {
                Log.e("OrderRepository", "❌ Update Order failed as well", updateE)
                throw e
            }
        }
    }
}