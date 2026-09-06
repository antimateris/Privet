package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.DreamGoalConfig
import com.example.ui.SavingsSource
import com.example.ui.SavingsTargetFor
import com.example.util.FormatUtils

data class PresetDreamItem(
    val name: String,
    val price: Long,
    val iconEmoji: String,
    val targetFor: SavingsTargetFor = SavingsTargetFor.PEMILIK
)

val PRESET_DREAM_ITEMS = listOf(
    PresetDreamItem("Kompresor Steam Matrix 2 HP", 2_200_000L, "💨", SavingsTargetFor.PEMILIK),
    PresetDreamItem("Tabung Salju Snow Wash 20L", 850_000L, "🧼", SavingsTargetFor.PEMILIK),
    PresetDreamItem("Bonus & Tabungan THR Karyawan", 3_000_000L, "👥", SavingsTargetFor.KARYAWAN),
    PresetDreamItem("Mesin Steam Jet Cleaner High Pressure", 1_750_000L, "🚿", SavingsTargetFor.PEMILIK),
    PresetDreamItem("Uang Kas / Tabungan Bersama Karyawan", 1_500_000L, "💰", SavingsTargetFor.KARYAWAN),
    PresetDreamItem("HP Android Kasir & Mini Thermal Printer", 1_400_000L, "📱", SavingsTargetFor.PEMILIK),
    PresetDreamItem("Renovasi Kanopi & Tempat Cuci", 3_500_000L, "🏗️", SavingsTargetFor.PEMILIK)
)

@Composable
fun EditDreamGoalDialog(
    currentConfig: DreamGoalConfig,
    onDismiss: () -> Unit,
    onSave: (DreamGoalConfig) -> Unit
) {
    var itemName by remember { mutableStateOf(currentConfig.itemName) }
    var targetAmountText by remember { mutableStateOf(currentConfig.targetAmount.toString()) }
    var dailyTargetText by remember { mutableStateOf(currentConfig.dailyTargetAmount.toString()) }
    var initialSavingsText by remember { mutableStateOf(currentConfig.initialSavings.toString()) }
    var savingSource by remember { mutableStateOf(currentConfig.savingSource) }
    var targetFor by remember { mutableStateOf(currentConfig.targetFor) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("edit_dream_goal_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Atur Target Tabungan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pilih peruntukan karyawan atau pemilik",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Target Tabungan Untuk Siapa?
                Text(
                    text = "Peruntukan Target Tabungan:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option Pemilik
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                targetFor = SavingsTargetFor.PEMILIK
                                if (savingSource == SavingsSource.WASHER_SHARE) {
                                    savingSource = SavingsSource.TOTAL_REVENUE
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (targetFor == SavingsTargetFor.PEMILIK) Color(0xFFEFF6FF)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            if (targetFor == SavingsTargetFor.PEMILIK) 2.dp else 1.dp,
                            if (targetFor == SavingsTargetFor.PEMILIK) Color(0xFF2563EB)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = if (targetFor == SavingsTargetFor.PEMILIK) Color(0xFF2563EB) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pemilik Usaha",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (targetFor == SavingsTargetFor.PEMILIK) Color(0xFF1E40AF) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Investasi alat steam & renovasi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Option Karyawan
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                targetFor = SavingsTargetFor.KARYAWAN
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (targetFor == SavingsTargetFor.KARYAWAN) Color(0xFFF0FDF4)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            if (targetFor == SavingsTargetFor.KARYAWAN) 2.dp else 1.dp,
                            if (targetFor == SavingsTargetFor.KARYAWAN) Color(0xFF16A34A)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (targetFor == SavingsTargetFor.KARYAWAN) Color(0xFF16A34A) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Karyawan Steam",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (targetFor == SavingsTargetFor.KARYAWAN) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tabungan / THR disisihkan per omset",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Preset Chips
                Text(
                    text = "Pilih Contoh Barang / Target:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_DREAM_ITEMS.forEach { preset ->
                        FilterChip(
                            selected = itemName == preset.name,
                            onClick = {
                                itemName = preset.name
                                targetAmountText = preset.price.toString()
                                targetFor = preset.targetFor
                            },
                            label = {
                                Text("${preset.iconEmoji} ${preset.name}", fontSize = 12.sp)
                            }
                        )
                    }
                }

                // Input Nama Barang
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nama Target Tabungan / Barang") },
                    placeholder = {
                        Text(
                            if (targetFor == SavingsTargetFor.KARYAWAN) "Misal: Bonus THR / Kas Karyawan"
                            else "Misal: Kompresor Steam Matrix 2 HP"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dream_item_name")
                )

                // Input Target Harga
                val parsedTarget = targetAmountText.toLongOrNull() ?: 0L
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) targetAmountText = input
                    },
                    label = { Text("Nominal Target Tabungan (Rp)") },
                    supportingText = {
                        if (parsedTarget > 0) {
                            Text("Terbaca: ${FormatUtils.formatRupiah(parsedTarget)}")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dream_target_amount")
                )

                // Input Target Tabungan Harian
                val parsedDailyTarget = dailyTargetText.toLongOrNull() ?: 0L
                OutlinedTextField(
                    value = dailyTargetText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) dailyTargetText = input
                    },
                    label = { Text("Target Pendapatan Harian (Rp/hari)") },
                    supportingText = {
                        Text(
                            if (parsedDailyTarget > 0) "Target per hari: ${FormatUtils.formatRupiah(parsedDailyTarget)}"
                            else "Digunakan untuk menghitung capaian harian"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Tabungan Awal (Opsional)
                val parsedInitialSavings = initialSavingsText.toLongOrNull() ?: 0L
                OutlinedTextField(
                    value = initialSavingsText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) initialSavingsText = input
                    },
                    label = { Text("Tabungan Awal Tersimpan (Opsional)") },
                    supportingText = {
                        Text(
                            if (parsedInitialSavings > 0) "Saldo awal: ${FormatUtils.formatRupiah(parsedInitialSavings)}"
                            else "Jika Anda sudah memiliki saldo tersimpan sebelumnya"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sumber Perhitungan
                Text(
                    text = "Disisihkan Dari Sumber Pendapatan:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SavingsSource.values().forEach { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { savingSource = source }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = savingSource == source,
                                onClick = { savingSource = source }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = source.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = source.shortDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (itemName.isBlank()) {
                                errorMessage = "Nama target tidak boleh kosong"
                                return@Button
                            }
                            val target = targetAmountText.toLongOrNull() ?: 0L
                            if (target <= 0) {
                                errorMessage = "Target harga harus lebih besar dari Rp 0"
                                return@Button
                            }
                            val daily = dailyTargetText.toLongOrNull() ?: 100_000L
                            val initial = initialSavingsText.toLongOrNull() ?: 0L

                            onSave(
                                DreamGoalConfig(
                                    itemName = itemName.trim(),
                                    targetAmount = target,
                                    initialSavings = initial,
                                    savingSource = savingSource,
                                    targetFor = targetFor,
                                    dailyTargetAmount = if (daily > 0) daily else 100_000L
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_dream_goal_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Target", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
