package com.aluma.owner.data.store.remote

import com.aluma.owner.data.store.model.StoreRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient

class StoreRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : StoreRemoteRepository {

    private val collection = "LaundryStore"

    override suspend fun fetchStores(): List<StoreRemote> {
        return client.records.getList<StoreRemote>(
            collection, page = 1, perPage = 50
        ).items.reversed()
    }
}