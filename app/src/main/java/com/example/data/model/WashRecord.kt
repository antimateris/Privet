package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wash_records")
data class WashRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val motorCount: Int = 1,
    val licensePlate: String = "",
    val motorType: String = "Standar (Matic/Bebek)",
    val washerName: String = "",
    val pricePerMotor: Long = 10000L,
    val washerSharePerMotor: Long = 5000L,
    val totalPrice: Long = 10000L,
    val totalWasherShare: Long = 5000L,
    val totalOwnerShare: Long = 5000L,
    val paymentMethod: String = "Tunai",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = true
)
