package com.aluma.laundry.data.service.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.service.model.ServiceRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServiceViewModel (
    private val storePreferences: StorePreferences,
): ViewModel() {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "0f9489584773.ngrok-free.app"
        }
    )

    private val collection = "Service"

    private val _token = MutableStateFlow<String?>(null)
    private val _idStore = MutableStateFlow<String?>(null)
    val idStore: StateFlow<String?> = _idStore
    private val _isLoggedIn = MutableStateFlow(false)
    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser

    private val _serviceRemote = MutableStateFlow<List<ServiceRemote>>(emptyList())
    val serviceRemote: StateFlow<List<ServiceRemote>> = _serviceRemote

    init {
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _idStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchService()
                }
            }
        }
    }

    fun fetchService() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<ServiceRemote>(collection, page = 1, perPage = 100)
                _serviceRemote.value = fetched.items.reversed()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch Services failed", e)
            }
        }
    }
}