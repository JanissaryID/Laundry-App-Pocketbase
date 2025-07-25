package com.aluma.laundry.data.order.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDAO {
    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<OrderLocal>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(orderLocal: OrderLocal)

    @Delete
    suspend fun delete(orderLocal: OrderLocal)

    @Update
    suspend fun update(orderLocal: OrderLocal): Int

    @Query("UPDATE orders SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatusOnly(id: String, status: SyncStatus): Int

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderLocal?

    @Query("SELECT * FROM orders WHERE syncStatus IN (:pending, :failed)")
    suspend fun getPendingOrFailedOrders(pending: SyncStatus, failed: SyncStatus): List<OrderLocal>
}