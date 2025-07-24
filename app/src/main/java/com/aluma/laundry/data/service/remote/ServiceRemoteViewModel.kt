package com.aluma.laundry.data.service.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.service.local.ServiceLocalRepository
import com.aluma.laundry.data.service.model.ServiceLocal
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
    private val serviceLocalRepository: ServiceLocalRepository
) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser

    private val _idStore = MutableStateFlow<String?>(null)
    val idStore: StateFlow<String?> = _idStore

    private val _isLoggedIn = MutableStateFlow(false)

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
            storePreferences.userToken.collectLatest { token ->
                _token.value = token
                val loggedIn = !token.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(token)
                    fetchServices()
                }
            }
        }
    }

    private fun fetchServices() {
        viewModelScope.launch {
            try {
                val fetched = serviceRepository.fetchServices()
                _serviceRemote.value = fetched

                fetched.forEach { service ->
                    val existing = serviceLocalRepository.getServiceById(service.id!!)

                    if (existing == null) {
                        val newService = ServiceLocal(
                            id = service.id!!,
                            typeMachine = service.typeMachine,
                            sizeMachine = service.sizeMachine,
                            user = service.user,
                            store = service.store,
                            priceService = service.priceService,
                            nameService = service.nameService

                        )
                        serviceLocalRepository.addService(newService)
                    } else {
                        val updated = existing.copy(
                            typeMachine = service.typeMachine,
                            sizeMachine = service.sizeMachine,
                            nameService = service.nameService,
                            priceService = service.priceService
                        )
                        serviceLocalRepository.updateService(updated)
                    }
                }
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Fetch Services failed", e)
            }
        }
    }
}