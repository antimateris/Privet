package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreExpense
import com.example.util.FormatUtils

@Composable
fun AddEditExpenseDialog(
    initialExpense: StoreExpense? = null,
    onSave: (title: String, category: String, amount: Long, note: String, timestamp: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: StoreExpense.CATEGORY_BAHAN_CUCI) }
    var amountText by remember { mutableStateOf(if (initialExpense != null && initialExpense.amount > 0) initialExpense.amount.toString() else "") }
    var note by remember { mutableStateOf(initialExpense?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickTitles = listOf(
        "Sabun Salju",
        "Semir Ban",
        "Bensin Mesin Steam",
        "Token Listrik & Air",
        "Lap Kanebo & Spon",
        "Konsumsi Petugas"
    )

    val quickAmounts = listOf(10_000L, 20_000L, 50_000L, 100_000L, 200_000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (initialExpense == null) "Catat Pengeluaran Toko" else "Ubah Pengeluaran Toko",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nama / Keperluan Pengeluaran
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Nama / Keperluan Pengeluaran *") },
                        placeholder = { Text("Contoh: Beli Sabun Salju 5 Liter") },
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_title_input")
                    )

                    // Quick title suggestions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTitles.forEach { quick ->
                            SuggestionChip(
                                onClick = {
                                    title = quick
                                    // Auto select matching category
                                    when {
                                        quick.contains("Sabun", true) || quick.contains("Semir", true) ->
                                            category = StoreExpense.CATEGORY_BAHAN_CUCI
                                        quick.contains("Bensin", true) ->
                                            category = StoreExpense.CATEGORY_BENSIN
                                        quick.contains("Listrik", true) || quick.contains("Air", true) ->
                                            category = StoreExpense.CATEGORY_LISTRIK_AIR
                                        quick.contains("Spon", true) || quick.contains("Lap", true) ->
                                            category = StoreExpense.CATEGORY_PERLENGKAPAN
                                        quick.contains("Konsumsi", true) ->
                                            category = StoreExpense.CATEGORY_KONSUMSI
                                    }
                                },
                                label = { Text(quick, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Kategori
                Column {
                    Text(
                        text = "Kategori Pengeluaran",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StoreExpense.ALL_CATEGORIES.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Nominal Pengeluaran
                Column {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            val clean = it.filter { char -> char.isDigit() }
                            amountText = clean
                            errorMessage = null
                        },
                        label = { Text("Nominal Pengeluaran (Rp) *") },
                        placeholder = { Text("0") },
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    val currentAmount = amountText.toLongOrNull() ?: 0L
                    if (currentAmount > 0) {
                        Text(
                            text = "Terformat: ${FormatUtils.formatRupiah(currentAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    // Quick amount increment buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAmounts.forEach { quickAdd ->
                            SuggestionChip(
                                onClick = {
                                    val cur = amountText.toLongOrNull() ?: 0L
                                    amountText = (cur + quickAdd).toString()
                                },
                                label = { Text("+${FormatUtils.formatRupiah(quickAdd)}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Catatan / Keterangan
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Beli di toko Sumber Rezeki, bon terlampir") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input")
                )

                // Error message banner if any
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isBlank()) {
                        errorMessage = "Nama pengeluaran tidak boleh kosong"
                        return@Button
                    }
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0L) {
                        errorMessage = "Nominal harus lebih besar dari Rp 0"
                        return@Button
                    }
                    val timestamp = initialExpense?.timestamp ?: System.currentTimeMillis()
                    onSave(trimmedTitle, category, amount, note.trim(), timestamp)
                },
                modifier = Modifier.testTag("submit_expense_button")
            ) {
                Text(if (initialExpense == null) "Simpan Pengeluaran" else "Perbarui")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_expense_button")
            ) {
                Text("Batal")
            }
        }
    )
}
