package com.aluma.laundry.data.store

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.store.model.StoreRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoreRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val storeRepository: StoreRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val _storeRemote = MutableStateFlow<List<StoreRemote>>(emptyList())
    val storeRemote: StateFlow<List<StoreRemote>> = _storeRemote

    private val _selectedStoreRemote = MutableStateFlow<StoreRemote?>(null)
    val selectedStoreRemote: StateFlow<StoreRemote?> = _selectedStoreRemote

    private val _nameStore = MutableStateFlow<String?>(null)
    val nameStore: StateFlow<String?> = _nameStore

    private val _cityStore = MutableStateFlow<String?>(null)
    val cityStore: StateFlow<String?> = _cityStore

    private val _streetStore = MutableStateFlow<String?>(null)
    val streetStore: StateFlow<String?> = _streetStore

    private val _isLoggedIn = MutableStateFlow(false)

    fun selectStore(storeRemote: StoreRemote?) {
        _selectedStoreRemote.value = storeRemote
    }

    fun saveStoreID() {
        viewModelScope.launch {
            _selectedStoreRemote.value?.storeName?.let { name ->
                _selectedStoreRemote.value!!.id?.let { idStore ->
                    _selectedStoreRemote.value!!.city?.let { city ->
                        _selectedStoreRemote.value!!.address?.let { street ->
                            storePreferences.saveStore(
                                idStore = idStore,
                                nameStore = name,
                                cityStore = city,
                                streetStore = street
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            storePreferences.userNameStore.collectLatest { _nameStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userCityStore.collectLatest { _cityStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userStreetStore.collectLatest { _streetStore.value = it.orEmpty() }
        }

        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    _isLoggedIn.value = true
                    fetchStore()
                }
            }
        }
    }

    private fun fetchStore() {
        viewModelScope.launch {
            try {
                _storeRemote.value = storeRepository.fetchStores()
            } catch (e: Exception) {
                Log.e("StoreViewModel", "❌ Fetch store failed", e)
            }
        }
    }
}