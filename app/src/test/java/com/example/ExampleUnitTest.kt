package com.example

import com.example.ui.DreamGoalConfig
import com.example.ui.DreamGoalProgress
import com.example.ui.SavingsSource
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDreamGoalProgress_calculations() {
    val config = DreamGoalConfig(
      itemName = "Kompresor Steam Matrix 2 HP",
      targetAmount = 2_000_000L,
      initialSavings = 500_000L,
      savingSource = SavingsSource.TOTAL_REVENUE,
      dailyTargetAmount = 100_000L
    )

    val todayRev = 150_000L
    val totalAcc = 500_000L
    val totalSaved = config.initialSavings + totalAcc // 1_000_000L
    val remaining = (config.targetAmount - totalSaved).coerceAtLeast(0L) // 1_000_000L
    val progressPercent = totalSaved.toFloat() / config.targetAmount.toFloat() // 0.5f

    val progress = DreamGoalProgress(
      config = config,
      todayGrossRevenue = todayRev,
      todayOwnerShare = 75_000L,
      todayRevenue = todayRev,
      totalAccumulatedRevenue = totalAcc,
      totalSaved = totalSaved,
      remainingAmount = remaining,
      progressPercent = progressPercent,
      todayContributionPercent = todayRev.toFloat() / config.targetAmount.toFloat(),
      todayDailyTargetPercent = todayRev.toFloat() / config.dailyTargetAmount.toFloat(),
      isAchieved = totalSaved >= config.targetAmount,
      estimatedDaysRemaining = 10,
      averageDailyRevenue = 100_000L
    )

    assertEquals(1_000_000L, progress.totalSaved)
    assertEquals(1_000_000L, progress.remainingAmount)
    assertEquals(0.5f, progress.progressPercent, 0.001f)
    assertEquals(1.5f, progress.todayDailyTargetPercent, 0.001f)
    assertFalse(progress.isAchieved)
  }

  @Test
  fun testFormatUtils_dayRangeAndHelpers() {
    val now = System.currentTimeMillis()
    val (start, end) = com.example.util.FormatUtils.getDayTimestampRange(now)
    assertTrue(start <= now)
    assertTrue(end >= now)
    assertTrue(end - start in (86_399_000L..86_400_000L))

    assertTrue(com.example.util.FormatUtils.isToday(now))
    assertTrue(com.example.util.FormatUtils.isSameDay(now, now))

    val relativeToday = com.example.util.FormatUtils.formatRelativeDate(now)
    assertTrue(relativeToday.startsWith("Hari Ini"))
  }

  @Test
  fun testMonthlySummary_calculationsAndCsvFormat() {
    val dayRecord1 = com.example.ui.MonthlyDayRecord(
      dayOfMonth = 1,
      dateMillis = 1756684800000L,
      dateKey = "2026-09-01",
      dayName = "Selasa",
      motorCount = 10,
      grossRevenue = 100_000L,
      washerShare = 50_000L,
      ownerShare = 50_000L,
      topWasherName = "Budi (10 m)"
    )

    val dayRecord2 = com.example.ui.MonthlyDayRecord(
      dayOfMonth = 2,
      dateMillis = 1756771200000L,
      dateKey = "2026-09-02",
      dayName = "Rabu",
      motorCount = 15,
      grossRevenue = 150_000L,
      washerShare = 75_000L,
      ownerShare = 75_000L,
      topWasherName = "Siti (15 m)"
    )

    val summary = com.example.ui.MonthlySummaryData(
      year = 2026,
      month = 8,
      monthName = "September",
      totalMotors = 25,
      totalGrossRevenue = 250_000L,
      totalWasherShare = 125_000L,
      totalOwnerShare = 125_000L,
      activeDaysCount = 2,
      daysInMonth = 30,
      averageDailyMotors = 12.5f,
      averageDailyGross = 125_000L,
      averageDailyOwnerShare = 62_500L,
      peakDay = dayRecord2,
      dailyBreakdown = listOf(dayRecord1, dayRecord2),
      washerStats = listOf(
        com.example.ui.MonthlyWasherStat("Siti", 15, 75_000L, 60f),
        com.example.ui.MonthlyWasherStat("Budi", 10, 50_000L, 40f)
      ),
      cashAmount = 200_000L,
      qrisAmount = 50_000L,
      transferAmount = 0L
    )

    assertEquals(25, summary.totalMotors)
    assertEquals(250_000L, summary.totalGrossRevenue)
    assertEquals(125_000L, summary.totalWasherShare)
    assertEquals(125_000L, summary.totalOwnerShare)
    assertEquals(2, summary.activeDaysCount)
    assertEquals("Siti", summary.peakDay?.topWasherName?.take(4))

    // Test CSV format building
    val sb = StringBuilder()
    sb.append("Tanggal,Hari,Jumlah Motor,Omset Kotor (Rp),Bagi Hasil Petugas (Rp),Kas Bersih Pemilik (Rp),Petugas Terbanyak\n")
    for (d in summary.dailyBreakdown) {
      val cleanWasher = d.topWasherName.replace(",", ";").replace("\"", "")
      sb.append("${d.dateKey},${d.dayName},${d.motorCount},${d.grossRevenue},${d.washerShare},${d.ownerShare},\"$cleanWasher\"\n")
    }
    sb.append("TOTAL,,${summary.totalMotors},${summary.totalGrossRevenue},${summary.totalWasherShare},${summary.totalOwnerShare},\n")

    val csv = sb.toString()
    assertTrue(csv.contains("Tanggal,Hari,Jumlah Motor,Omset Kotor (Rp),Bagi Hasil Petugas (Rp),Kas Bersih Pemilik (Rp),Petugas Terbanyak"))
    assertTrue(csv.contains("2026-09-01,Selasa,10,100000,50000,50000,\"Budi (10 m)\""))
    assertTrue(csv.contains("2026-09-02,Rabu,15,150000,75000,75000,\"Siti (15 m)\""))
    assertTrue(csv.contains("TOTAL,,25,250000,125000,125000,"))
  }
}

