package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.example.util.FormatUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditWashDialog(
    initialRecord: WashRecord? = null,
    activeWorkers: List<Worker>,
    preselectedWorker: String = "",
    defaultDateMillis: Long = initialRecord?.timestamp ?: System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        motorCount: Int,
        licensePlate: String,
        motorType: String,
        washerName: String,
        pricePerMotor: Long,
        washerSharePerMotor: Long,
        paymentMethod: String,
        note: String,
        timestamp: Long
    ) -> Unit
) {
    var motorCount by remember { mutableIntStateOf(initialRecord?.motorCount ?: 1) }
    var licensePlate by remember { mutableStateOf(initialRecord?.licensePlate ?: "") }
    var motorType by remember { mutableStateOf(initialRecord?.motorType ?: "Standar (Matic/Bebek)") }
    var selectedWorker by remember {
        mutableStateOf(initialRecord?.washerName ?: preselectedWorker)
    }
    var pricePerMotorText by remember {
        mutableStateOf((initialRecord?.pricePerMotor ?: 10000L).toString())
    }
    var washerSharePerMotorText by remember {
        mutableStateOf((initialRecord?.washerSharePerMotor ?: 5000L).toString())
    }
    var paymentMethod by remember { mutableStateOf(initialRecord?.paymentMethod ?: "Tunai") }
    var note by remember { mutableStateOf(initialRecord?.note ?: "") }
    var transactionTimestamp by remember {
        mutableLongStateOf(initialRecord?.timestamp ?: defaultDateMillis)
    }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val pricePerMotor = pricePerMotorText.toLongOrNull() ?: 10000L
    val washerSharePerMotor = washerSharePerMotorText.toLongOrNull() ?: 5000L
    val totalPrice = pricePerMotor * motorCount
    val totalWasherShare = washerSharePerMotor * motorCount
    val totalOwnerShare = (totalPrice - totalWasherShare).coerceAtLeast(0L)

    val commonTypes = listOf(
        "Standar (Matic/Bebek)",
        "Motor Besar (Sport/250cc+)",
        "Cuci + Semir Body",
        "Lainnya"
    )

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = transactionTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            // Preserve current time of day
                            val calCurrent = Calendar.getInstance().apply { timeInMillis = transactionTimestamp }
                            val hour = calCurrent.get(Calendar.HOUR_OF_DAY)
                            val minute = calCurrent.get(Calendar.MINUTE)

                            val calTarget = Calendar.getInstance().apply {
                                timeInMillis = pickedUtcMillis
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                            }
                            transactionTimestamp = calTarget.timeInMillis
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialRecord == null) "Catat Steam Motor" else "Edit Transaksi",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date & Time Selector for Past or Present
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = FormatUtils.formatRelativeDate(transactionTimestamp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = FormatUtils.formatTime(transactionTimestamp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Presets: Hari Ini, Kemarin, Pilih Tanggal Lain
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isToday = FormatUtils.isToday(transactionTimestamp)
                            val isYesterday = FormatUtils.isYesterday(transactionTimestamp)

                            FilterChip(
                                selected = isToday,
                                onClick = { transactionTimestamp = System.currentTimeMillis() },
                                label = { Text("Hari Ini (HP)", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )

                            FilterChip(
                                selected = isYesterday,
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    transactionTimestamp = cal.timeInMillis
                                },
                                label = { Text("Kemarin", fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )

                            OutlinedButton(
                                onClick = { showDatePickerDialog = true },
                                modifier = Modifier.height(28.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kalender...", fontSize = 11.sp)
                            }
                        }
                    }
                }
                // Stepper for Motor Count
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Jumlah Motor",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$motorCount Motor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (motorCount > 1) motorCount-- },
                                enabled = motorCount > 1,
                                modifier = Modifier.testTag("decrease_count_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurangi")
                            }

                            Text(
                                text = "$motorCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { motorCount++ },
                                modifier = Modifier.testTag("increase_count_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah")
                            }
                        }
                    }
                }

                // License plate
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text("Plat Nomor (Opsional, cth: B 1234 ABC)") },
                    placeholder = { Text("B 1234 XYZ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_plate_input")
                )

                // Worker Selection
                Text(
                    text = "Pilih Petugas Pencuci:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedWorker.isBlank(),
                        onClick = { selectedWorker = "" },
                        label = { Text("Tanpa Petugas") }
                    )
                    activeWorkers.forEach { worker ->
                        FilterChip(
                            selected = selectedWorker == worker.name,
                            onClick = { selectedWorker = worker.name },
                            label = { Text(worker.name) },
                            leadingIcon = if (selectedWorker == worker.name) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                // Motor Type Selection
                Text(
                    text = "Jenis Layanan / Tipe:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonTypes.forEach { type ->
                        FilterChip(
                            selected = motorType == type,
                            onClick = {
                                motorType = type
                                if (type == "Motor Besar (Sport/250cc+)" && pricePerMotorText == "10000") {
                                    pricePerMotorText = "15000"
                                    washerSharePerMotorText = "7500"
                                } else if (type == "Standar (Matic/Bebek)" && pricePerMotorText == "15000") {
                                    pricePerMotorText = "10000"
                                    washerSharePerMotorText = "5000"
                                }
                            },
                            label = { Text(type, fontSize = 12.sp) }
                        )
                    }
                }

                // Price and Share Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pricePerMotorText,
                        onValueChange = { pricePerMotorText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tarif / Motor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("price_input"),
                        singleLine = true,
                        prefix = { Text("Rp ") }
                    )

                    OutlinedTextField(
                        value = washerSharePerMotorText,
                        onValueChange = { washerSharePerMotorText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Bagi Hasil / Motor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_input"),
                        singleLine = true,
                        prefix = { Text("Rp ") }
                    )
                }

                // Calculation Result Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Rincian Pembagian Total ($motorCount Motor):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Omset:")
                            Text(FormatUtils.formatRupiah(totalPrice), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bagi Hasil Petugas:")
                            Text(
                                FormatUtils.formatRupiah(totalWasherShare),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Bersih Usaha Steam:")
                            Text(
                                FormatUtils.formatRupiah(totalOwnerShare),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Payment Method
                Text(
                    text = "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tunai", "QRIS", "Transfer").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: langganan, motor warna merah, dll.") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialRecord?.id ?: 0L,
                        motorCount,
                        licensePlate,
                        motorType,
                        selectedWorker,
                        pricePerMotor,
                        washerSharePerMotor,
                        paymentMethod,
                        note,
                        transactionTimestamp
                    )
                },
                modifier = Modifier.testTag("save_wash_button")
            ) {
                Text(if (initialRecord == null) "Simpan Transaksi" else "Perbarui")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_wash_button")
            ) {
                Text("Batal")
            }
        }
    )
}
