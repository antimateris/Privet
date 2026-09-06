package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.example.data.repository.FirestoreWashRepository
import com.example.data.repository.WashRepository
import com.example.util.CorporateReportGenerator
import com.example.util.FormatUtils
import com.example.util.TimePeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WashFinancialSummary(
    val totalMotors: Int = 0,
    val totalGrossRevenue: Long = 0L,
    val totalWasherShare: Long = 0L,
    val totalOwnerShare: Long = 0L,
    val disputedMotors: Int = 0,
    val disputedAmount: Long = 0L
)

data class WasherShareBreakdown(
    val washerName: String,
    val motorCount: Int,
    val totalShare: Long
)

enum class UserRole(val title: String, val subtitle: String) {
    KASIR("Kasir (Operator)", "Input motor cuci & cetak struk"),
    MANAGER_KEUANGAN("Manager Keuangan", "Verifikasi, validasi & sanggah transaksi"),
    PEMILIK("Pemilik Usaha (Owner)", "Akses penuh keuangan, laba & tabungan")
}

data class CurrentUser(
    val name: String = "Kasir Utama",
    val role: UserRole = UserRole.KASIR
)

enum class SavingsTargetFor(val label: String, val desc: String) {
    PEMILIK("Untuk Pemilik Usaha", "Tabungan pengadaan inventaris/alat steam (kompresor, renovasi)"),
    KARYAWAN("Untuk Karyawan / Pencuci", "Tabungan bersama / bonus kesejahteraan karyawan dari omset cuci")
}

enum class SavingsSource(val label: String, val shortDesc: String) {
    TOTAL_REVENUE("Total Omset Harian", "Disisihkan dari seluruh omset kotor cuci"),
    OWNER_SHARE("Kas Bersih Pemilik", "Disisihkan dari laba bersih pemilik (50%)"),
    WASHER_SHARE("Porsi Bagi Hasil Karyawan", "Disisihkan dari tabungan/jatah pencuci")
}

data class DreamGoalConfig(
    val itemName: String = "Kompresor Steam Matrix 2 HP",
    val targetAmount: Long = 2_200_000L,
    val initialSavings: Long = 0L,
    val savingSource: SavingsSource = SavingsSource.TOTAL_REVENUE,
    val targetFor: SavingsTargetFor = SavingsTargetFor.PEMILIK,
    val dailyTargetAmount: Long = 100_000L
)

data class DreamGoalProgress(
    val config: DreamGoalConfig,
    val todayGrossRevenue: Long,
    val todayOwnerShare: Long,
    val todayRevenue: Long,
    val totalAccumulatedRevenue: Long,
    val totalSaved: Long,
    val remainingAmount: Long,
    val progressPercent: Float,
    val todayContributionPercent: Float,
    val todayDailyTargetPercent: Float,
    val isAchieved: Boolean,
    val estimatedDaysRemaining: Int,
    val averageDailyRevenue: Long
)

data class DayRevenueSummary(
    val dateMillis: Long,
    val motorCount: Int,
    val totalRevenue: Long,
    val totalOwnerShare: Long
)

data class MonthlyDayRecord(
    val dayOfMonth: Int,
    val dateMillis: Long,
    val dateKey: String,
    val dayName: String,
    val motorCount: Int,
    val grossRevenue: Long,
    val washerShare: Long,
    val ownerShare: Long,
    val topWasherName: String = ""
)

data class MonthlyWasherStat(
    val washerName: String,
    val motorCount: Int,
    val totalShare: Long,
    val percentage: Float
)

data class MonthlySummaryData(
    val year: Int,
    val month: Int,
    val monthName: String,
    val totalMotors: Int,
    val totalGrossRevenue: Long,
    val totalWasherShare: Long,
    val totalOwnerShare: Long,
    val activeDaysCount: Int,
    val daysInMonth: Int,
    val averageDailyMotors: Float,
    val averageDailyGross: Long,
    val averageDailyOwnerShare: Long,
    val peakDay: MonthlyDayRecord?,
    val dailyBreakdown: List<MonthlyDayRecord>,
    val washerStats: List<MonthlyWasherStat>,
    val cashAmount: Long,
    val qrisAmount: Long,
    val transferAmount: Long
)

data class CompanyProfile(
    val companyName: String = "PT. LION STEAM MOTOR",
    val divisionName: String = "DIVISI OPERASIONAL & PERAWATAN KENDARAAN",
    val companyAddress: String = "Kawasan Sentra Bisnis Otomotif Terpadu",
    val companyPhone: String = "0812-3456-7890",
    val legalRegNo: String = "AHU-0038912.AH.01.01 / NIB: 9120003482190",
    val directorName: String = "Bpk. Mohamad Ricky, S.H. (Direktur Utama)",
    val financeManagerName: String = "Muhamad Agung Kurniawan (Manajer Keuangan)",
    val cashierName: String = "Admin / Kasir Operasional"
)

class WashViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WashRepository

    private val _selectedPeriod = MutableStateFlow(TimePeriod.HARI_INI)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _monthlyRecapYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val monthlyRecapYear: StateFlow<Int> = _monthlyRecapYear.asStateFlow()

    private val _monthlyRecapMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val monthlyRecapMonth: StateFlow<Int> = _monthlyRecapMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedQuickWasher = MutableStateFlow("")
    val selectedQuickWasher: StateFlow<String> = _selectedQuickWasher.asStateFlow()

    private val firestoreRepository by lazy {
        FirestoreWashRepository(context = getApplication<Application>())
    }

    private val _cloudSyncStatus = MutableStateFlow("Data tersimpan di HP (Offline)")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    private val userPrefs by lazy {
        getApplication<Application>().getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    }

    private val _currentUser = MutableStateFlow(loadCurrentUser())
    val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    private fun loadCurrentUser(): CurrentUser {
        val roleStr = userPrefs.getString("user_role", UserRole.KASIR.name) ?: UserRole.KASIR.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.KASIR
        }
        val defaultName = when (role) {
            UserRole.KASIR -> "Kasir Utama"
            UserRole.MANAGER_KEUANGAN -> "Manager Keuangan"
            UserRole.PEMILIK -> "Pemilik Usaha"
        }
        val name = userPrefs.getString("account_name_${role.name}", userPrefs.getString("user_name", defaultName) ?: defaultName) ?: defaultName
        return CurrentUser(name = name, role = role)
    }

    fun getAccountName(role: UserRole): String {
        val defaultName = when (role) {
            UserRole.KASIR -> "Kasir Utama"
            UserRole.MANAGER_KEUANGAN -> "Manager Keuangan"
            UserRole.PEMILIK -> "Pemilik Usaha"
        }
        return userPrefs.getString("account_name_${role.name}", defaultName) ?: defaultName
    }

    fun getAccountPassword(role: UserRole): String {
        return userPrefs.getString("account_pass_${role.name}", "1234") ?: "1234"
    }

    fun verifyPassword(role: UserRole, enteredPass: String): Boolean {
        val saved = getAccountPassword(role)
        return enteredPass.trim() == saved.trim()
    }

    fun switchUserRoleWithAuth(
        targetRole: UserRole,
        name: String,
        passwordInput: String,
        newPasswordInput: String? = null
    ): Pair<Boolean, String> {
        val savedPass = getAccountPassword(targetRole)
        if (passwordInput.trim() != savedPass.trim()) {
            return Pair(false, "Password salah untuk akun ${targetRole.title}!")
        }

        val finalName = name.ifBlank { getAccountName(targetRole) }.trim()
        val finalPassword = if (!newPasswordInput.isNullOrBlank()) newPasswordInput.trim() else savedPass

        userPrefs.edit()
            .putString("user_role", targetRole.name)
            .putString("user_name", finalName)
            .putString("account_name_${targetRole.name}", finalName)
            .putString("account_pass_${targetRole.name}", finalPassword)
            .apply()

        _currentUser.value = CurrentUser(name = finalName, role = targetRole)
        return Pair(true, "Berhasil masuk sebagai $finalName (${targetRole.title})")
    }

    fun switchUserRole(role: UserRole, name: String) {
        val finalName = name.trim().ifBlank { getAccountName(role) }
        val updated = CurrentUser(name = finalName, role = role)
        _currentUser.value = updated
        userPrefs.edit()
            .putString("user_role", role.name)
            .putString("user_name", finalName)
            .putString("account_name_${role.name}", finalName)
            .apply()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WashRepository(database.washDao())

        // Start listening to real-time cloud updates from other devices (e.g. Kasir / Owner)
        if (firestoreRepository.isAvailable()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    firestoreRepository.listenTransactions().collect { remoteRecords ->
                        if (remoteRecords.isNotEmpty()) {
                            val local = repository.getAllRecords().first()
                            val localTimestamps = local.map { it.timestamp }.toSet()
                            var newlyAdded = 0
                            for (remote in remoteRecords) {
                                if (remote.timestamp !in localTimestamps) {
                                    repository.insertRecord(remote)
                                    newlyAdded++
                                }
                            }
                            _cloudSyncStatus.value = "Real-time aktif • ${remoteRecords.size} transaksi di cloud"
                        }
                    }
                } catch (_: Exception) {
                    _cloudSyncStatus.value = "Data tersimpan di HP (Offline)"
                }
            }

            // Automatic Periodic Sync every 5 minutes (Auto-sync 5 menit)
            viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    delay(5 * 60 * 1000L) // 5 minutes
                    try {
                        val records = repository.getAllRecords().first()
                        if (records.isNotEmpty()) {
                            firestoreRepository.batchUploadLocalRecords(records)
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            _cloudSyncStatus.value = "Auto-sync 5 mnt • Terakhir: $timeStr"
                        }
                    } catch (_: Exception) {
                        // Ignore transient network failures
                    }
                }
            }
        } else {
            _cloudSyncStatus.value = "Data tersimpan di HP (Offline)"
        }
    }

    val activeWorkers: StateFlow<List<Worker>> = repository.getActiveWorkers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allRecords: StateFlow<List<WashRecord>> = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of "yyyy-MM-dd" to DayRevenueSummary for fast calendar indicators
    val dailySummariesMap: StateFlow<Map<String, DayRevenueSummary>> = allRecords.map { records ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val map = mutableMapOf<String, DayRevenueSummary>()
        for (r in records) {
            val key = sdf.format(Date(r.timestamp))
            val prev = map[key]
            if (prev == null) {
                map[key] = DayRevenueSummary(
                    dateMillis = r.timestamp,
                    motorCount = r.motorCount,
                    totalRevenue = r.totalPrice,
                    totalOwnerShare = r.totalOwnerShare
                )
            } else {
                map[key] = prev.copy(
                    motorCount = prev.motorCount + r.motorCount,
                    totalRevenue = prev.totalRevenue + r.totalPrice,
                    totalOwnerShare = prev.totalOwnerShare + r.totalOwnerShare
                )
            }
        }
        map
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Combined filtered records based on time period, custom date, and search query
    val filteredRecords: StateFlow<List<WashRecord>> = combine(
        allRecords,
        _selectedPeriod,
        _selectedDateMillis,
        _searchQuery
    ) { records, period, customDate, query ->
        val (start, end) = FormatUtils.getPeriodTimestampRange(period, customDate)
        val periodFiltered = records.filter { it.timestamp in start..end }
        if (query.isBlank()) {
            periodFiltered
        } else {
            val q = query.trim().lowercase()
            periodFiltered.filter { record ->
                record.licensePlate.lowercase().contains(q) ||
                record.washerName.lowercase().contains(q) ||
                record.note.lowercase().contains(q) ||
                record.motorType.lowercase().contains(q)
            }
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculation for the active period
    val financialSummary: StateFlow<WashFinancialSummary> = filteredRecords.combine(_selectedPeriod) { records, _ ->
        var motors = 0
        var gross = 0L
        var washer = 0L
        var owner = 0L
        var disputedCount = 0
        var disputedAmt = 0L

        for (item in records) {
            if (item.validationStatus == "DISPUTED") {
                disputedCount += item.motorCount
                disputedAmt += item.totalPrice
            }
            motors += item.motorCount
            gross += item.totalPrice
            washer += item.totalWasherShare
            owner += item.totalOwnerShare
        }

        WashFinancialSummary(
            totalMotors = motors,
            totalGrossRevenue = gross,
            totalWasherShare = washer,
            totalOwnerShare = owner,
            disputedMotors = disputedCount,
            disputedAmount = disputedAmt
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WashFinancialSummary())

    // Washer share breakdown
    val washerBreakdowns: StateFlow<List<WasherShareBreakdown>> = filteredRecords.combine(_selectedPeriod) { records, _ ->
        val mapCount = mutableMapOf<String, Int>()
        val mapShare = mutableMapOf<String, Long>()

        for (item in records) {
            val name = if (item.washerName.isBlank()) "Tanpa Petugas" else item.washerName
            mapCount[name] = (mapCount[name] ?: 0) + item.motorCount
            mapShare[name] = (mapShare[name] ?: 0L) + item.totalWasherShare
        }

        mapCount.map { (name, count) ->
            WasherShareBreakdown(
                washerName = name,
                motorCount = count,
                totalShare = mapShare[name] ?: 0L
            )
        }.sortedByDescending { it.motorCount }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-depth revenue and performance analysis
    val revenueAnalysis: StateFlow<RevenueAnalysisData> = combine(
        filteredRecords,
        _selectedPeriod
    ) { records, period ->
        computeRevenueAnalysis(records, period)
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RevenueAnalysisData())

    // Monthly Recap and Summary Data
    val monthlySummaryData: StateFlow<MonthlySummaryData> = combine(
        allRecords,
        _monthlyRecapYear,
        _monthlyRecapMonth
    ) { records, year, month ->
        val monthNames = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val dayNames = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

        val calStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val maxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH)

        val calEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, maxDays)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startMillis = calStart.timeInMillis
        val endMillis = calEnd.timeInMillis

        val monthRecords = records.filter { it.timestamp in startMillis..endMillis }

        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEEE", Locale("id", "ID"))

        val recordsByDay = monthRecords.groupBy { r ->
            val cal = Calendar.getInstance().apply { timeInMillis = r.timestamp }
            cal.get(Calendar.DAY_OF_MONTH)
        }

        val dailyBreakdown = mutableListOf<MonthlyDayRecord>()
        var totalMotors = 0
        var totalGross = 0L
        var totalWasherShare = 0L
        var totalOwnerShare = 0L
        var activeDays = 0

        var cash = 0L
        var qris = 0L
        var transfer = 0L

        for (day in 1..maxDays) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val dayKey = sdfKey.format(dayCal.time)
            val dayName = try {
                sdfDay.format(dayCal.time)
            } catch (_: Exception) {
                dayNames.getOrElse(dayCal.get(Calendar.DAY_OF_WEEK) - 1) { "" }
            }

            val dayRecs = recordsByDay[day] ?: emptyList()
            val dayMotors = dayRecs.sumOf { it.motorCount }
            val dayGross = dayRecs.sumOf { it.totalPrice }
            val dayWasher = dayRecs.sumOf { it.totalWasherShare }
            val dayOwner = dayRecs.sumOf { it.totalOwnerShare }

            if (dayMotors > 0) {
                activeDays++
            }

            totalMotors += dayMotors
            totalGross += dayGross
            totalWasherShare += dayWasher
            totalOwnerShare += dayOwner

            val topWasher = dayRecs.groupBy { it.washerName }
                .maxByOrNull { entry -> entry.value.sumOf { it.motorCount } }
                ?.let { entry ->
                    val name = entry.key.ifBlank { "Umum" }
                    val count = entry.value.sumOf { it.motorCount }
                    "$name ($count m)"
                } ?: ""

            dailyBreakdown.add(
                MonthlyDayRecord(
                    dayOfMonth = day,
                    dateMillis = dayCal.timeInMillis,
                    dateKey = dayKey,
                    dayName = dayName,
                    motorCount = dayMotors,
                    grossRevenue = dayGross,
                    washerShare = dayWasher,
                    ownerShare = dayOwner,
                    topWasherName = topWasher
                )
            )
        }

        for (r in monthRecords) {
            when {
                r.paymentMethod.equals("Tunai", ignoreCase = true) -> cash += r.totalPrice
                r.paymentMethod.contains("QRIS", ignoreCase = true) -> qris += r.totalPrice
                else -> transfer += r.totalPrice
            }
        }

        val washerGroups = monthRecords.groupBy { it.washerName.ifBlank { "Belum Ditentukan" } }
        val washerStats = washerGroups.map { (name, list) ->
            val wMotors = list.sumOf { it.motorCount }
            val wShare = list.sumOf { it.totalWasherShare }
            val pct = if (totalMotors > 0) (wMotors.toFloat() / totalMotors.toFloat()) * 100f else 0f
            MonthlyWasherStat(
                washerName = name,
                motorCount = wMotors,
                totalShare = wShare,
                percentage = pct
            )
        }.sortedByDescending { it.motorCount }

        val peakDay = dailyBreakdown.filter { it.motorCount > 0 }.maxByOrNull { it.motorCount }

        val now = Calendar.getInstance()
        val isCurrentMonth = now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == month
        val elapsedDays = if (isCurrentMonth) now.get(Calendar.DAY_OF_MONTH) else maxDays
        val divisor = elapsedDays.coerceAtLeast(1)

        val avgMotors = totalMotors.toFloat() / divisor.toFloat()
        val avgGross = totalGross / divisor
        val avgOwner = totalOwnerShare / divisor

        MonthlySummaryData(
            year = year,
            month = month,
            monthName = monthNames.getOrElse(month) { "Bulan ${month + 1}" },
            totalMotors = totalMotors,
            totalGrossRevenue = totalGross,
            totalWasherShare = totalWasherShare,
            totalOwnerShare = totalOwnerShare,
            activeDaysCount = activeDays,
            daysInMonth = maxDays,
            averageDailyMotors = avgMotors,
            averageDailyGross = avgGross,
            averageDailyOwnerShare = avgOwner,
            peakDay = peakDay,
            dailyBreakdown = dailyBreakdown,
            washerStats = washerStats,
            cashAmount = cash,
            qrisAmount = qris,
            transferAmount = transfer
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlySummaryData(
            year = Calendar.getInstance().get(Calendar.YEAR),
            month = Calendar.getInstance().get(Calendar.MONTH),
            monthName = "",
            totalMotors = 0,
            totalGrossRevenue = 0L,
            totalWasherShare = 0L,
            totalOwnerShare = 0L,
            activeDaysCount = 0,
            daysInMonth = 30,
            averageDailyMotors = 0f,
            averageDailyGross = 0L,
            averageDailyOwnerShare = 0L,
            peakDay = null,
            dailyBreakdown = emptyList(),
            washerStats = emptyList(),
            cashAmount = 0L,
            qrisAmount = 0L,
            transferAmount = 0L
        )
    )

    fun setMonthlyRecapMonth(year: Int, month: Int) {
        _monthlyRecapYear.value = year
        _monthlyRecapMonth.value = month
    }

    fun nextMonthlyRecapMonth() {
        val m = _monthlyRecapMonth.value
        val y = _monthlyRecapYear.value
        if (m == 11) {
            _monthlyRecapYear.value = y + 1
            _monthlyRecapMonth.value = 0
        } else {
            _monthlyRecapMonth.value = m + 1
        }
    }

    fun prevMonthlyRecapMonth() {
        val m = _monthlyRecapMonth.value
        val y = _monthlyRecapYear.value
        if (m == 0) {
            _monthlyRecapYear.value = y - 1
            _monthlyRecapMonth.value = 11
        } else {
            _monthlyRecapMonth.value = m - 1
        }
    }

    fun resetMonthlyRecapToCurrent() {
        val cal = Calendar.getInstance()
        _monthlyRecapYear.value = cal.get(Calendar.YEAR)
        _monthlyRecapMonth.value = cal.get(Calendar.MONTH)
    }

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("dream_goal_prefs", Context.MODE_PRIVATE)
    }

    private val _dreamGoalConfig = MutableStateFlow(loadDreamGoalConfig())
    val dreamGoalConfig: StateFlow<DreamGoalConfig> = _dreamGoalConfig.asStateFlow()

    private fun loadDreamGoalConfig(): DreamGoalConfig {
        val itemName = prefs.getString("item_name", "Kompresor Steam Matrix 2 HP") ?: "Kompresor Steam Matrix 2 HP"
        val targetAmount = prefs.getLong("target_amount", 2_200_000L)
        val initialSavings = prefs.getLong("initial_savings", 0L)
        val sourceStr = prefs.getString("source", SavingsSource.TOTAL_REVENUE.name) ?: SavingsSource.TOTAL_REVENUE.name
        val source = try {
            SavingsSource.valueOf(sourceStr)
        } catch (_: Exception) {
            SavingsSource.TOTAL_REVENUE
        }
        val targetForStr = prefs.getString("target_for", SavingsTargetFor.PEMILIK.name) ?: SavingsTargetFor.PEMILIK.name
        val targetFor = try {
            SavingsTargetFor.valueOf(targetForStr)
        } catch (_: Exception) {
            SavingsTargetFor.PEMILIK
        }
        val dailyTarget = prefs.getLong("daily_target", 100_000L)
        return DreamGoalConfig(
            itemName = itemName,
            targetAmount = targetAmount,
            initialSavings = initialSavings,
            savingSource = source,
            targetFor = targetFor,
            dailyTargetAmount = dailyTarget
        )
    }

    fun updateDreamGoalConfig(newConfig: DreamGoalConfig) {
        _dreamGoalConfig.value = newConfig
        prefs.edit()
            .putString("item_name", newConfig.itemName)
            .putLong("target_amount", newConfig.targetAmount)
            .putLong("initial_savings", newConfig.initialSavings)
            .putString("source", newConfig.savingSource.name)
            .putString("target_for", newConfig.targetFor.name)
            .putLong("daily_target", newConfig.dailyTargetAmount)
            .apply()
    }

    private val companyPrefs by lazy {
        getApplication<Application>().getSharedPreferences("company_profile_prefs", Context.MODE_PRIVATE)
    }

    private val _companyProfile = MutableStateFlow(loadCompanyProfile())
    val companyProfile: StateFlow<CompanyProfile> = _companyProfile.asStateFlow()

    private fun loadCompanyProfile(): CompanyProfile {
        return CompanyProfile(
            companyName = companyPrefs.getString("company_name", "PT. LION STEAM MOTOR") ?: "PT. LION STEAM MOTOR",
            divisionName = companyPrefs.getString("division_name", "DIVISI OPERASIONAL & PERAWATAN KENDARAAN") ?: "DIVISI OPERASIONAL & PERAWATAN KENDARAAN",
            companyAddress = companyPrefs.getString("company_address", "Kawasan Sentra Bisnis Otomotif Terpadu") ?: "Kawasan Sentra Bisnis Otomotif Terpadu",
            companyPhone = companyPrefs.getString("company_phone", "0812-3456-7890") ?: "0812-3456-7890",
            legalRegNo = companyPrefs.getString("legal_reg_no", "AHU-0038912.AH.01.01 / NIB: 9120003482190") ?: "AHU-0038912.AH.01.01 / NIB: 9120003482190",
            directorName = companyPrefs.getString("director_name", "Bpk. Mohamad Ricky, S.H. (Direktur Utama)") ?: "Bpk. Mohamad Ricky, S.H. (Direktur Utama)",
            financeManagerName = companyPrefs.getString("finance_manager_name", "Muhamad Agung Kurniawan (Manajer Keuangan)") ?: "Muhamad Agung Kurniawan (Manajer Keuangan)",
            cashierName = companyPrefs.getString("cashier_name", "Admin / Kasir Operasional") ?: "Admin / Kasir Operasional"
        )
    }

    fun updateCompanyProfile(newProfile: CompanyProfile) {
        _companyProfile.value = newProfile
        companyPrefs.edit()
            .putString("company_name", newProfile.companyName)
            .putString("division_name", newProfile.divisionName)
            .putString("company_address", newProfile.companyAddress)
            .putString("company_phone", newProfile.companyPhone)
            .putString("legal_reg_no", newProfile.legalRegNo)
            .putString("director_name", newProfile.directorName)
            .putString("finance_manager_name", newProfile.financeManagerName)
            .putString("cashier_name", newProfile.cashierName)
            .apply()
    }

    fun generateDocumentNumber(prefix: String = "FIN-OPS", month: Int, year: Int): String {
        val romanMonths = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
        val roman = if (month in 1..12) romanMonths[month - 1] else "IX"
        val serial = String.format(Locale.getDefault(), "%03d", (month * 7 + (year % 100)) % 900 + 101)
        val shortName = _companyProfile.value.companyName
            .replace("PT.", "", ignoreCase = true)
            .replace("PT", "", ignoreCase = true)
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "KMG" }
        return "$serial/$prefix/PT-$shortName/$roman/$year"
    }

    // Reactive Dream Goal Progress based on daily revenue and total accumulated income
    val dreamGoalProgress: StateFlow<DreamGoalProgress> = combine(
        allRecords,
        _dreamGoalConfig
    ) { records, config ->
        val (todayStart, todayEnd) = FormatUtils.getPeriodTimestampRange(TimePeriod.HARI_INI)
        val todayRecords = records.filter { it.timestamp in todayStart..todayEnd }

        val todayGross = todayRecords.sumOf { it.totalPrice }
        val todayOwner = todayRecords.sumOf { it.totalOwnerShare }
        val todayWasher = todayRecords.sumOf { it.totalWasherShare }

        val totalGross = records.sumOf { it.totalPrice }
        val totalOwner = records.sumOf { it.totalOwnerShare }
        val totalWasher = records.sumOf { it.totalWasherShare }

        val todayRev = when (config.savingSource) {
            SavingsSource.TOTAL_REVENUE -> todayGross
            SavingsSource.OWNER_SHARE -> todayOwner
            SavingsSource.WASHER_SHARE -> todayWasher
        }
        val totalAcc = when (config.savingSource) {
            SavingsSource.TOTAL_REVENUE -> totalGross
            SavingsSource.OWNER_SHARE -> totalOwner
            SavingsSource.WASHER_SHARE -> totalWasher
        }

        val totalSaved = config.initialSavings + totalAcc
        val remaining = (config.targetAmount - totalSaved).coerceAtLeast(0L)
        val progress = if (config.targetAmount > 0) (totalSaved.toFloat() / config.targetAmount.toFloat()).coerceIn(0f, 1f) else 1f
        val todayContrib = if (config.targetAmount > 0) (todayRev.toFloat() / config.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
        val todayDailyPercent = if (config.dailyTargetAmount > 0) (todayRev.toFloat() / config.dailyTargetAmount.toFloat()).coerceIn(0f, 1f) else 0f

        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val distinctDays = records.map { dateFmt.format(Date(it.timestamp)) }.distinct().size.coerceAtLeast(1)
        val avgDaily = if (distinctDays > 0) totalAcc / distinctDays else todayRev
        val effectiveDaily = if (avgDaily > 0) avgDaily else (if (todayRev > 0) todayRev else 50_000L)

        val estDays = if (remaining <= 0) 0 else ceil(remaining.toDouble() / effectiveDaily.toDouble()).toInt()

        DreamGoalProgress(
            config = config,
            todayGrossRevenue = todayGross,
            todayOwnerShare = todayOwner,
            todayRevenue = todayRev,
            totalAccumulatedRevenue = totalAcc,
            totalSaved = totalSaved,
            remainingAmount = remaining,
            progressPercent = progress,
            todayContributionPercent = todayContrib,
            todayDailyTargetPercent = todayDailyPercent,
            isAchieved = totalSaved >= config.targetAmount,
            estimatedDaysRemaining = estDays,
            averageDailyRevenue = effectiveDaily
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DreamGoalProgress(
            config = DreamGoalConfig(),
            todayGrossRevenue = 0L,
            todayOwnerShare = 0L,
            todayRevenue = 0L,
            totalAccumulatedRevenue = 0L,
            totalSaved = 0L,
            remainingAmount = 2_200_000L,
            progressPercent = 0f,
            todayContributionPercent = 0f,
            todayDailyTargetPercent = 0f,
            isAchieved = false,
            estimatedDaysRemaining = 0,
            averageDailyRevenue = 0L
        )
    )

    private fun computeRevenueAnalysis(records: List<WashRecord>, period: TimePeriod): RevenueAnalysisData {
        if (records.isEmpty()) {
            return RevenueAnalysisData(
                insights = listOf(
                    "Belum ada data cuci pada periode ini. Mulai catat motor untuk melihat analisis performa dan proyeksi pendapatan."
                )
            )
        }

        val totalMotors = records.sumOf { it.motorCount }
        val totalGross = records.sumOf { it.totalPrice }
        val totalWasher = records.sumOf { it.totalWasherShare }
        val totalOwner = records.sumOf { it.totalOwnerShare }

        val ownerMargin = if (totalGross > 0) {
            (totalOwner.toFloat() / totalGross.toFloat()) * 100f
        } else 0f

        // Calculate days for average
        val daysCount = when (period) {
            TimePeriod.HARI_INI, TimePeriod.KEMARIN, TimePeriod.TANGGAL_PILIHAN -> 1
            TimePeriod.TUJUH_HARI -> 7
            TimePeriod.BULAN_INI -> {
                val cal = Calendar.getInstance()
                cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            }
            TimePeriod.SEMUA -> {
                val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val distinctDays = records.map { dateFmt.format(Date(it.timestamp)) }.distinct().size
                distinctDays.coerceAtLeast(1)
            }
        }

        val avgDailyMotors = totalMotors.toFloat() / daysCount.toFloat()
        val avgDailyGross = (totalGross / daysCount.toLong())
        val avgDailyOwner = (totalOwner / daysCount.toLong())

        val projMonthlyGross = avgDailyGross * 30L
        val projMonthlyOwner = avgDailyOwner * 30L

        // Peak Hours Analysis (Pagi: 06-11, Siang: 11-15, Sore: 15-18, Malam: 18-23)
        val cal = Calendar.getInstance()
        var pagiMotors = 0
        var siangMotors = 0
        var soreMotors = 0
        var malamMotors = 0

        for (r in records) {
            cal.timeInMillis = r.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..10 -> pagiMotors += r.motorCount
                in 11..14 -> siangMotors += r.motorCount
                in 15..17 -> soreMotors += r.motorCount
                else -> malamMotors += r.motorCount
            }
        }

        val totalSlotMotors = (pagiMotors + siangMotors + soreMotors + malamMotors).coerceAtLeast(1)
        val timeSlots = listOf(
            TimeSlotStat("Pagi", "06:00 - 11:00", pagiMotors, (pagiMotors.toFloat() / totalSlotMotors) * 100f),
            TimeSlotStat("Siang", "11:00 - 15:00", siangMotors, (siangMotors.toFloat() / totalSlotMotors) * 100f),
            TimeSlotStat("Sore", "15:00 - 18:00", soreMotors, (soreMotors.toFloat() / totalSlotMotors) * 100f),
            TimeSlotStat("Malam", "18:00 - Tutup", malamMotors, (malamMotors.toFloat() / totalSlotMotors) * 100f)
        )

        val peakSlotStat = timeSlots.maxByOrNull { it.motorCount }
        val peakSlotDesc = if (peakSlotStat != null && peakSlotStat.motorCount > 0) {
            "${peakSlotStat.slotName} (${peakSlotStat.timeRange})"
        } else {
            "Merata"
        }

        // Payment Methods Breakdown
        val pmMap = mutableMapOf<String, Pair<Int, Long>>()
        for (r in records) {
            val method = if (r.paymentMethod.isBlank()) "Tunai" else r.paymentMethod
            val current = pmMap[method] ?: Pair(0, 0L)
            pmMap[method] = Pair(current.first + r.motorCount, current.second + r.totalPrice)
        }

        val paymentMethods = pmMap.map { (method, pair) ->
            PaymentMethodStat(
                method = method,
                motorCount = pair.first,
                totalAmount = pair.second,
                percentage = if (totalGross > 0) (pair.second.toFloat() / totalGross.toFloat()) * 100f else 0f
            )
        }.sortedByDescending { it.totalAmount }

        // Chart Data Points
        val chartPoints = if (period == TimePeriod.HARI_INI || period == TimePeriod.KEMARIN) {
            // Group by 2-hour intervals: 08:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00
            val intervals = listOf(
                "08:00" to (6..9),
                "11:00" to (10..12),
                "14:00" to (13..15),
                "17:00" to (16..18),
                "20:00" to (19..23)
            )
            intervals.map { (label, range) ->
                val matching = records.filter {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.HOUR_OF_DAY) in range
                }
                DailyChartPoint(
                    label = label,
                    grossRevenue = matching.sumOf { it.totalPrice },
                    washerShare = matching.sumOf { it.totalWasherShare },
                    ownerShare = matching.sumOf { it.totalOwnerShare },
                    motorCount = matching.sumOf { it.motorCount }
                )
            }
        } else {
            // Group by day format dd/MM
            val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val sortedRecords = records.sortedBy { it.timestamp }
            val groupedByDay = sortedRecords.groupBy { dayFormat.format(Date(it.timestamp)) }
            groupedByDay.map { (dayLabel, dayList) ->
                DailyChartPoint(
                    label = dayLabel,
                    grossRevenue = dayList.sumOf { it.totalPrice },
                    washerShare = dayList.sumOf { it.totalWasherShare },
                    ownerShare = dayList.sumOf { it.totalOwnerShare },
                    motorCount = dayList.sumOf { it.motorCount }
                )
            }.takeLast(10) // Keep last 10 points for clean graph display
        }

        // Actionable Insights
        val insights = mutableListOf<String>()
        insights.add("Kas bersih pemilik menyerap ${String.format(Locale.getDefault(), "%.0f", ownerMargin)}% (${FormatUtils.formatRupiah(totalOwner)}) dari omset kotor ${FormatUtils.formatRupiah(totalGross)}.")
        if (peakSlotDesc != "Merata") {
            insights.add("Waktu paling ramai pencucian berada di $peakSlotDesc. Siapkan armada pencuci dan sampo steam maksimal pada waktu ini.")
        }
        if (paymentMethods.isNotEmpty()) {
            val topPm = paymentMethods.first()
            insights.add("Sebesar ${String.format(Locale.getDefault(), "%.0f", topPm.percentage)}% transaksi menggunakan ${topPm.method}.")
        }
        if (avgDailyMotors > 0) {
            insights.add("Rata-rata volume cuci ${String.format(Locale.getDefault(), "%.1f", avgDailyMotors)} motor/hari. Jika stabil, estimasi kas bersih pemilik mencapai ${FormatUtils.formatRupiah(projMonthlyOwner)} per bulan.")
        }

        return RevenueAnalysisData(
            totalMotors = totalMotors,
            totalGrossRevenue = totalGross,
            totalWasherShare = totalWasher,
            totalOwnerShare = totalOwner,
            ownerMarginPercent = ownerMargin,
            averageDailyMotors = avgDailyMotors,
            averageDailyGross = avgDailyGross,
            averageDailyOwnerNet = avgDailyOwner,
            projectedMonthlyGross = projMonthlyGross,
            projectedMonthlyOwnerNet = projMonthlyOwner,
            peakTimeSlot = peakSlotDesc,
            timeSlots = timeSlots,
            paymentMethods = paymentMethods,
            chartPoints = chartPoints,
            insights = insights
        )
    }

    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun setSelectedDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
        _selectedPeriod.value = TimePeriod.TANGGAL_PILIHAN
    }

    fun selectToday() {
        _selectedDateMillis.value = System.currentTimeMillis()
        _selectedPeriod.value = TimePeriod.HARI_INI
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectQuickWasher(name: String) {
        _selectedQuickWasher.value = if (_selectedQuickWasher.value == name) "" else name
    }

    fun setOrUpdateDailyRevenue(
        targetDateMillis: Long,
        motorCount: Int,
        pricePerMotor: Long = 10000L,
        washerSharePerMotor: Long = 5000L,
        washerName: String = "",
        paymentMethod: String = "Tunai",
        note: String = "Rekapitulasi omset tanggal ini",
        replaceExistingForDay: Boolean = true
    ) {
        viewModelScope.launch {
            val (start, end) = FormatUtils.getDayTimestampRange(targetDateMillis)
            if (replaceExistingForDay) {
                repository.deleteRecordsBetween(start, end)
            }

            if (motorCount > 0) {
                val totalPrice = pricePerMotor * motorCount
                val totalWasherShare = washerSharePerMotor * motorCount
                val totalOwnerShare = totalPrice - totalWasherShare

                val cal = Calendar.getInstance()
                cal.timeInMillis = targetDateMillis
                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)

                val record = WashRecord(
                    motorCount = motorCount,
                    licensePlate = "",
                    motorType = "Standar (Matic/Bebek)",
                    washerName = washerName,
                    pricePerMotor = pricePerMotor,
                    washerSharePerMotor = washerSharePerMotor,
                    totalPrice = totalPrice,
                    totalWasherShare = totalWasherShare,
                    totalOwnerShare = totalOwnerShare,
                    paymentMethod = paymentMethod,
                    note = note,
                    timestamp = cal.timeInMillis,
                    createdBy = _currentUser.value.name,
                    validationStatus = "PENDING"
                )
                repository.insertRecord(record)
                firestoreRepository.saveTransaction(record)
            }

            _selectedDateMillis.value = targetDateMillis
            _selectedPeriod.value = TimePeriod.TANGGAL_PILIHAN
        }
    }

    fun quickAddMotorForDate(targetDateMillis: Long, count: Int = 1) {
        viewModelScope.launch {
            val washer = _selectedQuickWasher.value
            val pricePerMotor = 10000L
            val washerSharePerMotor = 5000L
            val totalPrice = pricePerMotor * count
            val totalWasherShare = washerSharePerMotor * count
            val totalOwnerShare = totalPrice - totalWasherShare

            val timestamp = if (FormatUtils.isToday(targetDateMillis)) {
                System.currentTimeMillis()
            } else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = targetDateMillis
                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }

            val record = WashRecord(
                motorCount = count,
                licensePlate = "",
                motorType = "Standar (Matic/Bebek)",
                washerName = washer,
                pricePerMotor = pricePerMotor,
                washerSharePerMotor = washerSharePerMotor,
                totalPrice = totalPrice,
                totalWasherShare = totalWasherShare,
                totalOwnerShare = totalOwnerShare,
                paymentMethod = "Tunai",
                note = "",
                timestamp = timestamp,
                createdBy = _currentUser.value.name,
                validationStatus = "PENDING"
            )
            repository.insertRecord(record)
            firestoreRepository.saveTransaction(record)
        }
    }

    fun quickAddMotor(count: Int = 1) {
        viewModelScope.launch {
            val washer = _selectedQuickWasher.value
            val pricePerMotor = 10000L
            val washerSharePerMotor = 5000L
            val totalPrice = pricePerMotor * count
            val totalWasherShare = washerSharePerMotor * count
            val totalOwnerShare = totalPrice - totalWasherShare

            val record = WashRecord(
                motorCount = count,
                licensePlate = "",
                motorType = "Standar (Matic/Bebek)",
                washerName = washer,
                pricePerMotor = pricePerMotor,
                washerSharePerMotor = washerSharePerMotor,
                totalPrice = totalPrice,
                totalWasherShare = totalWasherShare,
                totalOwnerShare = totalOwnerShare,
                paymentMethod = "Tunai",
                note = "",
                timestamp = System.currentTimeMillis(),
                createdBy = _currentUser.value.name,
                validationStatus = "PENDING"
            )
            repository.insertRecord(record)
            firestoreRepository.saveTransaction(record)
        }
    }

    fun addOrUpdateRecord(
        id: Long = 0,
        motorCount: Int,
        licensePlate: String,
        motorType: String,
        washerName: String,
        pricePerMotor: Long,
        washerSharePerMotor: Long,
        paymentMethod: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        existingRecord: WashRecord? = null
    ): WashRecord {
        val totalPrice = pricePerMotor * motorCount
        val totalWasherShare = washerSharePerMotor * motorCount
        val totalOwnerShare = totalPrice - totalWasherShare

        val record = WashRecord(
            id = id,
            motorCount = motorCount,
            licensePlate = licensePlate.trim().uppercase(),
            motorType = motorType,
            washerName = washerName.trim(),
            pricePerMotor = pricePerMotor,
            washerSharePerMotor = washerSharePerMotor,
            totalPrice = totalPrice,
            totalWasherShare = totalWasherShare,
            totalOwnerShare = totalOwnerShare,
            paymentMethod = paymentMethod,
            note = note.trim(),
            timestamp = timestamp,
            createdBy = existingRecord?.createdBy ?: _currentUser.value.name,
            validationStatus = existingRecord?.validationStatus ?: "PENDING",
            disputeReason = existingRecord?.disputeReason ?: "",
            disputedBy = existingRecord?.disputedBy ?: "",
            disputedAt = existingRecord?.disputedAt ?: 0L
        )

        viewModelScope.launch {
            val finalId = if (id == 0L) {
                repository.insertRecord(record)
            } else {
                repository.updateRecord(record)
                id
            }
            firestoreRepository.saveTransaction(record.copy(id = finalId))
        }

        return record
    }

    fun disputeTransaction(record: WashRecord, reason: String) {
        viewModelScope.launch {
            val updated = record.copy(
                validationStatus = "DISPUTED",
                disputeReason = reason.trim(),
                disputedBy = _currentUser.value.name,
                disputedAt = System.currentTimeMillis()
            )
            repository.updateRecord(updated)
            firestoreRepository.saveTransaction(updated)
        }
    }

    fun validateTransaction(record: WashRecord) {
        viewModelScope.launch {
            val updated = record.copy(
                validationStatus = "VALID",
                disputeReason = "",
                disputedBy = "",
                disputedAt = 0L
            )
            repository.updateRecord(updated)
            firestoreRepository.saveTransaction(updated)
        }
    }

    fun revokeDispute(record: WashRecord) {
        viewModelScope.launch {
            val updated = record.copy(
                validationStatus = "PENDING",
                disputeReason = "",
                disputedBy = "",
                disputedAt = 0L
            )
            repository.updateRecord(updated)
            firestoreRepository.saveTransaction(updated)
        }
    }

    fun deleteRecord(record: WashRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            firestoreRepository.deleteTransaction(record)
        }
    }

    fun deleteRecordsForDate(targetDateMillis: Long) {
        viewModelScope.launch {
            val (start, end) = FormatUtils.getDayTimestampRange(targetDateMillis)
            val recordsToDelete = repository.getAllRecords().first().filter { it.timestamp in start..end }
            repository.deleteRecordsBetween(start, end)
            for (rec in recordsToDelete) {
                firestoreRepository.deleteTransaction(rec)
            }
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            firestoreRepository.clearAllRemoteTransactions()
        }
    }

    fun syncAllLocalToCloud(onComplete: (Int, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            val records = repository.getAllRecords().first()
            if (records.isEmpty()) {
                onComplete(0, "Belum ada transaksi tersimpan untuk disinkronkan")
                return@launch
            }
            if (!firestoreRepository.isAvailable()) {
                _cloudSyncStatus.value = "Data tersimpan di HP (Offline)"
                onComplete(records.size, "Mode lokal aktif: ${records.size} transaksi aman tersimpan di HP")
                return@launch
            }
            val res = firestoreRepository.batchUploadLocalRecords(records)
            res.onSuccess { count ->
                _cloudSyncStatus.value = "Tersinkron • $count transaksi di cloud"
                onComplete(count, "Berhasil menyinkronkan $count transaksi ke Cloud")
            }.onFailure { _ ->
                _cloudSyncStatus.value = "Data tersimpan di HP (Offline)"
                onComplete(records.size, "Mode lokal aktif: ${records.size} transaksi aman tersimpan di HP")
            }
        }
    }

    fun addWorker(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertWorker(Worker(name = name.trim()))
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            if (_selectedQuickWasher.value == worker.name) {
                _selectedQuickWasher.value = ""
            }
        }
    }

    fun buildWhatsAppReportText(): String {
        return buildCorporateWhatsAppDailyReport()
    }

    fun buildCorporateWhatsAppDailyReport(): String {
        val period = _selectedPeriod.value
        val summary = financialSummary.value
        val breakdowns = washerBreakdowns.value
        val profile = _companyProfile.value
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val cal = Calendar.getInstance()
        val docNo = generateDocumentNumber("OPS-HARIAN", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        val ownerMargin = if (summary.totalGrossRevenue > 0) {
            (summary.totalOwnerShare.toFloat() / summary.totalGrossRevenue.toFloat()) * 100f
        } else 0f

        val sb = StringBuilder()
        sb.append("🏢 *${profile.companyName.uppercase()}*\n")
        sb.append("*${profile.divisionName}*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📑 *MEMORANDUM KEUANGAN HARIAN (AUDITED)*\n")
        sb.append("No. Dokumen : $docNo\n")
        sb.append("Sifat       : RAHASIA & TERBATAS\n")
        sb.append("Periode     : ${period.title} ($currentDateStr)\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("Kepada Yth,\n")
        sb.append("*Dewan Direksi & Manajemen ${profile.companyName}*\n\n")
        sb.append("Berikut terlampir Laporan Pertanggungjawaban Finansial Operasional Terverifikasi Sistem:\n\n")

        sb.append("📊 *I. RINGKASAN EKSEKUTIF OPERASIONAL*\n")
        sb.append("• Total Armada Dilayani  : *${summary.totalMotors} Unit*\n")
        sb.append("• Pendapatan Kotor (Gross): *${FormatUtils.formatRupiah(summary.totalGrossRevenue)}*\n")
        sb.append("• Beban Jasa Tenaga Cuci : *${FormatUtils.formatRupiah(summary.totalWasherShare)}* (50.0%)\n")
        sb.append("• Laba Bersih Kas PT/Owner: *${FormatUtils.formatRupiah(summary.totalOwnerShare)}* (${String.format(Locale.getDefault(), "%.0f", ownerMargin)}%)\n\n")

        if (breakdowns.isNotEmpty()) {
            sb.append("👥 *II. PERTANGGUNGJAWABAN HAK KOMISI OPERATOR*\n")
            breakdowns.forEachIndexed { idx, item ->
                sb.append("${idx + 1}. *${item.washerName}*: ${item.motorCount} unit • Hak: *${FormatUtils.formatRupiah(item.totalShare)}*\n")
            }
            sb.append("\n")
        }

        sb.append("📌 *III. PENGESAHAN DOKUMEN SISTEM*\n")
        sb.append("• Dibuat Oleh   : ${profile.cashierName}\n")
        sb.append("• Diperiksa     : ${profile.financeManagerName}\n")
        sb.append("• Disahkan Oleh : ${profile.directorName}\n")
        sb.append("• Status Sistem : TERVERIFIKASI SISTEM PEMBUKUAN OTOMATIS\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Demikian laporan resmi ini diterbitkan secara sah.\n")
        sb.append("🏛️ *${profile.companyName}*")
        return sb.toString()
    }

    fun buildRevenueAnalysisShareText(): String {
        val period = _selectedPeriod.value
        val analysis = revenueAnalysis.value
        val profile = _companyProfile.value
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val cal = Calendar.getInstance()
        val docNo = generateDocumentNumber("ANL-RISK", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        val sb = StringBuilder()
        sb.append("🏢 *${profile.companyName.uppercase()}*\n")
        sb.append("*LAPORAN ANALISIS KINERJA & ARUS KAS KORPORAT*\n")
        sb.append("No. Dok: $docNo | Sifat: Dokumen Manajemen Eksekutif\n")
        sb.append("Periode: ${period.title} ($currentDateStr)\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("📊 *I. INDIKATOR PERFORMA BISNIS (KPI)*\n")
        sb.append("• Total Armada Dikerjakan: *${analysis.totalMotors} Unit*\n")
        sb.append("• Pendapatan Bruto (Gross): *${FormatUtils.formatRupiah(analysis.totalGrossRevenue)}*\n")
        sb.append("• Beban Jasa Petugas Cuci: *${FormatUtils.formatRupiah(analysis.totalWasherShare)}*\n")
        sb.append("• Laba Bersih Kas Pemilik: *${FormatUtils.formatRupiah(analysis.totalOwnerShare)}* (${String.format(Locale.getDefault(), "%.0f", analysis.ownerMarginPercent)}% Margin)\n\n")

        sb.append("📈 *II. RATA-RATA & PROYEKSI FINANSIAL*\n")
        sb.append("• Throughput Rata-rata   : ${String.format(Locale.getDefault(), "%.1f", analysis.averageDailyMotors)} motor/hari\n")
        sb.append("• Rata-rata Laba Bersih  : ${FormatUtils.formatRupiah(analysis.averageDailyOwnerNet)}/hari\n")
        sb.append("• Proyeksi Kas Bulan Ini : ${FormatUtils.formatRupiah(analysis.projectedMonthlyOwnerNet)}\n")
        sb.append("• Jam Operasional Puncak : ${analysis.peakTimeSlot}\n\n")

        if (analysis.paymentMethods.isNotEmpty()) {
            sb.append("💳 *III. REKONSILIASI KANAL PEMBAYARAN*\n")
            for (pm in analysis.paymentMethods) {
                sb.append("• ${pm.method}: ${pm.motorCount} unit (${String.format(Locale.getDefault(), "%.0f", pm.percentage)}%)\n")
            }
            sb.append("\n")
        }

        if (analysis.insights.isNotEmpty()) {
            sb.append("💡 *IV. CATATAN AUDIT & INSIGHT DIREKSI*\n")
            for (ins in analysis.insights) {
                sb.append("• $ins\n")
            }
            sb.append("\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Disahkan oleh *${profile.directorName}* • ${profile.companyName}")
        return sb.toString()
    }

    fun buildDreamGoalShareText(): String {
        val progress = dreamGoalProgress.value
        val profile = _companyProfile.value
        val percent = String.format(Locale.getDefault(), "%.1f%%", progress.progressPercent * 100f)
        val sb = StringBuilder()
        sb.append("🎯 *${profile.companyName.uppercase()}*\n")
        sb.append("*RENCANA ALOKASI INVESTASI & DANA CADANGAN USAHA*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📌 *Target Belanja Modal:* ${progress.config.itemName}\n")
        sb.append("💰 *Anggaran Biaya:* ${FormatUtils.formatRupiah(progress.config.targetAmount)}\n")
        sb.append("📈 *Dana Terakumulasi:* ${FormatUtils.formatRupiah(progress.totalSaved)} ($percent)\n")
        sb.append("⚡ *Realisasi Hari Ini:* +${FormatUtils.formatRupiah(progress.todayRevenue)}\n")
        sb.append("⏳ *Sisa Kebutuhan Dana:* ${if (progress.remainingAmount <= 0) "TERPENUHI LUNAS! 🎉" else FormatUtils.formatRupiah(progress.remainingAmount)}\n")
        sb.append("🗓️ *Estimasi Waktu:* ${if (progress.isAchieved) "Target Investasi Tercapai! ✨" else "~${progress.estimatedDaysRemaining} Hari Operasional"}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💡 _Basis Alokasi Kas: ${progress.config.savingSource.label}_\n")
        sb.append("📱 _Dicatat oleh Sistem Terpadu ${profile.companyName}_")
        return sb.toString()
    }

    fun buildMonthlyReportWhatsAppText(summary: MonthlySummaryData = monthlySummaryData.value): String {
        return buildCorporateWhatsAppReport(summary)
    }

    fun buildCorporateWhatsAppReport(
        summary: MonthlySummaryData = monthlySummaryData.value,
        profile: CompanyProfile = _companyProfile.value
    ): String {
        val docNo = generateDocumentNumber("FIN-OPS", summary.month + 1, summary.year)
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val ownerMargin = if (summary.totalGrossRevenue > 0) {
            (summary.totalOwnerShare.toFloat() / summary.totalGrossRevenue.toFloat()) * 100f
        } else 0f

        val sb = StringBuilder()
        sb.append("🏢 *${profile.companyName.uppercase()}*\n")
        sb.append("*${profile.divisionName}*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📑 *MEMORANDUM LAPORAN KEUANGAN RESMI (AUDITED)*\n")
        sb.append("No. Dokumen : $docNo\n")
        sb.append("Sifat       : RAHASIA & TERBATAS (CONFIDENTIAL)\n")
        sb.append("Periode     : ${summary.monthName.uppercase()} ${summary.year}\n")
        sb.append("Tanggal     : $currentDateStr\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("Kepada Yth,\n")
        sb.append("*Dewan Direksi & Manajemen ${profile.companyName}*\n\n")
        sb.append("Dengan hormat,\n")
        sb.append("Bersama ini kami sampaikan Laporan Pertanggungjawaban Finansial dan Kinerja Operasional Divisi Perawatan Kendaraan untuk periode ${summary.monthName} ${summary.year} yang telah diverifikasi secara akuntabel:\n\n")

        sb.append("📊 *I. RINGKASAN EKSEKUTIF (EXECUTIVE SUMMARY)*\n")
        sb.append("• Total Armada Dilayani  : *${summary.totalMotors} Unit*\n")
        sb.append("• Pendapatan Kotor (Gross): *${FormatUtils.formatRupiah(summary.totalGrossRevenue)}*\n")
        sb.append("• Beban Jasa Tenaga Cuci : *${FormatUtils.formatRupiah(summary.totalWasherShare)}* (50.0%)\n")
        sb.append("• Laba Bersih Kas PT/Owner: *${FormatUtils.formatRupiah(summary.totalOwnerShare)}* (${String.format(Locale.getDefault(), "%.0f", ownerMargin)}%)\n")
        sb.append("• Throughput Rata-rata   : ${String.format(Locale.getDefault(), "%.1f", summary.averageDailyMotors)} unit/hari aktif\n")
        sb.append("• Hari Kerja Operasional : ${summary.activeDaysCount} dari ${summary.daysInMonth} hari\n")
        if (summary.peakDay != null) {
            sb.append("• 🏆 Rekor Hari Teramai  : ${summary.peakDay.dayName}, ${summary.peakDay.dayOfMonth} ${summary.monthName} (${summary.peakDay.motorCount} unit - ${FormatUtils.formatRupiah(summary.peakDay.grossRevenue)})\n")
        }
        sb.append("\n")

        sb.append("💳 *II. SALURAN PENERIMAAN KAS & BANK*\n")
        val grossFloat = if (summary.totalGrossRevenue > 0) summary.totalGrossRevenue.toFloat() else 1f
        val cashPct = (summary.cashAmount.toFloat() / grossFloat) * 100f
        val qrisPct = (summary.qrisAmount.toFloat() / grossFloat) * 100f
        sb.append("• Kas Tunai (Cash on Hand) : ${FormatUtils.formatRupiah(summary.cashAmount)} (${String.format(Locale.getDefault(), "%.1f", cashPct)}%)\n")
        sb.append("• QRIS Merchant Settlement : ${FormatUtils.formatRupiah(summary.qrisAmount)} (${String.format(Locale.getDefault(), "%.1f", qrisPct)}%)\n")
        if (summary.transferAmount > 0) {
            val trfPct = (summary.transferAmount.toFloat() / grossFloat) * 100f
            sb.append("• Transfer Rekening Bank   : ${FormatUtils.formatRupiah(summary.transferAmount)} (${String.format(Locale.getDefault(), "%.1f", trfPct)}%)\n")
        }
        sb.append("\n")

        if (summary.washerStats.isNotEmpty()) {
            sb.append("👥 *III. ALOKASI BEBAN HONORARIUM OPERATOR*\n")
            summary.washerStats.forEachIndexed { idx, w ->
                sb.append("${idx + 1}. *${w.washerName}*: ${w.motorCount} unit • Hak: *${FormatUtils.formatRupiah(w.totalShare)}* (${String.format(Locale.getDefault(), "%.0f", w.percentage)}%)\n")
            }
            sb.append("\n")
        }

        sb.append("📌 *IV. LEMBAR PENGESAHAN & OTORISASI PERUSAHAAN*\n")
        sb.append("• Dibuat Oleh   : ${profile.cashierName}\n")
        sb.append("• Diperiksa     : ${profile.financeManagerName}\n")
        sb.append("• Disahkan Oleh : ${profile.directorName}\n")
        sb.append("• Status Sistem : TERVERIFIKASI DIGITAL & TERAKREDITASI SISTEM\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Demikian laporan resmi ini diterbitkan secara sah dan akuntabel.\n")
        sb.append("🏛️ *${profile.companyName}*")
        return sb.toString()
    }

    fun buildMonthlyCsvContent(summary: MonthlySummaryData = monthlySummaryData.value): String {
        return buildCorporateMonthlyCsv(summary)
    }

    fun buildCorporateMonthlyCsv(
        summary: MonthlySummaryData = monthlySummaryData.value,
        profile: CompanyProfile = _companyProfile.value
    ): String {
        val docNo = generateDocumentNumber("FIN-OPS", summary.month + 1, summary.year)
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val sb = StringBuilder()

        // Header KOP SURAT RESMI PERUSAHAAN
        sb.append("\"====================================================================================================\"\n")
        sb.append("\"${profile.companyName.uppercase()} - ${profile.divisionName}\"\n")
        sb.append("\"LAPORAN KEUANGAN OPERASIONAL & PERTANGGUNGJAWABAN KAS RESMI (CORPORATE FINANCIAL STATEMENT)\"\n")
        sb.append("\"Izin Usaha / NIB: ${profile.legalRegNo} | Alamat: ${profile.companyAddress}\"\n")
        sb.append("\"No. Dokumen: $docNo\",\"Sifat: RAHASIA & TERBATAS (CONFIDENTIAL)\",\"Periode: ${summary.monthName} ${summary.year}\"\n")
        sb.append("\"Tanggal Terbit: $currentDateStr\",\"Status Audit: TERVERIFIKASI SISTEM PEMBUKUAN OTOMATIS\",\"Mata Uang: IDR (Rupiah)\"\n")
        sb.append("\"====================================================================================================\"\n\n")

        // SECTION 1: RINGKASAN EKSEKUTIF FINANSIAL
        sb.append("\"--- 1. RINGKASAN EKSEKUTIF FINANSIAL (EXECUTIVE SUMMARY) ---\"\n")
        sb.append("Indikator Finansial,Nilai Akumulasi,Satuan,Keterangan\n")
        sb.append("Total Volume Armada,${summary.totalMotors},Unit,Volume jasa cuci terselesaikan\n")
        sb.append("Total Pendapatan Bruto (Gross),${summary.totalGrossRevenue},Rupiah,Penerimaan tarif Rp 10.000/motor\n")
        sb.append("Beban Jasa Operator Cuci,${summary.totalWasherShare},Rupiah,Alokasi bagi hasil 50% (Rp 5.000/motor)\n")
        sb.append("Laba Bersih Kas Pemilik / PT,${summary.totalOwnerShare},Rupiah,Sisa hasil usaha bersih 50%\n")
        sb.append("Rata-rata Volume Harian,${String.format(Locale.getDefault(), "%.1f", summary.averageDailyMotors)},Unit/Hari,Dihitung dari hari aktif operasional\n")
        sb.append("Hari Buka Operasional,${summary.activeDaysCount},Hari,Total hari kerja dalam periode\n\n")

        // SECTION 2: SALURAN KAS & PEMBAYARAN
        sb.append("\"--- 2. REKAPITULASI PENERIMAAN KAS & REKENING (CASH FLOW RECONCILIATION) ---\"\n")
        sb.append("Saluran Pembayaran,Nominal (Rp),Proporsi (%),Status Rekonsiliasi\n")
        val grossFloat = if (summary.totalGrossRevenue > 0) summary.totalGrossRevenue.toFloat() else 1f
        val cashPct = (summary.cashAmount.toFloat() / grossFloat) * 100f
        val qrisPct = (summary.qrisAmount.toFloat() / grossFloat) * 100f
        val trfPct = (summary.transferAmount.toFloat() / grossFloat) * 100f
        sb.append("Kas Tunai (Cash on Hand),${summary.cashAmount},${String.format(Locale.getDefault(), "%.1f", cashPct)}%,Fisik Kas Kasir Terverifikasi\n")
        sb.append("QRIS Merchant Terintegrasi,${summary.qrisAmount},${String.format(Locale.getDefault(), "%.1f", qrisPct)}%,Settlement Rekening Penampung\n")
        if (summary.transferAmount > 0) {
            sb.append("Transfer Bank Non-Tunai,${summary.transferAmount},${String.format(Locale.getDefault(), "%.1f", trfPct)}%,Mutasi Rekening Bank Valid\n")
        }
        sb.append("\n")

        // SECTION 3: REKAPITULASI PEMBUKUAN ARUS KAS HARIAN
        sb.append("\"--- 3. BUKU BESAR PEMBUKUAN ARUS KAS HARIAN ---\"\n")
        sb.append("No,Tanggal,Hari,Jumlah Armada,Omset Bruto (Rp),Beban Operator (Rp),Kas Bersih PT (Rp),Operator Bertugas\n")
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        summary.dailyBreakdown.forEachIndexed { idx, d ->
            val dateStr = sdfDate.format(Date(d.dateMillis))
            val cleanWasher = d.topWasherName.replace(",", ";").replace("\"", "")
            sb.append("${idx + 1},$dateStr,${d.dayName},${d.motorCount},${d.grossRevenue},${d.washerShare},${d.ownerShare},\"$cleanWasher\"\n")
        }
        sb.append("TOTAL AKUMULASI,,,\"${summary.totalMotors}\",\"${summary.totalGrossRevenue}\",\"${summary.totalWasherShare}\",\"${summary.totalOwnerShare}\",\"Semua Petugas\"\n\n")

        // SECTION 4: REKAPITULASI TENAGA KERJA
        if (summary.washerStats.isNotEmpty()) {
            sb.append("\"--- 4. PERTANGGUNGJAWABAN HAK KOMISI & HONORARIUM OPERATOR ---\"\n")
            sb.append("No,Nama Operator Cuci,Volume Unit,Kontribusi (%),Hak Bagi Hasil (Rp),Status Hak\n")
            summary.washerStats.forEachIndexed { idx, w ->
                val cleanName = w.washerName.replace(",", " ").replace("\"", "")
                sb.append("${idx + 1},\"$cleanName\",${w.motorCount},${String.format(Locale.getDefault(), "%.1f", w.percentage)}%,${w.totalShare},Telah Direkonsiliasi\n")
            }
            sb.append("\n")
        }

        // SECTION 5: PENGESAHAN DOKUMEN RESMI PT
        sb.append("\"--- 5. LEMBAR PENGESAHAN OTORISASI PERUSAHAAN ---\"\n")
        sb.append("\"Dibuat Oleh: ${profile.cashierName}\",\"Diperiksa Oleh: ${profile.financeManagerName}\",\"Disahkan Oleh: ${profile.directorName}\"\n")
        sb.append("\"Status: Terverifikasi Digital\",\"Status: Telah Diaudit Akuntansi\",\"Status: Sah & Mengikat Direksi\"\n")
        sb.append("\"Dokumen ini diterbitkan secara otomatis oleh Sistem Pembukuan Terintegrasi ${profile.companyName}\"\n")

        return sb.toString()
    }

    fun buildDetailedCsvContent(records: List<WashRecord> = filteredRecords.value): String {
        return buildCorporateDetailedCsv(records)
    }

    fun buildCorporateDetailedCsv(
        records: List<WashRecord> = filteredRecords.value,
        profile: CompanyProfile = _companyProfile.value
    ): String {
        val cal = Calendar.getInstance()
        val docNo = generateDocumentNumber("LEDGER-DTL", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val sb = StringBuilder()

        sb.append("\"====================================================================================================\"\n")
        sb.append("\"${profile.companyName.uppercase()} - ${profile.divisionName}\"\n")
        sb.append("\"BUKU BESAR RINCIAN TRANSAKSI PER UNIT ARMADA KENDARAAN (DETAILED TRANSACTION LEDGER)\"\n")
        sb.append("\"Izin Usaha / NIB: ${profile.legalRegNo} | Alamat: ${profile.companyAddress}\"\n")
        sb.append("\"No. Dokumen: $docNo\",\"Sifat: RAHASIA (AUDIT TRAIL)\",\"Tanggal Terbit: $currentDateStr\"\n")
        sb.append("\"Total Catatan: ${records.size} Rekaman\",\"Mata Uang: IDR (Rupiah)\",\"Status: Terverifikasi Sistem\"\n")
        sb.append("\"====================================================================================================\"\n\n")

        sb.append("ID,Waktu Lengkap,Tanggal,Jam,Jumlah Motor,Plat Nomor,Tipe Motor,Petugas Cuci,Tarif Per Motor,Bagi Hasil Per Motor,Total Omset Kotor,Bagi Hasil Petugas,Kas Bersih Pemilik,Metode Pembayaran,Catatan Operasional\n")
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (r in records) {
            val d = Date(r.timestamp)
            val fullTime = sdfFull.format(d)
            val dateStr = sdfDate.format(d)
            val timeStr = sdfTime.format(d)
            val cleanPlate = r.licensePlate.replace(",", " ").replace("\"", "")
            val cleanWasher = r.washerName.replace(",", " ").replace("\"", "")
            val cleanNote = r.note.replace(",", ";").replace("\n", " ").replace("\"", "")
            sb.append("${r.id},$fullTime,$dateStr,$timeStr,${r.motorCount},\"$cleanPlate\",${r.motorType},\"$cleanWasher\",${r.pricePerMotor},${r.washerSharePerMotor},${r.totalPrice},${r.totalWasherShare},${r.totalOwnerShare},${r.paymentMethod},\"$cleanNote\"\n")
        }

        val totalGross = records.sumOf { it.totalPrice }
        val totalWasher = records.sumOf { it.totalWasherShare }
        val totalOwner = records.sumOf { it.totalOwnerShare }
        val totalMotors = records.sumOf { it.motorCount.toLong() }
        sb.append("TOTAL AKUMULASI,,,,$totalMotors,,,,,\"Total:\",$totalGross,$totalWasher,$totalOwner,,\n\n")

        sb.append("\"--- LEMBAR PENGESAHAN DIREKSI & AUDITOR ---\"\n")
        sb.append("\"Dibuat Oleh: ${profile.cashierName}\",\"Diperiksa Oleh: ${profile.financeManagerName}\",\"Disahkan Oleh: ${profile.directorName}\"\n")
        sb.append("\"Status: Sah & Mengikat\",\"Dokumen Sistem Terintegrasi ${profile.companyName}\",\"\"\n")

        return sb.toString()
    }

    /**
     * Generates an official corporate HTML document for Monthly Recap
     */
    fun buildCorporateMonthlyHtml(summary: MonthlySummaryData = monthlySummaryData.value): String {
        val docNo = generateDocumentNumber("FIN-OPS", summary.month + 1, summary.year)
        return CorporateReportGenerator.generateMonthlyCorporateHtml(
            summary = summary,
            profile = _companyProfile.value,
            documentNumber = docNo
        )
    }

    /**
     * Generates an official corporate HTML document for detailed transaction list
     */
    fun buildCorporateDetailedHtml(
        records: List<WashRecord> = filteredRecords.value,
        periodTitle: String = _selectedPeriod.value.title
    ): String {
        val cal = Calendar.getInstance()
        val docNo = generateDocumentNumber("LEDGER-DTL", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        return CorporateReportGenerator.generateDetailedCorporateHtml(
            records = records,
            profile = _companyProfile.value,
            periodTitle = periodTitle,
            documentNumber = docNo
        )
    }

    fun getAllRecordsList(): List<WashRecord> = allRecords.value
}
