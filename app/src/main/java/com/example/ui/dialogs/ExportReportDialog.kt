package com.example.ui.dialogs

import android.content.Context
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.CompanyProfile
import com.example.ui.MonthlySummaryData
import com.example.ui.WashViewModel
import com.example.util.ExportUtils

enum class ExportPeriodChoice(val title: String) {
    BULAN_INI("Bulan Ini"),
    BULAN_TERPILIH("Bulan Rekap"),
    SEMUA_RIWAYAT("Semua Riwayat Database")
}

enum class ExportFormatChoice(val title: String, val desc: String, val badge: String? = null) {
    CORPORATE_PDF(
        "Cetak / Simpan PDF Resmi PT",
        "Kop surat resmi PT, nomor dokumen, tabel bergaris, watermark audit & lembar tanda tangan direksi",
        "Gaya PT Keren"
    ),
    CORPORATE_HTML(
        "Bagikan Dokumen HTML Resmi",
        "File dokumen berstandar korporat yang bisa dibuka di browser HP/PC atau dikirim ke WhatsApp",
        "Rekomendasi"
    ),
    CSV_EXCEL(
        "File CSV / Excel Korporat",
        "Buku besar berstandar akuntansi perusahaan, rekapitulasi kas tunai & QRIS",
        null
    ),
    TEXT_WHATSAPP(
        "Memorandum Resmi WhatsApp",
        "Format memo eksekutif dengan nomor surat resmi, rincian omset & pengesahan",
        null
    ),
    COPY_CLIPBOARD(
        "Salin ke Clipboard",
        "Salin seluruh isi laporan audit ke papan klip ponsel",
        null
    )
}

enum class ExportDetailChoice(val title: String, val desc: String) {
    REKAP_HARIAN("Buku Besar Harian per Tanggal", "Total volume motor, omset bruto, bagi hasil 50%, kas bersih PT"),
    DETAIL_PER_MOTOR("Rincian Transaksi per Unit Armada", "ID unit, tanggal, jam, plat nomor, operator cuci, metode bayar")
}

@Composable
fun ExportReportDialog(
    viewModel: WashViewModel,
    monthlySummary: MonthlySummaryData,
    onDismiss: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val companyProfile by viewModel.companyProfile.collectAsState()

    var selectedPeriod by remember { mutableStateOf(ExportPeriodChoice.BULAN_TERPILIH) }
    var selectedFormat by remember { mutableStateOf(ExportFormatChoice.CORPORATE_PDF) }
    var selectedDetail by remember { mutableStateOf(ExportDetailChoice.REKAP_HARIAN) }
    var showEditCompanyDialog by remember { mutableStateOf(false) }

    if (showEditCompanyDialog) {
        EditCompanyProfileDialog(
            currentProfile = companyProfile,
            onSave = { updated ->
                viewModel.updateCompanyProfile(updated)
                showEditCompanyDialog = false
                onShowSnackbar("Kop surat ${updated.companyName} berhasil disimpan!")
            },
            onDismiss = { showEditCompanyDialog = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Laporan Resmi Perusahaan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PT Style",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = companyProfile.companyName,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // KARTU KOP SURAT PERUSAHAAN (PT)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "KOP SURAT & LEGALITAS PT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "Ubah Data PT",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { showEditCompanyDialog = true }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = companyProfile.companyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${companyProfile.divisionName} • ${companyProfile.legalRegNo}",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Direktur: ${companyProfile.directorName} • Keuangan: ${companyProfile.financeManagerName}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // 1. Pilih Periode Laporan
                Text(
                    text = "1. Pilih Periode Dokumen:",
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
                            label = { Text(labelText, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                // 2. Pilih Tingkat Kerincian
                Text(
                    text = "2. Pilih Format Pembukuan:",
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
                    text = "3. Pilih Format Output Resmi:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormatChoice.values().forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
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
                                    selected = isSelected,
                                    onClick = { selectedFormat = fmt }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fmt.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (fmt.badge != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (fmt == ExportFormatChoice.CORPORATE_PDF) Color(0xFF0F172A) else Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = fmt.badge,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (fmt == ExportFormatChoice.CORPORATE_PDF) Color(0xFF38BDF8) else Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
                    executeCorporateExport(
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
                        ExportFormatChoice.CORPORATE_PDF -> Color(0xFF0F172A)
                        ExportFormatChoice.CORPORATE_HTML -> Color(0xFF0369A1)
                        ExportFormatChoice.CSV_EXCEL -> Color(0xFF15803D)
                        ExportFormatChoice.TEXT_WHATSAPP -> Color(0xFF25D366)
                        ExportFormatChoice.COPY_CLIPBOARD -> MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier.testTag("btn_confirm_export")
            ) {
                Icon(
                    imageVector = when (selectedFormat) {
                        ExportFormatChoice.CORPORATE_PDF -> Icons.Default.Print
                        ExportFormatChoice.CORPORATE_HTML -> Icons.Default.PictureAsPdf
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
                        ExportFormatChoice.CORPORATE_PDF -> "Cetak / Simpan PDF PT"
                        ExportFormatChoice.CORPORATE_HTML -> "Bagikan Dokumen PT"
                        ExportFormatChoice.CSV_EXCEL -> "Bagikan Buku Kas CSV"
                        ExportFormatChoice.TEXT_WHATSAPP -> "Kirim Memo ke WhatsApp"
                        ExportFormatChoice.COPY_CLIPBOARD -> "Salin Teks Resmi"
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

@Composable
fun EditCompanyProfileDialog(
    currentProfile: CompanyProfile,
    onSave: (CompanyProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var companyName by remember { mutableStateOf(currentProfile.companyName) }
    var divisionName by remember { mutableStateOf(currentProfile.divisionName) }
    var legalRegNo by remember { mutableStateOf(currentProfile.legalRegNo) }
    var companyAddress by remember { mutableStateOf(currentProfile.companyAddress) }
    var directorName by remember { mutableStateOf(currentProfile.directorName) }
    var financeManagerName by remember { mutableStateOf(currentProfile.financeManagerName) }
    var cashierName by remember { mutableStateOf(currentProfile.cashierName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sesuaikan Kop Surat PT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Informasi di bawah akan tercetak pada Kop Surat, Header Tabel, Watermark, dan Lembar Pengesahan Laporan:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nama PT / Badan Usaha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = divisionName,
                    onValueChange = { divisionName = it },
                    label = { Text("Nama Divisi / Bagian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = legalRegNo,
                    onValueChange = { legalRegNo = it },
                    label = { Text("Nomor Legalitas / NIB / AHU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyAddress,
                    onValueChange = { companyAddress = it },
                    label = { Text("Alamat Kantor / Lokasi Usaha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = directorName,
                    onValueChange = { directorName = it },
                    label = { Text("Nama & Gelar Direktur Utama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = financeManagerName,
                    onValueChange = { financeManagerName = it },
                    label = { Text("Nama & Gelar Manajer Keuangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cashierName,
                    onValueChange = { cashierName = it },
                    label = { Text("Nama Kasir / Admin Operasional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CompanyProfile(
                            companyName = companyName.ifBlank { "PT. KILAU MOTOR GEMILANG" },
                            divisionName = divisionName.ifBlank { "DIVISI OPERASIONAL & PERAWATAN KENDARAAN" },
                            legalRegNo = legalRegNo.ifBlank { "AHU-0038912.AH.01.01 / NIB: 9120003482190" },
                            companyAddress = companyAddress.ifBlank { "Kawasan Sentra Bisnis Otomotif Terpadu" },
                            directorName = directorName.ifBlank { "Bpk. Hendra Gunawan, S.E. (Direktur Utama)" },
                            financeManagerName = financeManagerName.ifBlank { "Ibu Siti Rahmawati, S.Ak. (Manajer Keuangan)" },
                            cashierName = cashierName.ifBlank { "Admin / Kasir Operasional" }
                        )
                    )
                }
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun executeCorporateExport(
    context: Context,
    viewModel: WashViewModel,
    monthlySummary: MonthlySummaryData,
    periodChoice: ExportPeriodChoice,
    formatChoice: ExportFormatChoice,
    detailChoice: ExportDetailChoice,
    onShowSnackbar: (String) -> Unit
) {
    val records = when (periodChoice) {
        ExportPeriodChoice.SEMUA_RIWAYAT -> viewModel.getAllRecordsList()
        else -> viewModel.filteredRecords.value
    }
    val periodTitle = when (periodChoice) {
        ExportPeriodChoice.BULAN_TERPILIH -> "${monthlySummary.monthName} ${monthlySummary.year}"
        ExportPeriodChoice.SEMUA_RIWAYAT -> "Semua Riwayat Database"
        ExportPeriodChoice.BULAN_INI -> "Bulan Ini"
    }

    when (formatChoice) {
        ExportFormatChoice.CORPORATE_PDF -> {
            val htmlContent: String
            val jobName: String
            if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                htmlContent = viewModel.buildCorporateDetailedHtml(records, periodTitle)
                jobName = "Laporan_Transaksi_Rinci_PT_${monthlySummary.monthName}_${monthlySummary.year}"
            } else {
                htmlContent = viewModel.buildCorporateMonthlyHtml(monthlySummary)
                jobName = "Laporan_Keuangan_PT_${monthlySummary.monthName}_${monthlySummary.year}"
            }

            val success = ExportUtils.printHtmlDocument(
                context = context,
                jobName = jobName,
                htmlContent = htmlContent
            )
            if (success) {
                onShowSnackbar("Membuka Layanan Cetak / Simpan PDF Resmi PT...")
            } else {
                onShowSnackbar("Gagal memproses dokumen cetak PDF.")
            }
        }

        ExportFormatChoice.CORPORATE_HTML -> {
            val htmlContent: String
            val fileName: String
            if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                htmlContent = viewModel.buildCorporateDetailedHtml(records, periodTitle)
                fileName = "Laporan_Resmi_PT_Transaksi_${monthlySummary.monthName}_${monthlySummary.year}.html"
            } else {
                htmlContent = viewModel.buildCorporateMonthlyHtml(monthlySummary)
                fileName = "Laporan_Keuangan_Resmi_PT_${monthlySummary.monthName}_${monthlySummary.year}.html"
            }

            val success = ExportUtils.shareHtmlFile(
                context = context,
                fileName = fileName,
                htmlContent = htmlContent,
                chooserTitle = "Bagikan Dokumen Laporan Resmi PT"
            )
            if (success) {
                onShowSnackbar("Membuka aplikasi untuk membagikan Dokumen Resmi PT...")
            } else {
                onShowSnackbar("Gagal mengekspor dokumen HTML.")
            }
        }

        ExportFormatChoice.CSV_EXCEL -> {
            val csvContent: String
            val fileName: String
            if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                csvContent = viewModel.buildCorporateDetailedCsv(records)
                fileName = "Buku_Besar_Transaksi_PT_${monthlySummary.monthName}_${monthlySummary.year}.csv"
            } else {
                csvContent = viewModel.buildCorporateMonthlyCsv(monthlySummary)
                fileName = "Buku_Kas_Bulanan_PT_${monthlySummary.monthName}_${monthlySummary.year}.csv"
            }

            val success = ExportUtils.shareCsvFile(
                context = context,
                fileName = fileName,
                csvContent = csvContent,
                chooserTitle = "Bagikan Buku Kas CSV Resmi PT"
            )
            if (success) {
                onShowSnackbar("Membuka aplikasi untuk menyimpan/membagikan CSV PT...")
            } else {
                onShowSnackbar("Gagal mengekspor file CSV.")
            }
        }

        ExportFormatChoice.TEXT_WHATSAPP -> {
            val text = viewModel.buildCorporateWhatsAppReport(monthlySummary)
            ExportUtils.shareText(
                context = context,
                text = text,
                chooserTitle = "Bagikan Memorandum Resmi ke WhatsApp"
            )
        }

        ExportFormatChoice.COPY_CLIPBOARD -> {
            val text = if (detailChoice == ExportDetailChoice.DETAIL_PER_MOTOR) {
                viewModel.buildCorporateDetailedCsv(records)
            } else {
                viewModel.buildCorporateWhatsAppReport(monthlySummary)
            }
            ExportUtils.copyToClipboard(context, "Laporan Resmi PT", text)
            onShowSnackbar("Laporan resmi PT berhasil disalin ke clipboard!")
        }
    }
}
