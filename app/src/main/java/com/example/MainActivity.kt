package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppThemeMode
import com.example.data.model.DebtType
import com.example.data.model.TransactionType
import com.example.ui.FinanceViewModel
import com.example.ui.components.DynamicIslandHeader
import com.example.ui.components.IPhoneBottomBar
import com.example.ui.dialogs.AddAssetDialog
import com.example.ui.dialogs.AddDebtDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.AddWishlistDialog
import com.example.ui.dialogs.AppearanceSettingsDialog
import com.example.ui.dialogs.AssetManagementDialog
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DebtScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FinanceViewModel = viewModel()
      val appearanceSettings by viewModel.appearanceSettings.collectAsState()
      val isDark = when (appearanceSettings.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      val view = LocalView.current
      if (!view.isInEditMode) {
        SideEffect {
          val window = (view.context as? Activity)?.window
          if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
          }
        }
      }

      MyApplicationTheme(darkTheme = isDark) {
        FinanceAppRoot(
          viewModel = viewModel,
          isDarkTheme = isDark
        )
      }
    }
  }
}

@Composable
fun FinanceAppRoot(
  viewModel: FinanceViewModel = viewModel(),
  isDarkTheme: Boolean = true
) {
  val context = LocalContext.current

  val appearanceSettings by viewModel.appearanceSettings.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val isDynamicIslandExpanded by viewModel.isDynamicIslandExpanded.collectAsState()
  val totalBalance by viewModel.totalBalance.collectAsState()
  val currentMonthExpense by viewModel.currentMonthExpense.collectAsState()
  val currentMonthIncome by viewModel.currentMonthIncome.collectAsState()
  val budgetSetting by viewModel.budgetSetting.collectAsState()
  val transactions by viewModel.filteredTransactions.collectAsState()
  val timeFilter by viewModel.timeFilter.collectAsState()
  val debts by viewModel.allDebts.collectAsState()
  val wishlists by viewModel.allWishlists.collectAsState()
  val monthlyReport by viewModel.monthlyReport.collectAsState()
  val selectedMonthMillis by viewModel.selectedMonthMillis.collectAsState()

  // Annual Report & Asset Metrics
  val assets by viewModel.allAssets.collectAsState()
  val yearlyReport by viewModel.yearlyReport.collectAsState()
  val selectedYear by viewModel.selectedYear.collectAsState()
  val totalUnpaidHutang by viewModel.totalUnpaidHutang.collectAsState()
  val totalUnpaidPiutang by viewModel.totalUnpaidPiutang.collectAsState()
  val totalAssetsValue by viewModel.totalAssetsValue.collectAsState()
  val netWorth by viewModel.netWorth.collectAsState()
  val levelInfo by viewModel.levelInfo.collectAsState()

  // Budget Items (Pos Anggaran)
  val budgetItemsWithProgress by viewModel.budgetItemsWithProgress.collectAsState()
  val totalAllocatedBudgetItems by viewModel.totalAllocatedBudgetItems.collectAsState()

  // Dialog States
  var showAppearanceSettingsDialog by remember { mutableStateOf(false) }
  var showAddTransactionDialog by remember { mutableStateOf(false) }
  var addTransactionInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }

  var showAddDebtDialog by remember { mutableStateOf(false) }
  var addDebtInitialType by remember { mutableStateOf(DebtType.HUTANG) }

  var showAddWishlistDialog by remember { mutableStateOf(false) }

  var showAddAssetDialog by remember { mutableStateOf(false) }
  var showManageAssetsDialog by remember { mutableStateOf(false) }

  // Launcher untuk menyimpan berkas ekspor (backup) ke penyimpanan HP
  val exportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    if (uri != null) {
      try {
        val jsonString = viewModel.exportDataToJson()
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
          outputStream.write(jsonString.toByteArray())
        }
        Toast.makeText(context, "Data berhasil diekspor", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal menyimpan berkas ekspor", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Launcher untuk memilih berkas backup yang akan diimpor
  val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      try {
        val jsonString = context.contentResolver.openInputStream(uri)
          ?.bufferedReader()
          ?.use { it.readText() }
        if (jsonString.isNullOrBlank()) {
          Toast.makeText(context, "Berkas kosong atau tidak dapat dibaca", Toast.LENGTH_SHORT).show()
        } else {
          viewModel.importDataFromJson(jsonString) { success, message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
          }
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal membaca berkas impor", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      DynamicIslandHeader(
        totalBalance = totalBalance,
        monthlyBudget = budgetSetting.monthlyBudget,
        currentExpense = currentMonthExpense,
        isExpanded = isDynamicIslandExpanded,
        onToggleExpand = { viewModel.toggleDynamicIsland() },
        onOpenAppearanceSettings = { showAppearanceSettingsDialog = true },
        onQuickToggleTheme = { viewModel.quickToggleTheme() },
        isDarkTheme = isDarkTheme
      )
    },
    bottomBar = {
      IPhoneBottomBar(
        selectedTab = selectedTab,
        onTabSelected = { viewModel.setTab(it) },
        navStyle = appearanceSettings.navStyle,
        accentColor = appearanceSettings.navAccent.colorValue,
        showLabels = appearanceSettings.showNavLabels,
        showActiveIndicatorPill = appearanceSettings.showActiveIndicatorPill
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> HomeScreen(
          totalBalance = totalBalance,
          currentMonthIncome = currentMonthIncome,
          currentMonthExpense = currentMonthExpense,
          budgetSetting = budgetSetting,
          transactions = transactions,
          selectedFilter = timeFilter,
          onFilterSelected = { viewModel.setTimeFilter(it) },
          onAddTransactionClick = { typeStr ->
            addTransactionInitialType = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
            showAddTransactionDialog = true
          },
          onDeleteTransaction = { viewModel.deleteTransaction(it) },
          onNavigateToBudget = { viewModel.setTab(4) },
          totalUnpaidHutang = totalUnpaidHutang,
          totalUnpaidPiutang = totalUnpaidPiutang,
          totalAssetsValue = totalAssetsValue,
          netWorth = netWorth,
          assetCount = assets.size,
          onManageAssetsClick = { showManageAssetsDialog = true },
          onAddAssetClick = { showAddAssetDialog = true },
          onNavigateToDebts = { viewModel.setTab(2) },
          levelInfo = levelInfo,
          levelingEnabled = appearanceSettings.levelingEnabled
        )

        1 -> ReportScreen(
          reportData = monthlyReport,
          selectedMonthMillis = selectedMonthMillis,
          onPreviousMonth = { viewModel.previousMonth() },
          onNextMonth = { viewModel.nextMonth() },
          yearlyReport = yearlyReport,
          selectedYear = selectedYear,
          onPreviousYear = { viewModel.previousYear() },
          onNextYear = { viewModel.nextYear() },
          cashOnHand = totalBalance,
          totalPiutang = totalUnpaidPiutang,
          totalHutang = totalUnpaidHutang,
          totalAssetsValue = totalAssetsValue,
          netWorth = netWorth
        )

        2 -> DebtScreen(
          debts = debts,
          onAddDebtClick = { type ->
            addDebtInitialType = type
            showAddDebtDialog = true
          },
          onToggleStatus = { debt, recordToCashflow ->
            viewModel.toggleDebtStatus(debt, recordToCashflow)
          },
          onDeleteDebt = { viewModel.deleteDebt(it) }
        )

        3 -> WishlistScreen(
          wishlists = wishlists,
          onAddWishlistClick = { showAddWishlistDialog = true },
          onDeposit = { item, depositAmount, recordExpense ->
            viewModel.depositToWishlist(item, depositAmount, recordExpense)
          },
          onDeleteWishlist = { viewModel.deleteWishlist(it) }
        )

        4 -> BudgetScreen(
          budgetSetting = budgetSetting,
          currentExpense = currentMonthExpense,
          totalCash = totalBalance,
          budgetItemsWithProgress = budgetItemsWithProgress,
          totalAllocatedBudgetItems = totalAllocatedBudgetItems,
          onUpdateSetting = { monthlyBudget, thresholdPercent, notificationEnabled ->
            viewModel.updateBudgetSetting(monthlyBudget, thresholdPercent, notificationEnabled)
          },
          onAddBudgetItem = { name, category, allocatedAmount, deductFromCash, colorHex, notes ->
            viewModel.addBudgetItem(name, category, allocatedAmount, deductFromCash, colorHex, notes)
          },
          onTopUpBudgetItem = { item, additionalAmount, deductFromCash ->
            viewModel.topUpBudgetItem(item, additionalAmount, deductFromCash)
          },
          onReturnBudgetFunds = { item, returnAmount ->
            viewModel.returnBudgetFundsToCash(item, returnAmount)
          },
          onDeleteBudgetItem = { item, refundRemaining, remainingAmount ->
            viewModel.deleteBudgetItem(item, refundRemaining, remainingAmount)
          },
          onAllocateMonthlyBudgetFromCash = { amount, note ->
            viewModel.allocateGeneralBudgetFromCash(amount, note)
          },
          onTestNotification = {
            viewModel.triggerTestNotification(context)
          },
          onOpenAppearanceClick = { showAppearanceSettingsDialog = true }
        )
      }
    }
  }

  // Appearance & Navigation Customizer Dialog
  if (showAppearanceSettingsDialog) {
    AppearanceSettingsDialog(
      currentSettings = appearanceSettings,
      onUpdateThemeMode = { viewModel.updateThemeMode(it) },
      onUpdateNavStyle = { viewModel.updateNavStyle(it) },
      onUpdateNavAccent = { viewModel.updateNavAccent(it) },
      onToggleNavLabels = { viewModel.toggleNavLabels() },
      onToggleActivePill = { viewModel.toggleActiveIndicatorPill() },
      onToggleLeveling = { viewModel.toggleLevelingFeature() },
      onExportData = {
        val fileName = "ifinance_backup_${System.currentTimeMillis()}.json"
        exportLauncher.launch(fileName)
      },
      onImportData = {
        importLauncher.launch(arrayOf("application/json"))
      },
      onDismiss = { showAppearanceSettingsDialog = false }
    )
  }

  // Dialogs
  if (showAddTransactionDialog) {
    AddTransactionDialog(
      initialType = addTransactionInitialType,
      onDismiss = { showAddTransactionDialog = false },
      onSave = { title, amount, type, category, note ->
        viewModel.addTransaction(title, amount, type, category, System.currentTimeMillis(), note, context)
      }
    )
  }

  if (showAddDebtDialog) {
    AddDebtDialog(
      initialType = addDebtInitialType,
      onDismiss = { showAddDebtDialog = false },
      onSave = { personName, type, amount, dueDateMillis, notes ->
        viewModel.addDebt(personName, type, amount, dueDateMillis, notes)
      }
    )
  }

  if (showAddWishlistDialog) {
    AddWishlistDialog(
      onDismiss = { showAddWishlistDialog = false },
      onSave = { name, targetAmount, initialSaved, targetDateMillis, category, notes ->
        viewModel.addWishlist(name, targetAmount, initialSaved, targetDateMillis, category, notes)
      }
    )
  }

  if (showAddAssetDialog) {
    AddAssetDialog(
      onDismiss = { showAddAssetDialog = false },
      onSave = { name, category, value, notes ->
        viewModel.addAsset(name, category, value, notes)
        showAddAssetDialog = false
      }
    )
  }

  if (showManageAssetsDialog) {
    AssetManagementDialog(
      assets = assets,
      totalAssetValue = totalAssetsValue,
      onDismiss = { showManageAssetsDialog = false },
      onAddClick = {
        showManageAssetsDialog = false
        showAddAssetDialog = true
      },
      onDeleteAsset = { asset ->
        viewModel.deleteAsset(asset)
      }
    )
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun FinanceAppPreview() {
  MyApplicationTheme {
    Greeting("iFinance")
  }
}
