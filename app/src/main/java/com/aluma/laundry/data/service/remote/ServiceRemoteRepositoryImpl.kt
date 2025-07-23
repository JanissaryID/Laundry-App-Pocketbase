package com.aluma.laundry.data.service.remote

import com.aluma.laundry.data.service.model.ServiceRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient

class ServiceRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : ServiceRemoteRepository {

    private val collection = "Service"

    override suspend fun fetchServices(): List<ServiceRemote> {
        return client.records.getList<ServiceRemote>(
            collection,
            page = 1,
            perPage = 100
        ).items.reversed()
    }
}