package com.aluma.laundry.data.store.local

import com.aluma.laundry.data.store.model.StoreLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreLocalViewModel(private val repo: StoreLocalRepository) : ViewModel() {

    private val _selectedStore = MutableStateFlow<StoreLocal?>(null)
    val selectedStore: StateFlow<StoreLocal?> = _selectedStore

    private val _selectedStoreIndex = MutableStateFlow<Int>(0)
    val selectedStoreIndex: StateFlow<Int> = _selectedStoreIndex

    fun setSelectedStoreIndex(index: Int) {
        _selectedStoreIndex.value = index
    }

    fun setSelectedStore(store: StoreLocal?) {
        _selectedStore.value = store
    }

    val stores: StateFlow<List<StoreLocal>> = repo.storeLocal
        .map { it.reversed() } // <- Membalik urutan list
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    fun addStore(storeLocal: StoreLocal) = viewModelScope.launch {
        repo.addStore(storeLocal)
    }

    fun deleteStore(storeLocal: StoreLocal) = viewModelScope.launch {
        repo.deleteStore(storeLocal)
    }

    fun deleteAllStores() {
        viewModelScope.launch {
            repo.deleteAllStores()
        }
    }

    fun updateStore(storeLocal: StoreLocal) = viewModelScope.launch {
        repo.updateStore(storeLocal)
    }

    suspend fun getStoreById(id: String): StoreLocal? {
        return repo.getStoreById(id)
    }
}