package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlinx.coroutines.flow.Flow

class OrderLocalRepository(private val dao: OrderDAO) {
    val orders: Flow<List<OrderLocal>> = dao.getAllOrders()

    suspend fun addOrder(orderLocal: OrderLocal) = dao.insert(orderLocal)
    suspend fun deleteOrder(orderLocal: OrderLocal) = dao.delete(orderLocal)
    suspend fun updateOrderWithResult(orderLocal: OrderLocal): Int = dao.update(orderLocal)
    suspend fun getOrderById(id: String): OrderLocal? = dao.getOrderById(id)

    suspend fun getPendingOrFailedOrders(): List<OrderLocal> {
        return dao.getPendingOrFailedOrders(SyncStatus.PENDING, SyncStatus.FAILED)
    }
}