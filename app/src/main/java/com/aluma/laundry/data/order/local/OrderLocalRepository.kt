package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlinx.coroutines.flow.Flow

class OrderLocalRepository(private val dao: OrderDAO) {
    val orders: Flow<List<OrderLocal>> = dao.getAllOrders()

    suspend fun addOrder(orderLocal: OrderLocal) = dao.insert(orderLocal)
    suspend fun deleteOrder(orderLocal: OrderLocal) = dao.delete(orderLocal)
    suspend fun updateOrderWithResult(orderLocal: OrderLocal): Int {
        Log.d("RepoUpdate", "📝 Updating order ${orderLocal.id} → step=${orderLocal.stepMachine}, sync=${orderLocal.syncStatus}")
        val result = dao.update(orderLocal)
        Log.d("RepoUpdate", "✅ update() result: $result row(s) affected")
        return result
    }
    suspend fun updateSyncStatusOnly(id: String, syncStatus: SyncStatus): Int {
        Log.d("RepoUpdate", "🔄 Sync only: setting syncStatus of $id to $syncStatus")
        return dao.updateSyncStatusOnly(id, syncStatus)
    }
    suspend fun getOrderById(id: String): OrderLocal? = dao.getOrderById(id)

    suspend fun getPendingOrFailedOrders(): List<OrderLocal> {
        return dao.getPendingOrFailedOrders(SyncStatus.PENDING, SyncStatus.FAILED)
    }
}