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
import androidx.compose.material.icons.filled.Savings
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
import com.example.util.FormatUtils

data class PresetDreamItem(
    val name: String,
    val price: Long,
    val iconEmoji: String
)

val PRESET_DREAM_ITEMS = listOf(
    PresetDreamItem("Kompresor Steam Matrix 2 HP", 2_200_000L, "💨"),
    PresetDreamItem("Tabung Salju Snow Wash 20L", 850_000L, "🧼"),
    PresetDreamItem("Mesin Steam Jet Cleaner High Pressure", 1_750_000L, "🚿"),
    PresetDreamItem("HP Android Kasir & Mini Thermal Printer", 1_400_000L, "📱"),
    PresetDreamItem("Renovasi Kanopi & Tempat Cuci", 3_500_000L, "🏗️"),
    PresetDreamItem("Motor Operasional Antar-Jemput", 8_000_000L, "🛵")
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
                                text = "Atur Barang Impian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target tabungan dari omset harian",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Preset Chips
                Text(
                    text = "Pilih Preset Barang Steam (Opsional):",
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
                    label = { Text("Nama Barang Impian") },
                    placeholder = { Text("Misal: Kompresor Steam Matrix 2 HP") },
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
                    label = { Text("Target Harga Barang (Rp)") },
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
                            if (parsedInitialSavings > 0) "Modal awal: ${FormatUtils.formatRupiah(parsedInitialSavings)}"
                            else "Jika Anda sudah ada saldo tabungan sebelumnya"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sumber Perhitungan
                Text(
                    text = "Basis Perhitungan Tabungan:",
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
                                errorMessage = "Nama barang tidak boleh kosong"
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
