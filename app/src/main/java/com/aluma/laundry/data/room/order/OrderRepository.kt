package com.aluma.laundry.data.room.order

import android.util.Log
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val dao: OrderDAO) {
    val orders: Flow<List<OrderRoom>> = dao.getAllOrders()

    suspend fun addOrder(orderRoom: OrderRoom) = dao.insert(orderRoom)
    suspend fun deleteOrder(orderRoom: OrderRoom) = dao.delete(orderRoom)
    suspend fun updateOrder(orderRoom: OrderRoom) {
        val result = dao.update(orderRoom)
        Log.d("RoomUpdate", "Rows updated: $result")
    }
}