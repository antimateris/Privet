package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ValidationState {
    VALID_APPROVED,
    VALID_AUTO_24H,
    PENDING_REVIEW,
    DISPUTED
}

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
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String = "Kasir",
    val validationStatus: String = "PENDING", // PENDING, VALID, DISPUTED
    val disputeReason: String = "",
    val disputedBy: String = "",
    val disputedAt: Long = 0L
) {
    fun getValidationState(): ValidationState {
        return when {
            validationStatus == "DISPUTED" -> ValidationState.DISPUTED
            validationStatus == "VALID" -> ValidationState.VALID_APPROVED
            System.currentTimeMillis() - timestamp >= 24 * 60 * 60 * 1000L -> ValidationState.VALID_AUTO_24H
            else -> ValidationState.PENDING_REVIEW
        }
    }

    fun isEffectivelyValid(): Boolean {
        val state = getValidationState()
        return state == ValidationState.VALID_APPROVED || state == ValidationState.VALID_AUTO_24H
    }

    fun getRemainingVerificationHours(): Long {
        val elapsed = System.currentTimeMillis() - timestamp
        val oneDay = 24 * 60 * 60 * 1000L
        if (elapsed >= oneDay) return 0L
        return ((oneDay - elapsed) / (1000 * 60 * 60)).coerceAtLeast(1)
    }
}

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = true
)
