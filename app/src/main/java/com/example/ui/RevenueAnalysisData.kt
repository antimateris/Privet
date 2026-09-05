package com.example.ui

data class TimeSlotStat(
    val slotName: String,
    val timeRange: String,
    val motorCount: Int,
    val percentage: Float
)

data class PaymentMethodStat(
    val method: String,
    val motorCount: Int,
    val totalAmount: Long,
    val percentage: Float
)

data class DailyChartPoint(
    val label: String,
    val grossRevenue: Long,
    val washerShare: Long,
    val ownerShare: Long,
    val motorCount: Int
)

data class RevenueAnalysisData(
    val totalMotors: Int = 0,
    val totalGrossRevenue: Long = 0L,
    val totalWasherShare: Long = 0L,
    val totalOwnerShare: Long = 0L,
    val ownerMarginPercent: Float = 0f,
    val averageDailyMotors: Float = 0f,
    val averageDailyGross: Long = 0L,
    val averageDailyOwnerNet: Long = 0L,
    val projectedMonthlyGross: Long = 0L,
    val projectedMonthlyOwnerNet: Long = 0L,
    val peakTimeSlot: String = "-",
    val timeSlots: List<TimeSlotStat> = emptyList(),
    val paymentMethods: List<PaymentMethodStat> = emptyList(),
    val chartPoints: List<DailyChartPoint> = emptyList(),
    val insights: List<String> = emptyList()
)
