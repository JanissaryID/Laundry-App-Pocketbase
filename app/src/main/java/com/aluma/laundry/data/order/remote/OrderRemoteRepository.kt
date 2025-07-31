package com.aluma.laundry.data.order.remote

import com.aluma.laundry.data.order.model.OrderRemote
import java.time.LocalDate

interface OrderRemoteRepository {
    suspend fun fetchOrder(storeID: String, date: LocalDate? = null): List<OrderRemote>
}