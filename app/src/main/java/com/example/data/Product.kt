package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: String,
    val description: String,
    val imageUrl: String = "",
    val bpomStatus: String = "Sudah berizin BPOM & Halal",
    val isEnabled: Boolean = true
)
