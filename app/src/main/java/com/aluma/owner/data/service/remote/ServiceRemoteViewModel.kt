package com.aluma.owner.data.service.remote

import android.util.Log
import com.aluma.owner.data.datastore.StorePreferences
import com.aluma.owner.data.service.model.ServiceRemote
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

    private val _selectedServiceRemote = MutableStateFlow<ServiceRemote?>(null)
    val selectedServiceRemote: StateFlow<ServiceRemote?> = _selectedServiceRemote

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _storeId = MutableStateFlow<String?>(null)
    val storeId: StateFlow<String?> = _storeId

    fun setStoreId(storeId: String?) {
        _storeId.value = storeId
    }

    fun setSelectedService(service: ServiceRemote?) {
        _selectedServiceRemote.value = service
    }

    init {
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _userId.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
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

    fun addService(service: ServiceRemote, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                serviceRepository.addService(service)
                onResult(true)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Add Service failed", e)
                onResult(false)
            }
        }
    }

    fun editService(serviceId: String, service: ServiceRemote, onResult: (Boolean) -> Unit) {
        Log.e("ServiceViewModel", "Edit Service $service")
        viewModelScope.launch {
            try {
                serviceRepository.editService(service, serviceId = serviceId)
                onResult(true)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Edit Service failed", e)
                onResult(false)
            }
        }
    }

    fun deleteService(serviceId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                serviceRepository.deleteService(serviceId)
                onResult(true)
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Delete failed", e)
                onResult(false)
            }
        }
    }

}