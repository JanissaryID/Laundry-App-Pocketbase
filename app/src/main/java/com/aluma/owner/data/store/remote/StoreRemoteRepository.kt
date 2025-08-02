package com.aluma.owner.data.store.remote

import com.aluma.owner.data.store.model.StoreRemote

interface StoreRemoteRepository {
    suspend fun fetchStores(): List<StoreRemote>
}