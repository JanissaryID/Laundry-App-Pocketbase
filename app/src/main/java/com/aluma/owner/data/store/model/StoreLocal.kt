package com.aluma.owner.data.store.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store")
data class StoreLocal(
    @PrimaryKey val id: String,
    val storeName: String? = null,
    val address: String? = null,
    val city: String? = null,
    val user: String? = null,
)