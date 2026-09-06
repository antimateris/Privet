package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WashRecord
import com.example.util.ThermalReceiptUtils

@Composable
fun ThermalReceiptDialog(
    record: WashRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showEditInfo by remember { mutableStateOf(false) }
    var receiptPhone by remember { mutableStateOf(ThermalReceiptUtils.getReceiptPhone(context)) }
    var receiptCashier by remember { mutableStateOf(record.createdBy.ifBlank { "Kasir" }) }

    val receiptText = remember(record, receiptPhone, receiptCashier) {
        ThermalReceiptUtils.generateReceiptText(
            record = record,
            phone = receiptPhone,
            cashierName = receiptCashier
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Struk Pelanggan (Thermal)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Settings Card: Ubah WA & Kasir
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEditInfo = !showEditInfo },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showEditInfo) "Tutup Pengaturan Struk" else "Ubah Nomor WA & Nama Kasir",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                imageVector = if (showEditInfo) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showEditInfo) {
                            OutlinedTextField(
                                value = receiptPhone,
                                onValueChange = {
                                    receiptPhone = it
                                    ThermalReceiptUtils.saveReceiptPhone(context, it)
                                },
                                label = { Text("Nomor WA / Kontak Struk") },
                                placeholder = { Text("Contoh: WA: 0812-3456-7890") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_receipt_wa_phone")
                            )

                            OutlinedTextField(
                                value = receiptCashier,
                                onValueChange = {
                                    receiptCashier = it
                                },
                                label = { Text("Nama Kasir pada Struk") },
                                placeholder = { Text("Contoh: Kasir Utama, Budi") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_receipt_cashier")
                            )

                            Text(
                                text = "Perubahan nomor WA akan otomatis disimpan untuk struk berikutnya.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Simulated Thermal Paper Roll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), shape = RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = receiptText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("thermal_receipt_preview_text")
                    )
                }

                Text(
                    text = "Ukuran standar printer 58mm / 80mm ESC/POS. Kompatibel dengan printer Bluetooth (RawBT, Bluetooth Print) & WiFi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        ThermalReceiptUtils.shareToThermalPrinter(
                            context = context,
                            record = record,
                            phone = receiptPhone,
                            cashierName = receiptCashier
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_print_thermal_bluetooth")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim ke Printer Thermal / Bluetooth")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ThermalReceiptUtils.printReceiptNative(
                                context = context,
                                record = record,
                                phone = receiptPhone,
                                cashierName = receiptCashier
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_thermal_native")
                    ) {
                        Text("Cetak WiFi/PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            ThermalReceiptUtils.copyReceiptToClipboard(
                                context = context,
                                record = record,
                                phone = receiptPhone,
                                cashierName = receiptCashier
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_receipt")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Teks", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {}
    )
}
