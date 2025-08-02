package com.aluma.owner.data.order.remote

import com.aluma.owner.data.order.model.OrderRemote
import java.time.LocalDate

interface OrderRemoteRepository {
    suspend fun fetchOrder(storeID: String, date: LocalDate? = null): List<OrderRemote>
}