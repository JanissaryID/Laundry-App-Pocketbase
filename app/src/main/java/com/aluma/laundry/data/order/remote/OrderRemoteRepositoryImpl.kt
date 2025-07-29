package com.aluma.laundry.data.order.remote

import com.aluma.laundry.data.order.model.OrderRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter

class OrderRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : OrderRemoteRepository {

    private val collection = "Order"

    override suspend fun fetchOrder(storeID: String): List<OrderRemote> {
        return client.records.getList<OrderRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"${storeID}\"")
        ).items.reversed()
    }
}