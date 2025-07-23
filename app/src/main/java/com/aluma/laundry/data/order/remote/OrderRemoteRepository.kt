package com.aluma.laundry.data.order.remote

import com.aluma.laundry.data.order.model.OrderRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import kotlinx.serialization.json.Json

class OrderRemoteRepository(private val client: PocketbaseClient) {

    private val collection = "Order"

    suspend fun fetchOrders(): List<OrderRemote> {
        val result = client.records.getList<OrderRemote>(sub = collection, page = 1, perPage = 200)
        return result.items
    }

    suspend fun createOrder(order: OrderRemote) {
        client.records.create<OrderRemote>(
            sub = collection,
            body = Json.encodeToString(order)
        )
    }

    suspend fun patchOrder(id: String, order: OrderRemote) {
        client.records.update<OrderRemote>(
            id = id,
            sub = collection,
            body = Json.Default.encodeToString(order))
    }
}