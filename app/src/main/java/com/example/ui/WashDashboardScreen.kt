package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.example.ui.components.DreamGoalSavingsCard
import com.example.ui.components.DreamSavingsScreen
import com.example.ui.components.MonthlyRecapScreen
import com.example.ui.components.RevenueAnalysisSection
import com.example.ui.components.SummaryCardsSection
import com.example.ui.components.TransactionItemCard
import com.example.ui.components.WasherBreakdownCard
import com.example.ui.dialogs.AddEditWashDialog
import com.example.ui.dialogs.CalendarRevenueDialog
import com.example.ui.dialogs.DisputeTransactionDialog
import com.example.ui.dialogs.EditDreamGoalDialog
import com.example.ui.dialogs.EditPastDateRevenueDialog
import com.example.ui.dialogs.ExportReportDialog
import com.example.ui.dialogs.ManageWorkersDialog
import com.example.ui.dialogs.PostSaveReceiptOptionDialog
import com.example.ui.dialogs.SwitchUserDialog
import com.example.ui.dialogs.ThermalReceiptDialog
import com.example.util.ExportUtils
import com.example.util.FormatUtils
import com.example.util.TimePeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WashDashboardScreen(
    viewModel: WashViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedQuickWasher by viewModel.selectedQuickWasher.collectAsStateWithLifecycle()
    val activeWorkers by viewModel.activeWorkers.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val washerBreakdowns by viewModel.washerBreakdowns.collectAsStateWithLifecycle()
    val revenueAnalysis by viewModel.revenueAnalysis.collectAsStateWithLifecycle()
    val dreamGoalProgress by viewModel.dreamGoalProgress.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val dailySummariesMap by viewModel.dailySummariesMap.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummaryData.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<WashRecord?>(null) }
    var showWorkersDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<WashRecord?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showEditDreamGoalDialog by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showEditPastRevenueDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }
    var recordForThermalReceipt by remember { mutableStateOf<WashRecord?>(null) }
    var savedRecordForReceiptOption by remember { mutableStateOf<WashRecord?>(null) }
    var recordForDispute by remember { mutableStateOf<WashRecord?>(null) }
    var targetPastDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var targetPastSummary by remember { mutableStateOf<DayRevenueSummary?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (currentTab) {
                                    0 -> Icons.Default.DirectionsBike
                                    1 -> Icons.Default.CalendarMonth
                                    2 -> Icons.Default.BarChart
                                    else -> Icons.Default.Savings
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentTab) {
                                    0 -> "Steam Motor"
                                    1 -> "Rekap Bulanan"
                                    2 -> "Analisis Pendapatan"
                                    else -> "Barang Impian"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = when (currentTab) {
                                0 -> "1 Motor 10rb • Bagi Hasil 5rb"
                                1 -> "${monthlySummary.monthName} ${monthlySummary.year} • Ekspor CSV & WA"
                                2 -> "Performa Finansial & Proyeksi Usaha"
                                else -> "Target & Progres Tabungan Omset"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Ekspor Laporan Dialog Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("appbar_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Ekspor Laporan (CSV / WA)"
                        )
                    }

                    // Calendar & Rekap Button
                    IconButton(
                        onClick = { showCalendarDialog = true },
                        modifier = Modifier.testTag("appbar_calendar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Kalender & Rekap Omset"
                        )
                    }

                    // WhatsApp Share
                    IconButton(
                        onClick = {
                            val text = when (currentTab) {
                                0 -> viewModel.buildWhatsAppReportText()
                                1 -> viewModel.buildMonthlyReportWhatsAppText(monthlySummary)
                                2 -> viewModel.buildRevenueAnalysisShareText()
                                else -> viewModel.buildDreamGoalShareText()
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Informasi Steam Motor")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.testTag("share_whatsapp_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan Laporan")
                    }

                    // Team/Workers Dialog
                    IconButton(
                        onClick = { showWorkersDialog = true },
                        modifier = Modifier.testTag("manage_workers_button")
                    ) {
                        Icon(Icons.Default.Group, contentDescription = "Kelola Petugas")
                    }

                    // User Profile / Role Switcher
                    IconButton(
                        onClick = { showSwitchUserDialog = true },
                        modifier = Modifier.testTag("appbar_user_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Ganti Akun",
                            tint = when (currentUser.role) {
                                UserRole.KASIR -> Color(0xFF0284C7)
                                UserRole.MANAGER_KEUANGAN -> Color(0xFFD97706)
                                UserRole.PEMILIK -> Color(0xFF16A34A)
                            }
                        )
                    }

                    // More Menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ganti Akun (${currentUser.name} - ${currentUser.role.title})") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                showSwitchUserDialog = true
                            },
                            modifier = Modifier.testTag("menu_switch_user")
                        )

                        DropdownMenuItem(
                            text = { Text("Laporan Resmi PT (PDF / CSV)") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF0F172A)) },
                            onClick = {
                                showMenu = false
                                showExportDialog = true
                            },
                            modifier = Modifier.testTag("menu_export_corporate")
                        )

                        DropdownMenuItem(
                            text = { Text("Sinkronkan ke Cloud (Firebase)") },
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF16A34A)) },
                            onClick = {
                                showMenu = false
                                viewModel.syncAllLocalToCloud { _, msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("menu_sync_cloud")
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (currentTab) {
                                        0 -> "Salin Teks Laporan Kasir"
                                        1 -> "Salin Rekap Bulanan"
                                        2 -> "Salin Analisis Pendapatan"
                                        else -> "Salin Progres Barang Impian"
                                    }
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val textToCopy = when (currentTab) {
                                    0 -> viewModel.buildWhatsAppReportText()
                                    1 -> viewModel.buildMonthlyReportWhatsAppText(monthlySummary)
                                    2 -> viewModel.buildRevenueAnalysisShareText()
                                    else -> viewModel.buildDreamGoalShareText()
                                }
                                val clip = ClipData.newPlainText("Laporan Steam Motor", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Teks berhasil disalin ke clipboard!")
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Hapus Semua Riwayat", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showClearAllConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Kasir") },
                    modifier = Modifier.testTag("nav_cashier_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Bulanan") },
                    modifier = Modifier.testTag("nav_monthly_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Analisis") },
                    modifier = Modifier.testTag("nav_analysis_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                    label = { Text("Impian") },
                    modifier = Modifier.testTag("nav_dream_goal_tab")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingRecord = null
                        showAddEditDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Catat Lengkap") },
                    modifier = Modifier.testTag("add_custom_wash_fab")
                )
            }
        }
    ) { innerPadding ->
        if (currentTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Cloud Sync Status Banner (Real-time Firebase)
                item {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cloud_sync_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cloudSyncStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    viewModel.syncAllLocalToCloud { _, msg ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("cloud_sync_now_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = Color(0xFF166534),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Sinkronkan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Active User Account Status
                item {
                    Surface(
                        color = when (currentUser.role) {
                            UserRole.KASIR -> Color(0xFFF0F9FF)
                            UserRole.MANAGER_KEUANGAN -> Color(0xFFFFFBEB)
                            UserRole.PEMILIK -> Color(0xFFF0FDF4)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            when (currentUser.role) {
                                UserRole.KASIR -> Color(0xFFBAE6FD)
                                UserRole.MANAGER_KEUANGAN -> Color(0xFFFDE68A)
                                UserRole.PEMILIK -> Color(0xFFBBF7D0)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_user_status_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = when (currentUser.role) {
                                        UserRole.KASIR -> Color(0xFF0284C7)
                                        UserRole.MANAGER_KEUANGAN -> Color(0xFFD97706)
                                        UserRole.PEMILIK -> Color(0xFF16A34A)
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${currentUser.name} (${currentUser.role.title})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentUser.role.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showSwitchUserDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("switch_user_button")
                            ) {
                                Text("Ganti", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Disputed Alert Banner (if any disputed transactions exist)
                if (financialSummary.disputedMotors > 0) {
                    item {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("disputed_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Perhatian: ${financialSummary.disputedMotors} Transaksi Disanggah Manager",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Total Rp ${FormatUtils.formatRupiah(financialSummary.disputedAmount)} belum sah dan memerlukan peninjauan kembali.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Live Device Date & Past Date Status Card
                item {
                    val isPastDate = selectedPeriod == TimePeriod.TANGGAL_PILIHAN && !FormatUtils.isToday(selectedDateMillis)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPastDate) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPastDate) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPastDate) Color(0xFFFDE68A) else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPastDate) Icons.Default.CalendarToday else Icons.Default.Today,
                                            contentDescription = null,
                                            tint = if (isPastDate) Color(0xFFB45309) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isPastDate)
                                                "Sedang Menampilkan Tanggal Terlewati:"
                                            else
                                                "Tanggal Hari Ini (Sesuai HP):",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPastDate) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isPastDate)
                                                FormatUtils.formatDateFull(selectedDateMillis)
                                            else
                                                FormatUtils.formatDateFull(System.currentTimeMillis()),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPastDate) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showCalendarDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("open_calendar_dashboard_button")
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kalender", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isPastDate) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val key = sdf.format(Date(selectedDateMillis))
                                            targetPastDateMillis = selectedDateMillis
                                            targetPastSummary = dailySummariesMap[key]
                                            showEditPastRevenueDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("edit_past_date_banner_button"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Pemasukan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.selectToday() },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("back_to_today_button"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 0: Banner to switch to revenue analysis
                item {
                    Card(
                        onClick = { currentTab = 1 },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_to_analysis")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Analisis Pendapatan Usaha",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Grafik omset, jam ramai cuci & proyeksi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "Buka →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Section 1: Quick Action Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚡ Cuci Cepat (+1 Motor)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "10rb | Bagi 5rb",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Petugas Selector Chips for Quick Add
                        Text(
                            text = if (selectedQuickWasher.isBlank()) "Pilih Petugas (Opsional):" else "Petugas Terpilih: $selectedQuickWasher",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedQuickWasher.isBlank(),
                                onClick = { viewModel.selectQuickWasher("") },
                                label = { Text("Tanpa Petugas", fontSize = 12.sp) }
                            )

                            activeWorkers.forEach { worker ->
                                FilterChip(
                                    selected = selectedQuickWasher == worker.name,
                                    onClick = { viewModel.selectQuickWasher(worker.name) },
                                    label = { Text(worker.name, fontSize = 12.sp) }
                                )
                            }

                            OutlinedButton(
                                onClick = { showWorkersDialog = true },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("+ Petugas", fontSize = 11.sp)
                            }
                        }

                        // Big Quick Add Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isViewingPastDate = selectedPeriod == TimePeriod.TANGGAL_PILIHAN && !FormatUtils.isToday(selectedDateMillis)

                            Button(
                                onClick = {
                                    if (isViewingPastDate) {
                                        viewModel.quickAddMotorForDate(selectedDateMillis, 1)
                                    } else {
                                        viewModel.quickAddMotor(1)
                                    }
                                    coroutineScope.launch {
                                        val washerMsg = if (selectedQuickWasher.isNotBlank()) " oleh $selectedQuickWasher" else ""
                                        val dateMsg = if (isViewingPastDate) " di tanggal ${FormatUtils.formatDateMedium(selectedDateMillis)}" else ""
                                        snackbarHostState.showSnackbar("+1 Motor dicatat (Rp 10.000$washerMsg)$dateMsg")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("quick_add_1_motor_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+1 Motor (10rb)", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isViewingPastDate) {
                                        viewModel.quickAddMotorForDate(selectedDateMillis, 2)
                                    } else {
                                        viewModel.quickAddMotor(2)
                                    }
                                    coroutineScope.launch {
                                        val dateMsg = if (isViewingPastDate) " di tanggal ${FormatUtils.formatDateMedium(selectedDateMillis)}" else ""
                                        snackbarHostState.showSnackbar("+2 Motor dicatat (Rp 20.000)$dateMsg")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_add_2_motor_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+2 Motor", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 2: Time Period Filter Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimePeriod.values().forEach { period ->
                        val chipTitle = if (period == TimePeriod.TANGGAL_PILIHAN) {
                            if (selectedPeriod == TimePeriod.TANGGAL_PILIHAN) {
                                "📅 ${FormatUtils.formatDateMedium(selectedDateMillis)}"
                            } else {
                                "📅 Pilih Tanggal"
                            }
                        } else {
                            period.title
                        }

                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                if (period == TimePeriod.TANGGAL_PILIHAN) {
                                    showCalendarDialog = true
                                } else {
                                    viewModel.setPeriod(period)
                                }
                            },
                            label = {
                                Text(
                                    text = chipTitle,
                                    fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("period_chip_${period.name}")
                        )
                    }
                }
            }

            // Section 3: 4 KPI Summary Cards
            item {
                SummaryCardsSection(summary = financialSummary)
            }

            // Section 3.5: Tabungan Barang Impian (Visual Progress Bar)
            item {
                DreamGoalSavingsCard(
                    progress = dreamGoalProgress,
                    onEditGoalClick = { showEditDreamGoalDialog = true }
                )
            }

            // Section 4: Washer Breakdown (Commission Payouts)
            item {
                WasherBreakdownCard(breakdowns = washerBreakdowns)
            }

            // Section 5: Search & Transaction History Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Riwayat Cuci (${filteredRecords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = selectedPeriod.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari plat nomor atau nama petugas...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_records_input")
                    )
                }
            }

            // Section 6: Records List or Empty State
            if (filteredRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (searchQuery.isEmpty()) "Belum ada pencatatan cuci" else "Tidak ada transaksi yang cocok",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (searchQuery.isEmpty())
                                    "Tekan tombol '+1 Motor' di atas untuk mencatat motor pertama hari ini!"
                                else
                                    "Coba cari dengan kata kunci plat atau nama petugas lainnya.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            if (searchQuery.isEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.quickAddMotor(1) },
                                    modifier = Modifier.testTag("empty_state_add_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+1 Motor Sekarang")
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredRecords,
                    key = { it.id },
                    contentType = { "transaction_card" }
                ) { record ->
                    TransactionItemCard(
                        record = record,
                        currentUser = currentUser,
                        onEdit = {
                            editingRecord = it
                            showAddEditDialog = true
                        },
                        onDelete = {
                            recordToDelete = it
                        },
                        onPrintReceipt = {
                            recordForThermalReceipt = it
                        },
                        onDispute = {
                            recordForDispute = it
                        },
                        onValidate = {
                            viewModel.validateTransaction(it)
                            Toast.makeText(context, "Transaksi disahkan menjadi VALID", Toast.LENGTH_SHORT).show()
                        },
                        onRevokeDispute = {
                            viewModel.revokeDispute(it)
                            Toast.makeText(context, "Sanggahan dicabut", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    } else if (currentTab == 1) {
        MonthlyRecapScreen(
            viewModel = viewModel,
            monthlySummary = monthlySummary,
            onNavigateToDateInCashier = { dateMillis ->
                viewModel.setSelectedDate(dateMillis)
                currentTab = 0
            },
            onShowSnackbar = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            },
            modifier = Modifier.padding(innerPadding)
        )
    } else if (currentTab == 2) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RevenueAnalysisSection(
                    viewModel = viewModel,
                    analysis = revenueAnalysis,
                    washerBreakdowns = washerBreakdowns,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    onShowSnackbar = { coroutineScope.launch { snackbarHostState.showSnackbar(it) } }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    } else {
        DreamSavingsScreen(
            progress = dreamGoalProgress,
            onEditGoalClick = { showEditDreamGoalDialog = true },
            onSelectPreset = { newConfig ->
                viewModel.updateDreamGoalConfig(newConfig)
            },
            onShareClick = {
                val text = viewModel.buildDreamGoalShareText()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Progres Barang Impian")
                context.startActivity(shareIntent)
            },
            onShowSnackbar = { message ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

    // Dialog: Add / Edit Transaction
    if (showAddEditDialog) {
        AddEditWashDialog(
            initialRecord = editingRecord,
            activeWorkers = activeWorkers,
            preselectedWorker = selectedQuickWasher,
            defaultDateMillis = selectedDateMillis,
            onDismiss = {
                showAddEditDialog = false
                editingRecord = null
            },
            onSave = { id, motorCount, licensePlate, motorType, washerName, price, share, payment, note, timestamp, printImmediately ->
                val savedRecord = viewModel.addOrUpdateRecord(
                    id = id,
                    motorCount = motorCount,
                    licensePlate = licensePlate,
                    motorType = motorType,
                    washerName = washerName,
                    pricePerMotor = price,
                    washerSharePerMotor = share,
                    paymentMethod = payment,
                    note = note,
                    timestamp = timestamp,
                    existingRecord = editingRecord
                )
                showAddEditDialog = false
                editingRecord = null

                if (printImmediately) {
                    recordForThermalReceipt = savedRecord
                } else if (id == 0L) {
                    savedRecordForReceiptOption = savedRecord
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaksi berhasil diperbarui!")
                    }
                }
            }
        )
    }

    // Dialog: Opsi Cetak Struk Setelah Simpan Transaksi
    if (savedRecordForReceiptOption != null) {
        PostSaveReceiptOptionDialog(
            record = savedRecordForReceiptOption!!,
            onPrintReceipt = {
                val target = savedRecordForReceiptOption
                savedRecordForReceiptOption = null
                recordForThermalReceipt = target
            },
            onDismiss = {
                savedRecordForReceiptOption = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Transaksi berhasil disimpan!")
                }
            }
        )
    }

    // Dialog: Kalender Omset & Rekap
    if (showCalendarDialog) {
        CalendarRevenueDialog(
            initialSelectedDateMillis = selectedDateMillis,
            dailySummariesMap = dailySummariesMap,
            onDismiss = { showCalendarDialog = false },
            onDateSelected = { pickedDateMillis ->
                viewModel.setSelectedDate(pickedDateMillis)
                showCalendarDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Menampilkan transaksi: ${FormatUtils.formatDateMedium(pickedDateMillis)}")
                }
            },
            onOpenEditRevenueForDate = { dateMillis, summary ->
                targetPastDateMillis = dateMillis
                targetPastSummary = summary
                showCalendarDialog = false
                showEditPastRevenueDialog = true
            },
            onOpenAddTransactionForDate = { dateMillis ->
                viewModel.setSelectedDate(dateMillis)
                editingRecord = null
                showCalendarDialog = false
                showAddEditDialog = true
            }
        )
    }

    // Dialog: Edit Pemasukan Tanggal Terlewati
    if (showEditPastRevenueDialog) {
        EditPastDateRevenueDialog(
            dateMillis = targetPastDateMillis,
            existingSummary = targetPastSummary,
            activeWorkers = activeWorkers,
            onDismiss = { showEditPastRevenueDialog = false },
            onSave = { targetDate, motorCount, price, share, worker, payment, note, replace ->
                viewModel.setOrUpdateDailyRevenue(
                    targetDateMillis = targetDate,
                    motorCount = motorCount,
                    pricePerMotor = price,
                    washerSharePerMotor = share,
                    washerName = worker,
                    paymentMethod = payment,
                    note = note,
                    replaceExistingForDay = replace
                )
                showEditPastRevenueDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Pemasukan tanggal ${FormatUtils.formatDateMedium(targetDate)} berhasil diperbarui!")
                }
            },
            onDeleteRecordsForDate = { targetDate ->
                viewModel.deleteRecordsForDate(targetDate)
                showEditPastRevenueDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Data tanggal ${FormatUtils.formatDateMedium(targetDate)} telah dihapus.")
                }
            }
        )
    }

    // Dialog: Manage Workers
    if (showWorkersDialog) {
        ManageWorkersDialog(
            workers = activeWorkers,
            onAddWorker = { viewModel.addWorker(it) },
            onDeleteWorker = { viewModel.deleteWorker(it) },
            onDismiss = { showWorkersDialog = false }
        )
    }

    // Dialog: Confirm Delete Single Record
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text("Transaksi ${if (record.licensePlate.isNotBlank()) record.licensePlate else "${record.motorCount} motor"} (${FormatUtils.formatRupiah(record.totalPrice)}) akan dihapus dari catatan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(record)
                        recordToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Transaksi telah dihapus")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Confirm Clear All
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Hapus Semua Riwayat Transaksi?") },
            text = {
                Text("Semua data riwayat cuci steam motor akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecords()
                        showClearAllConfirm = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Semua riwayat berhasil dikosongkan")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Edit Dream Goal
    if (showEditDreamGoalDialog) {
        EditDreamGoalDialog(
            currentConfig = dreamGoalProgress.config,
            onDismiss = { showEditDreamGoalDialog = false },
            onSave = { newConfig ->
                viewModel.updateDreamGoalConfig(newConfig)
                showEditDreamGoalDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Target barang impian berhasil diperbarui!")
                }
            }
        )
    }

    // Dialog: Ekspor Laporan
    if (showExportDialog) {
        ExportReportDialog(
            viewModel = viewModel,
            monthlySummary = monthlySummary,
            onDismiss = { showExportDialog = false },
            onShowSnackbar = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    // Dialog: Cetak Struk Thermal Printer
    if (recordForThermalReceipt != null) {
        ThermalReceiptDialog(
            record = recordForThermalReceipt!!,
            onDismiss = { recordForThermalReceipt = null }
        )
    }

    // Dialog: Sanggah Transaksi (Manager Keuangan / Pemilik)
    if (recordForDispute != null) {
        DisputeTransactionDialog(
            record = recordForDispute!!,
            managerName = currentUser.name,
            onDismiss = { recordForDispute = null },
            onConfirmDispute = { reason ->
                val target = recordForDispute
                if (target != null) {
                    viewModel.disputeTransaction(target, reason)
                    recordForDispute = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaksi telah disanggah dan ditandai tidak sesuai")
                    }
                }
            }
        )
    }

    // Dialog: Ganti Akun Pengguna (Kasir / Manager / Pemilik)
    if (showSwitchUserDialog) {
        SwitchUserDialog(
            currentUser = currentUser,
            getAccountName = { role -> viewModel.getAccountName(role) },
            onDismiss = { showSwitchUserDialog = false },
            onConfirm = { targetRole, name, password, newPassword ->
                val result = viewModel.switchUserRoleWithAuth(targetRole, name, password, newPassword)
                if (result.first) {
                    showSwitchUserDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(result.second)
                    }
                }
                result
            }
        )
    }
}
