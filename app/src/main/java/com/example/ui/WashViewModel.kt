package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.AppUpdateInfo
import com.example.data.model.StoreExpense
import com.example.data.model.UserPresence
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.example.data.repository.FirestoreWashRepository
import com.example.data.repository.MaintenanceStatusData
import com.example.data.repository.WashRepository
import com.example.util.CorporateReportGenerator
import com.example.util.FormatUtils
import com.example.util.NotificationHelper
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

// -- trimmed rest of the file above for brevity; we only modify the section near firestoreRepository --

package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.AppUpdateInfo
import com.example.data.model.StoreExpense
import com.example.data.model.UserPresence
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.example.data.repository.FirestoreWashRepository
import com.example.data.repository.MaintenanceStatusData
import com.example.data.repository.WashRepository
import com.example.util.CorporateReportGenerator
import com.example.util.FormatUtils
import com.example.util.NotificationHelper
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

    // Temporary cache to support undoing deletions (timestamp -> record)
    private val recentlyDeletedRecords = mutableMapOf<Long, WashRecord>()

    private val _cloudSyncStatus = MutableStateFlow("Data tersimpan di HP (Offline)")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    private val _maintenanceStatus = MutableStateFlow(MaintenanceStatusData())
    val maintenanceStatus: StateFlow<MaintenanceStatusData> = _maintenanceStatus.asStateFlow()

    private val userPrefs by lazy {
        getApplication<Application>().getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    }

    init {
        // Must run before anything else reads "user_role" / "account_name_*" /
        // "account_pass_*" so people updating from the old 3-role version don't
        // get bumped back to Kasir or lose their saved passwords.
        migrateLegacyRoleKeys()
    }

    // ... rest of the original file unchanged ...

    fun deleteStoreExpense(
        expense: StoreExpense,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            viewModelScope.launch(Dispatchers.IO) {
                firestoreRepository.deleteExpenseRemote(expense.timestamp)
            }
            onComplete(true, "Pengeluaran toko berhasil dihapus!")
        }
    }

    /**
     * Deletes a WashRecord locally and stores a copy for potential undo.
     * The onComplete callback receives (success, message) which the UI can show in a Snackbar.
     */
    fun deleteRecordWithUndo(record: WashRecord, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                // Cache the record so it can be restored if user taps Undo
                recentlyDeletedRecords[record.timestamp] = record

                // Delete from local DB immediately for responsive UI
                repository.deleteRecord(record)

                // Fire-and-forget: try to delete from cloud as well (best-effort)
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestoreRepository.deleteTransaction(record)
                    } catch (_: Exception) {
                        // ignore cloud failures — local deletion already succeeded
                    }
                }

                onComplete(true, "Transaksi dihapus")
            } catch (e: Exception) {
                recentlyDeletedRecords.remove(record.timestamp)
                onComplete(false, "Gagal menghapus transaksi: ${e.message}")
            }
        }
    }

    /**
     * Restores a previously deleted record (if available in cache).
     */
    fun restoreRecord(record: WashRecord, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                repository.insertRecord(record)
                recentlyDeletedRecords.remove(record.timestamp)

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestoreRepository.saveTransaction(record)
                    } catch (_: Exception) {
                        // ignore
                    }
                }

                onComplete(true, "Transaksi dikembalikan")
            } catch (e: Exception) {
                onComplete(false, "Gagal mengembalikan transaksi: ${e.message}")
            }
        }
    }
