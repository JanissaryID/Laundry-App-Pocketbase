package com.aluma.laundry.data.service.remote

import com.aluma.laundry.data.service.model.ServiceRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter

class ServiceRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : ServiceRemoteRepository {

    private val collection = "LaundryService"

    override suspend fun fetchServices(storeID: String): List<ServiceRemote> {
        return client.records.getList<ServiceRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"${storeID}\"")
        ).items.reversed()
    }
}