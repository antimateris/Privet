package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreExpense
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseScreen(
    expenses: List<StoreExpense>,
    todayTotal: Long,
    monthlyTotal: Long,
    monthlyOwnerShare: Long,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (StoreExpense) -> Unit,
    onDeleteExpenseClick: (StoreExpense) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val categoriesFilter = listOf("Semua") + StoreExpense.ALL_CATEGORIES

    val filteredExpenses = expenses.filter { expense ->
        val matchCategory = selectedCategory == "Semua" || expense.category == selectedCategory
        val matchSearch = searchQuery.isBlank() ||
                expense.title.contains(searchQuery, ignoreCase = true) ||
                expense.category.contains(searchQuery, ignoreCase = true) ||
                expense.note.contains(searchQuery, ignoreCase = true) ||
                expense.recordedBy.contains(searchQuery, ignoreCase = true)
        matchCategory && matchSearch
    }

    val netProfit = monthlyOwnerShare - monthlyTotal
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Financial Summary Overview Cards ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card Hari Ini
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("expense_card_today"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hari Ini",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatRupiah(todayTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Total biaya toko hari ini",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }

                    // Card Bulan Ini
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("expense_card_monthly"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFEDD5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bulan Ini",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9A3412),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatRupiah(monthlyTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C)
                            )
                            Text(
                                text = "Akumulasi operasional",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = Color(0xFF7C2D12)
                            )
                        }
                    }
                }

                // Card Laba Bersih Toko (Net Profit)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_net_profit_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (netProfit >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (netProfit >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (netProfit >= 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Laba Bersih Pemilik (Bulan Ini)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netProfit >= 0) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Porsi Pemilik (${FormatUtils.formatRupiah(monthlyOwnerShare)}) - Pengeluaran Toko",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }

                        Text(
                            text = (if (netProfit < 0) "- " else "") + FormatUtils.formatRupiah(kotlin.math.abs(netProfit)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netProfit >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // --- 2. Action Header: Add Expense Button ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Riwayat Pengeluaran Toko",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredExpenses.size} transaksi tercatat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_expense_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catat Pengeluaran", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // --- 3. Filter & Search Section ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari pengeluaran, nota, petugas...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_expense_input")
                )

                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesFilter.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // --- 4. List of Expenses ---
        if (filteredExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "Semua")
                                "Tidak ada pengeluaran yang sesuai filter"
                            else
                                "Belum ada pengeluaran toko yang dicatat",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isBlank() && selectedCategory == "Semua") {
                            Button(
                                onClick = onAddExpenseClick,
                                modifier = Modifier.testTag("empty_add_expense_button")
                            ) {
                                Text("+ Catat Pengeluaran Pertama")
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.timestamp }) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_item_${expense.timestamp}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: Category badge and Amount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = expense.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "- ${FormatUtils.formatRupiah(expense.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626)
                            )
                        }

                        // Row 2: Title / Purpose
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Row 3: Note if present
                        if (expense.note.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = expense.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Row 4: Meta & Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormat.format(Date(expense.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Dicatat oleh: ${expense.recordedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onEditExpenseClick(expense) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("edit_expense_btn_${expense.timestamp}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteExpenseClick(expense) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("delete_expense_btn_${expense.timestamp}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
