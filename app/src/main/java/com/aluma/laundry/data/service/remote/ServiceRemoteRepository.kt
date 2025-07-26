package com.aluma.laundry.data.service.remote

import com.aluma.laundry.data.service.model.ServiceRemote

interface ServiceRemoteRepository {
    suspend fun fetchServices(storeID: String): List<ServiceRemote>
}