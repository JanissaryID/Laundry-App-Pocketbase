package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import kotlinx.coroutines.flow.Flow

class OrderLocalRepository(private val dao: OrderDAO) {
    val orders: Flow<List<OrderLocal>> = dao.getAllOrders()

    suspend fun addOrder(orderLocal: OrderLocal) = dao.insert(orderLocal)
    suspend fun deleteOrder(orderLocal: OrderLocal) = dao.delete(orderLocal)
    suspend fun updateOrder(orderLocal: OrderLocal) {
        val result = dao.update(orderLocal)
        Log.d("RoomUpdate", "Rows updated: $result")
    }
    suspend fun getOrderById(id: String): OrderLocal? = dao.getOrderById(id)
}