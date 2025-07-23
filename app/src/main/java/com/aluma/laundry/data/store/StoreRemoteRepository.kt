package com.aluma.laundry.data.store

import com.aluma.laundry.data.store.model.StoreRemote

interface StoreRemoteRepository {
    suspend fun fetchStores(): List<StoreRemote>
}