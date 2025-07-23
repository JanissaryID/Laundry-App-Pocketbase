package com.aluma.laundry.data.order.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aluma.laundry.data.order.model.OrderLocal
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
    suspend fun update(orderLocal: OrderLocal)
}