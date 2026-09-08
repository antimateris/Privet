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
import kotlinx.coroutines.Job
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

// ... (rest of the original data classes and enums remain unchanged) ...

// For brevity the file keeps the previously defined data classes and helper types unchanged above.

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

    private val _maintenanceStatus = MutableStateFlow(MaintenanceStatusData())
    val maintenanceStatus: StateFlow<MaintenanceStatusData> = _maintenanceStatus.asStateFlow()

    private val userPrefs by lazy {
        getApplication<Application>().getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    }

    // Map of pending delete jobs so they can be cancelled if the user taps Undo
    private val pendingDeleteJobs = mutableMapOf<Long, Job>()

    init {
        // original init logic (migration, listeners, etc.) kept unchanged
        migrateLegacyRoleKeys()
    }

    // ... many existing methods unchanged ...

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

    // -- NEW: Delete with Undo support for WashRecord transactions --

    /**
     * Deletes a record locally immediately and schedules the remote (cloud) deletion after
     * [undoTimeoutMillis]. If the user selects Undo before the timeout, call [restoreRecord].
     */
    fun deleteRecordWithUndo(
        record: WashRecord,
        undoTimeoutMillis: Long = 5000L,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                // Remove locally so UI updates instantly
                repository.deleteRecord(record)
            } catch (e: Exception) {
                onComplete(false, "Gagal menghapus lokal: ${e.message}")
                return@launch
            }

            // Schedule remote deletion after timeout
            val job = viewModelScope.launch(Dispatchers.IO) {
                try {
                    delay(undoTimeoutMillis)
                    val res = firestoreRepository.deleteTransaction(record)
                    if (res.isSuccess) {
                        onComplete(true, "Transaksi dihapus")
                    } else {
                        onComplete(false, res.exceptionOrNull()?.message ?: "Gagal menghapus di cloud")
                    }
                } catch (e: Exception) {
                    onComplete(false, "Gagal menghapus di cloud: ${e.message}")
                } finally {
                    // clean up pending job
                    pendingDeleteJobs.remove(record.timestamp)
                }
            }
            // store job so Undo can cancel it
            pendingDeleteJobs[record.timestamp] = job
            // Inform caller that local delete succeeded and cloud pending
            onComplete(true, "Transaksi dihapus (Undo dalam ${undoTimeoutMillis / 1000}s)")
        }
    }

    /**
     * Restores a locally deleted record and cancels the pending remote deletion if it exists.
     */
    fun restoreRecord(
        record: WashRecord,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            // Cancel any scheduled remote deletion
            pendingDeleteJobs.remove(record.timestamp)?.cancel()

            try {
                // Re-insert locally
                repository.insertRecord(record)
                // Also re-upload to cloud asynchronously (best-effort)
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestoreRepository.saveTransaction(record)
                    } catch (_: Exception) {
                        // swallow: local restore is authoritative for immediate UX
                    }
                }
                onComplete(true, "Penghapusan dibatalkan")
            } catch (e: Exception) {
                onComplete(false, "Gagal memulihkan record: ${e.message}")
            }
        }
    }

    // ... rest of the existing ViewModel remains unchanged; the file keeps its previous listeners and logic ...
}
