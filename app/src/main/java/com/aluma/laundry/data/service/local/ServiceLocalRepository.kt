package com.aluma.laundry.data.service.local

import com.aluma.laundry.data.service.model.ServiceLocal
import kotlinx.coroutines.flow.Flow

class ServiceLocalRepository(private val dao: ServiceDAO) {
    val serviceLocal: Flow<List<ServiceLocal>> = dao.getAllService()

    suspend fun addService(serviceLocal: ServiceLocal) = dao.insert(serviceLocal)
    suspend fun deleteService(serviceLocal: ServiceLocal) = dao.delete(serviceLocal)
    suspend fun updateService(serviceLocal: ServiceLocal) = dao.update(serviceLocal)
    suspend fun getServiceById(id: String): ServiceLocal? = dao.getServiceById(id)
}