package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ValidationState
import com.example.data.model.WashRecord
import com.example.util.ThermalReceiptUtils

@Composable
fun ThermalReceiptDialog(
    record: WashRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptText = remember(record) {
        ThermalReceiptUtils.generateReceiptText(record)
    }

    val validationState = record.getValidationState()
    val badgeColor = when (validationState) {
        ValidationState.VALID_APPROVED -> Color(0xFF16A34A)
        ValidationState.VALID_AUTO_24H -> Color(0xFF2563EB)
        ValidationState.PENDING_REVIEW -> Color(0xFFD97706)
        ValidationState.DISPUTED -> Color(0xFFDC2626)
    }
    val badgeText = when (validationState) {
        ValidationState.VALID_APPROVED -> "Status: Valid (Disetujui)"
        ValidationState.VALID_AUTO_24H -> "Status: Valid (Auto 24 Jam)"
        ValidationState.PENDING_REVIEW -> "Status: Menunggu Verifikasi (${record.getRemainingVerificationHours()} jam)"
        ValidationState.DISPUTED -> "Status: Disanggah Manager (${record.disputeReason})"
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
                        text = "Cetak Struk Thermal",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Validation badge
                Surface(
                    color = badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
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
                        ThermalReceiptUtils.shareToThermalPrinter(context, record)
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
                            ThermalReceiptUtils.printReceiptNative(context, record)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_thermal_native")
                    ) {
                        Text("Cetak WiFi/PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            ThermalReceiptUtils.copyReceiptToClipboard(context, record)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_receipt")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = null
    )
}
