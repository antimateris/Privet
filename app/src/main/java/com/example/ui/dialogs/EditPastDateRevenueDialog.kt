package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Worker
import com.example.ui.DayRevenueSummary
import com.example.util.FormatUtils

@Composable
fun EditPastDateRevenueDialog(
    dateMillis: Long,
    existingSummary: DayRevenueSummary?,
    activeWorkers: List<Worker>,
    onDismiss: () -> Unit,
    onSave: (
        targetDateMillis: Long,
        motorCount: Int,
        pricePerMotor: Long,
        washerSharePerMotor: Long,
        washerName: String,
        paymentMethod: String,
        note: String,
        replaceExisting: Boolean
    ) -> Unit,
    onDeleteRecordsForDate: (targetDateMillis: Long) -> Unit
) {
    var motorCount by remember {
        mutableIntStateOf(existingSummary?.motorCount ?: 15)
    }
    var pricePerMotorText by remember { mutableStateOf("10000") }
    var washerSharePerMotorText by remember { mutableStateOf("5000") }
    var selectedWorker by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Tunai") }
    var note by remember {
        mutableStateOf(if (existingSummary != null) "Penyesuaian rekap pemasukan" else "Pemasukan tanggal terlewati")
    }
    var replaceExisting by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val pricePerMotor = pricePerMotorText.toLongOrNull() ?: 10000L
    val washerSharePerMotor = washerSharePerMotorText.toLongOrNull() ?: 5000L
    val totalRevenue = pricePerMotor * motorCount
    val totalWasherShare = washerSharePerMotor * motorCount
    val totalOwnerShare = (totalRevenue - totalWasherShare).coerceAtLeast(0L)

    val dateFormatted = FormatUtils.formatDateFull(dateMillis)
    val isPast = !FormatUtils.isToday(dateMillis)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Rekap Tanggal Ini?") },
            text = {
                Text("Semua data transaksi di tanggal $dateFormatted akan dihapus dan pemasukan tanggal ini menjadi Rp 0.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteRecordsForDate(dateMillis)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isPast) "Edit Pemasukan Tanggal Terlewati" else "Edit Pemasukan Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (existingSummary != null && existingSummary.motorCount > 0) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Status Data Saat Ini:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (existingSummary != null && existingSummary.motorCount > 0) {
                                Text(
                                    text = "${existingSummary.motorCount} Motor • ${FormatUtils.formatRupiah(existingSummary.totalRevenue)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8)
                                )
                                Text(
                                    text = "Kas Pemilik: ${FormatUtils.formatRupiah(existingSummary.totalOwnerShare)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Belum ada catatan transaksi (Rp 0)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (existingSummary != null && existingSummary.motorCount > 0) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus pemasukan tanggal ini",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Input Jumlah Motor
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Jumlah Motor Cuci di Tanggal Ini:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (motorCount > 0) motorCount-- },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedTextField(
                                value = motorCount.toString(),
                                onValueChange = {
                                    motorCount = it.toIntOrNull() ?: 0
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_past_motor_count_input"),
                                singleLine = true,
                                suffix = { Text("Motor") }
                            )

                            OutlinedButton(
                                onClick = { motorCount++ },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick preset chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 15, 20, 25, 30).forEach { count ->
                                FilterChip(
                                    selected = motorCount == count,
                                    onClick = { motorCount = count },
                                    label = { Text("$count", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Kalkulasi Hasil Rekap
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Omset Kotor:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                FormatUtils.formatRupiah(totalRevenue),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bagi Hasil Petugas (50%):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                FormatUtils.formatRupiah(totalWasherShare),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Bersih Pemilik (50%):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                FormatUtils.formatRupiah(totalOwnerShare),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Tarif Custom jika ada penyesuaian tarif
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pricePerMotorText,
                        onValueChange = { pricePerMotorText = it },
                        label = { Text("Tarif/Motor", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = washerSharePerMotorText,
                        onValueChange = { washerSharePerMotorText = it },
                        label = { Text("Bagi Hasil/Motor", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Petugas & Catatan
                if (activeWorkers.isNotEmpty()) {
                    Text(
                        text = "Petugas Pencuci (Opsional):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedWorker.isBlank(),
                            onClick = { selectedWorker = "" },
                            label = { Text("Semua/Campur", fontSize = 11.sp) }
                        )
                        activeWorkers.take(3).forEach { worker ->
                            FilterChip(
                                selected = selectedWorker == worker.name,
                                onClick = { selectedWorker = worker.name },
                                label = { Text(worker.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Rekap") },
                    placeholder = { Text("Contoh: Hujan sore hari, ramai pagi, dll.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Option to replace existing
                if (existingSummary != null && existingSummary.motorCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = replaceExisting,
                            onCheckedChange = { replaceExisting = it }
                        )
                        Text(
                            text = "Timpa/Perbarui seluruh data di tanggal ini",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        dateMillis,
                        motorCount,
                        pricePerMotor,
                        washerSharePerMotor,
                        selectedWorker,
                        paymentMethod,
                        note,
                        replaceExisting
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_past_revenue_button")
            ) {
                Text("Simpan Pemasukan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_past_revenue_button")
            ) {
                Text("Batal")
            }
        }
    )
}
