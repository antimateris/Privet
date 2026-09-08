package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WashRecord
import com.example.util.FormatUtils

@Composable
fun DisputeTransactionDialog(
    record: WashRecord,
    managerName: String,
    onDismiss: () -> Unit,
    onConfirmDispute: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val commonReasons = listOf(
        "Plat motor tidak cocok CCTV",
        "Jumlah motor selisih",
        "Pencuci tidak sesuai jadwal",
        "Nominal bayar tidak sesuai",
        "Data dobel / salah input"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sanggah Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Sebagai akun yang berwenang (Pemilik Usaha / Team IT), Anda dapat menyanggah transaksi jika terdapat ketidaksesuaian data. Jika dalam 24 jam tidak ada sanggahan, sistem akan otomatis mengesahkan data sebagai valid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Transaction info card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Detail Transaksi:",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "• ${record.motorCount} Motor (${record.motorType}) - ${FormatUtils.formatRupiah(record.totalPrice)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (record.licensePlate.isNotBlank()) {
                        Text(text = "• Plat: ${record.licensePlate}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = "• Pencuci: ${record.washerName.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Diinput oleh: ${record.createdBy}", style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = "Pilih Alasan Cepat:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(commonReasons) { quickReason ->
                        FilterChip(
                            selected = reason == quickReason,
                            onClick = { reason = quickReason },
                            label = { Text(quickReason, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Tuliskan Catatan Sanggahan") },
                    placeholder = { Text("Contoh: Nomor plat B 1234 tidak ada di log...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dispute_reason"),
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        errorText = "Alasan sanggahan wajib diisi!"
                    } else {
                        onConfirmDispute(reason.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_submit_dispute")
            ) {
                Text("Kirim Sanggahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
