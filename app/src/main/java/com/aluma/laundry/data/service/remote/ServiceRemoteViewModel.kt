package com.aluma.laundry.data.service.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.service.model.ServiceRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServiceRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val client: PocketbaseClient,
    private val serviceRepository: ServiceRemoteRepository,
) : ViewModel() {

    private val _serviceRemote = MutableStateFlow<List<ServiceRemote>>(emptyList())
    val serviceRemote: StateFlow<List<ServiceRemote>> = _serviceRemote

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                val loggedIn = !token.isNullOrEmpty()
                if (loggedIn) {
                    client.login(token)
                }
            }
        }
    }

    fun fetchServices(storeID: String) {
        viewModelScope.launch {
            try {
                val fetched = serviceRepository.fetchServices(storeID = storeID)
                _serviceRemote.value = fetched
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Fetch Services failed", e)
            }
        }
    }
}