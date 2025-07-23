package com.aluma.laundry.data.room.order

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDAO {
    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<OrderRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderRoom: OrderRoom)

    @Delete
    suspend fun delete(orderRoom: OrderRoom)

    @Update
    suspend fun update(orderRoom: OrderRoom)
}