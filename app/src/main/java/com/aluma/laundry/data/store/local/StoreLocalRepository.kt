package com.aluma.laundry.data.store.local

import com.aluma.laundry.data.store.model.StoreLocal
import kotlinx.coroutines.flow.Flow

class StoreLocalRepository(private val dao: StoreDAO) {
    val storeLocal: Flow<List<StoreLocal>> = dao.getAllStores()

    suspend fun addStore(storeLocal: StoreLocal) = dao.insert(storeLocal)
    suspend fun deleteStore(storeLocal: StoreLocal) = dao.delete(storeLocal)
    suspend fun deleteAllStores() = dao.deleteAll()
    suspend fun updateStore(storeLocal: StoreLocal) = dao.update(storeLocal)
    suspend fun getStoreById(id: String): StoreLocal? = dao.getStoresById(id)
}