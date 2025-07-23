package com.aluma.laundry.data.store

import com.aluma.laundry.data.store.model.StoreRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient

class StoreRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : StoreRemoteRepository {

    private val collection = "Store"

    override suspend fun fetchStores(): List<StoreRemote> {
        return client.records.getList<StoreRemote>(
            collection, page = 1, perPage = 50
        ).items.reversed()
    }
}