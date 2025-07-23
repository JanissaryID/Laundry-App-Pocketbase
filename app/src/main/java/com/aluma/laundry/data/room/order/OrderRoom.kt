package com.aluma.laundry.data.room.order

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "orders")
data class OrderRoom(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerName: String? = null,
    val serviceName: String? = null,
    val sizeMachine: Boolean = false,
    val stepMachine: Int = 0,
    val numberMachine: Int = 0,
    val typeMachineService: Int = 0,
    val price: String? = null,
    val typePayment: String? = null,
    val user: String? = null,
    val store: String? = null,
)