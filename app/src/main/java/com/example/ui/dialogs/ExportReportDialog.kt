package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.MonthlySummaryData
import com.example.ui.WashViewModel
import com.example.util.ExportUtils
import com.example.util.FormatUtils

enum class ExportPeriodChoice(val title: String) {
    BULAN_INI("Bulan Ini"),
    BULAN_TERPILIH("Bulan Rekap"),
    SEMUA_RIWAYAT("Semua Riwayat Database")
}

enum class ExportFormatChoice(val title: String, val desc: String) {
    CSV_EXCEL("File CSV (Excel)", "File .csv yang dapat dibuka di Google Sheets / Microsoft Excel"),
    TEXT_WHATSAPP("Teks Pesan WhatsApp", "Format rapi dengan emoji untuk dikirim langsung ke WhatsApp"),
    COPY_CLIPBOARD("Salin ke Clipboard", "Salin isi laporan ke papan klip ponsel")
}

enum class ExportDetailChoice(val title: String, val desc: String) {
    REKAP_HARIAN("Rekap Harian per Tanggal", "Total motor, omset kotor, bagi hasil petugas, kas pemilik"),
    DETAIL_PER_MOTOR("Rinci per Unit Transaksi", "ID, waktu, jam, plat motor, petugas cuci, metode bayar")
}

@Composable
fun ExportReportDialog(
    viewModel: WashViewModel,
    monthlySummary: MonthlySummaryData,
    onDismiss: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf(ExportPeriodChoice.BULAN_TERPILIH) }
    var selectedFormat by remember { mutableStateOf(ExportFormatChoice.CSV_EXCEL) }
    var selectedDetail by remember { mutableStateOf(ExportDetailChoice.REKAP_HARIAN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ekspor Laporan Keuangan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tarif 10rb • Bagi Hasil Petugas 5rb",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Pilih Periode Laporan
                Text(
                    text = "1. Pilih Periode Laporan:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportPeriodChoice.values().forEach { period ->
                        val labelText = if (period == ExportPeriodChoice.BULAN_TERPILIH) {
                            "${monthlySummary.monthName} ${monthlySummary.year}"
                        } else {
                            period.title
                        }
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(labelText, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                // 2. Pilih Tingkat Kerincian
                Text(
                    text = "2. Pilih Tingkat Kerincian Data:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportDetailChoice.values().forEach { choice ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedDetail == choice)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedDetail == choice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDetail = choice }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDetail == choice,
                                    onClick = { selectedDetail = choice }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = choice.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = choice.desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. Pilih Format Ekspor
                Text(
                    text = "3. Pilih Format Ekspor:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormatChoice.values().forEach { fmt ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFormat == fmt)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedFormat == fmt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFormat = fmt }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFormat == fmt,
                                    onClick = { selectedFormat = fmt }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fmt.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (fmt == ExportFormatChoice.CSV_EXCEL) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Rekomendasi",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = fmt.desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    executeExport(
                        context = context,
                        viewModel = viewModel,
                        monthlySummary = monthlySummary,
                        periodChoice = selectedPeriod,
                        formatChoice = selectedFormat,
                        detailChoice = selectedDetail,
                        onShowSnackbar = onShowSnackbar
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedFormat) {
                        ExportFormatChoice.CSV_EXCEL -> Color(0xFF15803D)
                        ExportFormatChoice.TEXT_WHATSAPP -> Color(0xFF25D366)
                        ExportFormatChoice.COPY_CLIPBOARD -> MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier.testTag("btn_confirm_export")
            ) {
                Icon(
                    imageVector = when (selectedFormat) {
                        ExportFormatChoice.CSV_EXCEL -> Icons.Default.TableChart
                        ExportFormatChoice.TEXT_WHATSAPP -> Icons.Default.Share
                        ExportFormatChoice.COPY_CLIPBOARD -> Icons.Default.ContentCopy
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (selectedFormat) {
                        ExportFormatChoice.CSV_EXCEL -> "Bagikan File CSV"
                        ExportFormatChoice.TEXT_WHATSAPP -> "Bagikan ke WhatsApp"
                        ExportFormatChoice.COPY_CLIPBOARD -> "Salin Laporan"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Batal")
            }
        }
    )
}

private fun executeExport(
    context: android.content.Context,
    viewModel: WashViewModel,
    monthlySummary: MonthlySummaryData,
    periodChoice: ExportPeriodChoice,
    formatChoice: ExportFormatChoice,
    detailChoice: ExportDetailChoice,
    onShowSnackbar: (String) -> Unit
) {
    when (formatChoice) {
        ExportFormatChoice.CSV_EXCEL -> {
            val csvContent: String
            val fileName: String
            if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                val records = when (periodChoice) {
                    ExportPeriodChoice.SEMUA_RIWAYAT -> viewModel.getAllRecordsList()
                    else -> viewModel.filteredRecords.value
                }
                csvContent = viewModel.buildDetailedCsvContent(records)
                fileName = "Laporan_Transaksi_Rinci_${monthlySummary.monthName}_${monthlySummary.year}.csv"
            } else {
                csvContent = viewModel.buildMonthlyCsvContent(monthlySummary)
                fileName = "Rekap_Harian_${monthlySummary.monthName}_${monthlySummary.year}.csv"
            }

            val success = ExportUtils.shareCsvFile(
                context = context,
                fileName = fileName,
                csvContent = csvContent,
                chooserTitle = "Bagikan File CSV Laporan Steam Motor"
            )
            if (success) {
                onShowSnackbar("Membuka aplikasi untuk menyimpan/membagikan CSV...")
            } else {
                onShowSnackbar("Gagal mengekspor file CSV.")
            }
        }

        ExportFormatChoice.TEXT_WHATSAPP -> {
            val text = viewModel.buildMonthlyReportWhatsAppText(monthlySummary)
            ExportUtils.shareText(
                context = context,
                text = text,
                chooserTitle = "Bagikan Rekap ke WhatsApp"
            )
        }

        ExportFormatChoice.COPY_CLIPBOARD -> {
            val text = if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                viewModel.buildDetailedCsvContent(viewModel.filteredRecords.value)
            } else {
                viewModel.buildMonthlyReportWhatsAppText(monthlySummary)
            }
            ExportUtils.copyToClipboard(context, "Laporan Steam Motor", text)
            onShowSnackbar("Laporan berhasil disalin ke clipboard!")
        }
    }
}
