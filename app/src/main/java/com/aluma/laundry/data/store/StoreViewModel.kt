package com.aluma.laundry.data.store

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoreViewModel (
    private val storePreferences: StorePreferences,
): ViewModel() {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "0f9489584773.ngrok-free.app"
        }
    )

    private val collection = "Store"

    private val _storeRemote = MutableStateFlow<List<StoreRemote>>(emptyList())
    val storeRemote: StateFlow<List<StoreRemote>> = _storeRemote

    private val _token = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

    private val _selectedStoreRemote = MutableStateFlow<StoreRemote?>(null)
    val selectedStoreRemote: StateFlow<StoreRemote?> = _selectedStoreRemote

    private val _nameStore = MutableStateFlow<String?>(null)
    val nameStore: StateFlow<String?> = _nameStore

    fun selectStore(storeRemote: StoreRemote?) {
        _selectedStoreRemote.value = storeRemote
    }

    fun saveStoreID(){
        viewModelScope.launch {
            _selectedStoreRemote.value?.storeName?.let {
                _selectedStoreRemote.value!!.id?.let {
                    idStore -> storePreferences.saveStore(idStore = idStore, nameStore = it)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            storePreferences.userNameStore.collectLatest { _nameStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchStore()
                }
            }
        }
    }

    fun fetchStore() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<StoreRemote>(collection, page = 1, perPage = 50)
                _storeRemote.value = fetched.items.reversed()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch store failed", e)
            }
        }
    }
}