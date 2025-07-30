package com.aluma.laundry.data.service.remote

import android.util.Log
import com.aluma.laundry.data.service.model.ServiceRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class ServiceRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : ServiceRemoteRepository {

    private val collection = "Service"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchServices(storeID: String): List<ServiceRemote> {
        return client.records.getList<ServiceRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = Filter("store=\"${storeID}\"")
        ).items.reversed()
    }

    override suspend fun addService(service: ServiceRemote) {
        Log.e("ServiceRepo", "Add Service $service")
        try {
            client.records.create<ServiceRemote>(
                sub = collection,
                body = json.encodeToString(service)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ServiceRepo", "❌ Add Service failed", e)
            throw e
        }
    }

    override suspend fun editService(service: ServiceRemote, serviceId: String) {
        try {
            client.records.update<ServiceRemote>(
                sub = collection,
                id = serviceId,
                body = json.encodeToString(service)
            )
            Log.e("ServiceRepo", "Servis $service")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ServiceRepo", "❌ Edit Service failed", e)
            throw e
        }
    }

    override suspend fun deleteService(serviceId: String) {
        try {
            client.records.delete(
                sub = collection,
                id = serviceId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ServiceRepo", "❌ Delete Service failed", e)
            throw e
        }
    }
}