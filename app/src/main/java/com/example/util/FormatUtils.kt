package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimePeriod(val title: String) {
    HARI_INI("Hari Ini"),
    KEMARIN("Kemarin"),
    TANGGAL_PILIHAN("Pilih Tanggal"),
    TUJUH_HARI("7 Hari"),
    BULAN_INI("Bulan Ini"),
    SEMUA("Semua")
}

object FormatUtils {
    private val idLocale = Locale("in", "ID")

    private val rupiahFormatThreadLocal = ThreadLocal.withInitial {
        val formatter = NumberFormat.getCurrencyInstance(idLocale)
        formatter.maximumFractionDigits = 0
        formatter
    }

    private val timeFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", idLocale)
    }

    private val dateShortThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM, HH:mm", idLocale)
    }

    private val dateMediumThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("d MMM yyyy", idLocale)
    }

    private val dateFullThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("EEEE, d MMMM yyyy", idLocale)
    }

    private val monthYearThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("MMMM yyyy", idLocale)
    }

    private val dayNameThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("EEEE", idLocale)
    }

    fun formatRupiah(amount: Long): String {
        val formatter = rupiahFormatThreadLocal.get() ?: NumberFormat.getCurrencyInstance(idLocale).apply { maximumFractionDigits = 0 }
        return formatter.format(amount).replace("Rp", "Rp ").trim()
    }

    fun formatTime(timestamp: Long): String {
        val sdf = timeFormatThreadLocal.get() ?: SimpleDateFormat("HH:mm", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = dateShortThreadLocal.get() ?: SimpleDateFormat("dd MMM, HH:mm", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateMedium(timestamp: Long): String {
        val sdf = dateMediumThreadLocal.get() ?: SimpleDateFormat("d MMM yyyy", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateFull(timestamp: Long): String {
        val sdf = dateFullThreadLocal.get() ?: SimpleDateFormat("EEEE, d MMMM yyyy", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = monthYearThreadLocal.get() ?: SimpleDateFormat("MMMM yyyy", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDayName(timestamp: Long): String {
        val sdf = dayNameThreadLocal.get() ?: SimpleDateFormat("EEEE", idLocale)
        return sdf.format(Date(timestamp))
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance()
        c1.timeInMillis = t1
        val c2 = Calendar.getInstance()
        c2.timeInMillis = t2
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }

    fun isYesterday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(timestamp, cal.timeInMillis)
    }

    fun formatRelativeDate(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Hari Ini • ${formatDateMedium(timestamp)}"
            isYesterday(timestamp) -> "Kemarin • ${formatDateMedium(timestamp)}"
            else -> formatDateFull(timestamp)
        }
    }

    fun getDayTimestampRange(timestamp: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    fun getPeriodTimestampRange(period: TimePeriod, customDateMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (period) {
            TimePeriod.HARI_INI -> getDayTimestampRange(System.currentTimeMillis())
            TimePeriod.KEMARIN -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                getDayTimestampRange(cal.timeInMillis)
            }
            TimePeriod.TANGGAL_PILIHAN -> getDayTimestampRange(customDateMillis)
            TimePeriod.TUJUH_HARI -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            TimePeriod.BULAN_INI -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            TimePeriod.SEMUA -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
