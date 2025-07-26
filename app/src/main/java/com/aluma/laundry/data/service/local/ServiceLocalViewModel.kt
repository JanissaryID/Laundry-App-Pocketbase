package com.aluma.laundry.data.service.local

import com.aluma.laundry.data.service.model.ServiceLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceLocalViewModel(private val repo: ServiceLocalRepository) : ViewModel() {

    private val _selectedService = MutableStateFlow<ServiceLocal?>(null)
    val selectedService: StateFlow<ServiceLocal?> = _selectedService

    val services: StateFlow<List<ServiceLocal>> = repo.serviceLocal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addService(serviceLocal: ServiceLocal) = viewModelScope.launch {
        repo.addService(serviceLocal)
    }

    fun deleteService(serviceLocal: ServiceLocal) = viewModelScope.launch {
        repo.deleteService(serviceLocal)
    }

    fun deleteAllServices() {
        viewModelScope.launch {
            repo.deleteAllServices()
        }
    }

    fun updateService(serviceLocal: ServiceLocal) = viewModelScope.launch {
        repo.updateService(serviceLocal)
    }

    suspend fun getServiceById(id: String): ServiceLocal? {
        return repo.getServiceById(id)
    }

}