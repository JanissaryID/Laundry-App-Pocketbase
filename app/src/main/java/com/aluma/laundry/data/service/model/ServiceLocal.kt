package com.aluma.laundry.data.service.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceLocal(
    @PrimaryKey val id: String,
    val store: String? = null,
    val user: String? = null,
    val nameService: String? = null,
    val priceService: String? = null,
    val typeMachine: Int = 0,
    val sizeMachine: Boolean = false,
)