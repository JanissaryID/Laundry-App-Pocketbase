package com.aluma.laundry.data.order.remote

import com.aluma.laundry.data.order.model.OrderRemote

interface OrderRemoteRepository {
    suspend fun createOrder(order: OrderRemote)
}