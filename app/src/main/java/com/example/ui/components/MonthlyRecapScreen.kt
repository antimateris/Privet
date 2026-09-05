package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MonthlyDayRecord
import com.example.ui.MonthlySummaryData
import com.example.ui.MonthlyWasherStat
import com.example.ui.WashViewModel
import com.example.util.ExportUtils
import com.example.util.FormatUtils
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun MonthlyRecapScreen(
    viewModel: WashViewModel,
    monthlySummary: MonthlySummaryData,
    onNavigateToDateInCashier: (Long) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentCalendar = remember { Calendar.getInstance() }
    val isCurrentActiveMonth = remember(monthlySummary.year, monthlySummary.month) {
        currentCalendar.get(Calendar.YEAR) == monthlySummary.year &&
                currentCalendar.get(Calendar.MONTH) == monthlySummary.month
    }

    var filterOnlyActiveDays by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        com.example.ui.dialogs.ExportReportDialog(
            viewModel = viewModel,
            monthlySummary = monthlySummary,
            onDismiss = { showExportDialog = false },
            onShowSnackbar = onShowSnackbar
        )
    }

    val displayedDays = remember(monthlySummary.dailyBreakdown, filterOnlyActiveDays) {
        if (filterOnlyActiveDays) {
            monthlySummary.dailyBreakdown.filter { it.motorCount > 0 }
        } else {
            monthlySummary.dailyBreakdown
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Month & Year Selector Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.prevMonthlyRecapMonth() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Bulan Sebelumnya",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${monthlySummary.monthName} ${monthlySummary.year}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrentActiveMonth) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Bulan Ini",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Laporan & Rekapitulasi Pembagian Hasil",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextMonthlyRecapMonth() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Bulan Berikutnya",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Quick Month Shortcuts (Current year months & Today button)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = isCurrentActiveMonth,
                                onClick = { viewModel.resetMonthlyRecapToCurrent() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Today,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("Bulan Ini (${currentCalendar.get(Calendar.YEAR)})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        // Year selector chips
                        val currentYear = currentCalendar.get(Calendar.YEAR)
                        listOf(currentYear, currentYear - 1, currentYear - 2).forEach { yr ->
                            item {
                                FilterChip(
                                    selected = monthlySummary.year == yr,
                                    onClick = { viewModel.setMonthlyRecapMonth(yr, monthlySummary.month) },
                                    label = { Text("Tahun $yr", fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Export Actions Hub Card (Corporate PT Style)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Laporan Resmi Perusahaan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Gaya PT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Kop surat PT, nomor dokumen, watermark audit & tanda tangan direksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Primary Hero Button: Cetak / Simpan PDF Resmi PT
                    Button(
                        onClick = {
                            val html = viewModel.buildCorporateMonthlyHtml(monthlySummary)
                            val jobName = "Laporan_Keuangan_PT_${monthlySummary.monthName}_${monthlySummary.year}"
                            val success = ExportUtils.printHtmlDocument(context, jobName, html)
                            if (success) {
                                onShowSnackbar("Membuka Layanan Cetak / Simpan PDF Resmi PT...")
                            } else {
                                onShowSnackbar("Gagal memproses dokumen cetak PDF.")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_corporate_pdf"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cetak / Simpan PDF Resmi PT (A4)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // Action buttons grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Dokumen HTML PT
                        Button(
                            onClick = {
                                val html = viewModel.buildCorporateMonthlyHtml(monthlySummary)
                                val fileName = "Laporan_Resmi_PT_${monthlySummary.monthName}_${monthlySummary.year}.html"
                                val success = ExportUtils.shareHtmlFile(
                                    context = context,
                                    fileName = fileName,
                                    htmlContent = html,
                                    chooserTitle = "Bagikan Dokumen Laporan PT"
                                )
                                if (success) {
                                    onShowSnackbar("Membuka pilihan ekspor dokumen PT...")
                                } else {
                                    onShowSnackbar("Gagal mengekspor dokumen HTML")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_html_monthly"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HTML PT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Button 2: Ekspor CSV (Excel)
                        Button(
                            onClick = {
                                val csvContent = viewModel.buildCorporateMonthlyCsv(monthlySummary)
                                val fileName = "Buku_Kas_Bulanan_PT_${monthlySummary.monthName}_${monthlySummary.year}.csv"
                                val success = ExportUtils.shareCsvFile(
                                    context = context,
                                    fileName = fileName,
                                    csvContent = csvContent,
                                    chooserTitle = "Bagikan Buku Kas CSV PT"
                                )
                                if (success) {
                                    onShowSnackbar("Membuka pilihan ekspor CSV PT...")
                                } else {
                                    onShowSnackbar("Gagal membuat file CSV")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_csv_monthly"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buku Kas CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Button 3: Bagikan WhatsApp Resmi
                        Button(
                            onClick = {
                                val waText = viewModel.buildCorporateWhatsAppReport(monthlySummary)
                                ExportUtils.shareText(
                                    context = context,
                                    text = waText,
                                    chooserTitle = "Bagikan Memorandum Resmi via WhatsApp"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share_wa_monthly"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Banner link to open the complete export modal & edit company profile
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExportDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Atur Kop Surat PT / Opsi Ekspor Lengkap",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Buka ▸",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 3. Hero Financial Summary Card (Tarif 10rb & Bagi Hasil 5rb)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Ringkasan Finansial ${monthlySummary.monthName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 4 Core Metric Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Unit
                        MonthlyMetricTile(
                            title = "Total Cuci",
                            value = "${monthlySummary.totalMotors} Unit",
                            subtitle = "Tarif 10rb / motor",
                            icon = Icons.Default.DirectionsBike,
                            iconColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        )

                        // Omset Kotor
                        MonthlyMetricTile(
                            title = "Omset Kotor",
                            value = FormatUtils.formatRupiah(monthlySummary.totalGrossRevenue),
                            subtitle = "100% penerimaan",
                            icon = Icons.Default.MonetizationOn,
                            iconColor = Color(0xFF0284C7),
                            containerColor = Color(0xFFE0F2FE),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bagi Hasil Pekerja
                        MonthlyMetricTile(
                            title = "Bagi Hasil Petugas",
                            value = FormatUtils.formatRupiah(monthlySummary.totalWasherShare),
                            subtitle = "Rp 5.000 / motor",
                            icon = Icons.Default.Handshake,
                            iconColor = Color(0xFFD97706),
                            containerColor = Color(0xFFFEF3C7),
                            modifier = Modifier.weight(1f)
                        )

                        // Kas Bersih Pemilik
                        MonthlyMetricTile(
                            title = "Kas Bersih Pemilik",
                            value = FormatUtils.formatRupiah(monthlySummary.totalOwnerShare),
                            subtitle = "Laba bersih steam (50%)",
                            icon = Icons.Default.Payments,
                            iconColor = Color(0xFF16A34A),
                            containerColor = Color(0xFFDCFCE7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Visual Split Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kas Bersih Usaha (50%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                            Text(
                                text = "Bagi Hasil Pekerja (50%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFFEF3C7))
                        ) {
                            val ownerFraction = if (monthlySummary.totalGrossRevenue > 0) {
                                (monthlySummary.totalOwnerShare.toFloat() / monthlySummary.totalGrossRevenue.toFloat()).coerceIn(0f, 1f)
                            } else 0.5f

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ownerFraction)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF16A34A))
                            )
                        }
                    }
                }
            }
        }

        // 4. Averages & Peak Insights Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rata-Rata & Hari Teramai",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InsightItem(
                            icon = Icons.Default.TrendingUp,
                            title = "Rata-rata Motor",
                            value = "${String.format(Locale.getDefault(), "%.1f", monthlySummary.averageDailyMotors)} unit/hari",
                            subtitle = "${monthlySummary.activeDaysCount} hari aktif buka",
                            modifier = Modifier.weight(1f)
                        )
                        InsightItem(
                            icon = Icons.Default.MonetizationOn,
                            title = "Rata-rata Kas Pemilik",
                            value = FormatUtils.formatRupiah(monthlySummary.averageDailyOwnerShare),
                            subtitle = "Laba bersih per hari",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (monthlySummary.peakDay != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hari Teramai Bulan Ini:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${monthlySummary.peakDay.dayName}, ${monthlySummary.peakDay.dayOfMonth} ${monthlySummary.monthName} • ${monthlySummary.peakDay.motorCount} Motor (${FormatUtils.formatRupiah(monthlySummary.peakDay.grossRevenue)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Worker Share & Ranking Breakdown for the Month
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Komisi Petugas Cuci Bulan Ini",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${monthlySummary.washerStats.size} Petugas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (monthlySummary.washerStats.isEmpty()) {
                        Text(
                            text = "Belum ada transaksi di bulan ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        monthlySummary.washerStats.forEachIndexed { index, washer ->
                            MonthlyWasherRow(
                                rank = index + 1,
                                stat = washer
                            )
                            if (index < monthlySummary.washerStats.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Payment Methods Breakdown (Tunai vs QRIS / Transfer)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Metode Pembayaran",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val totalPay = (monthlySummary.cashAmount + monthlySummary.qrisAmount + monthlySummary.transferAmount).coerceAtLeast(1L)
                    val cashPct = (monthlySummary.cashAmount.toFloat() / totalPay.toFloat()) * 100f
                    val qrisPct = (monthlySummary.qrisAmount.toFloat() / totalPay.toFloat()) * 100f
                    val trfPct = (monthlySummary.transferAmount.toFloat() / totalPay.toFloat()) * 100f

                    PaymentItem(
                        label = "Tunai (Cash)",
                        amount = monthlySummary.cashAmount,
                        percentage = cashPct,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PaymentItem(
                        label = "QRIS",
                        amount = monthlySummary.qrisAmount,
                        percentage = qrisPct,
                        color = Color(0xFF0D9488)
                    )
                    if (monthlySummary.transferAmount > 0) {
                        PaymentItem(
                            label = "Transfer Bank",
                            amount = monthlySummary.transferAmount,
                            percentage = trfPct,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }
        }

        // 7. Complete Daily Breakdown List for the Month
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rincian Tanggal 1 - ${monthlySummary.daysInMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ketuk tanggal untuk melihat rincian di Kasir",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilterChip(
                    selected = filterOnlyActiveDays,
                    onClick = { filterOnlyActiveDays = !filterOnlyActiveDays },
                    label = {
                        Text(
                            text = if (filterOnlyActiveDays) "Hari Aktif (${monthlySummary.activeDaysCount})" else "Semua Hari",
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Daily records items
        if (displayedDays.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada transaksi pada bulan ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = displayedDays,
                key = { it.dateKey },
                contentType = { "daily_row" }
            ) { dayRecord ->
                DailyBreakdownRowCard(
                    day = dayRecord,
                    monthName = monthlySummary.monthName,
                    onClick = {
                        onNavigateToDateInCashier(dayRecord.dateMillis)
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthlyMetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightItem(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthlyWasherRow(
    rank: Int,
    stat: MonthlyWasherStat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFEF3C7)
                            2 -> Color(0xFFF1F5F9)
                            3 -> Color(0xFFFFEDD5)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFFB45309)
                        2 -> Color(0xFF475569)
                        3 -> Color(0xFFC2410C)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stat.washerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stat.motorCount} unit dicuci • ${String.format(Locale.getDefault(), "%.0f", stat.percentage)}% dari total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = FormatUtils.formatRupiah(stat.totalShare),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD97706)
            )
            Text(
                text = "Hak Bagi Hasil",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentItem(
    label: String,
    amount: Long,
    percentage: Float,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${FormatUtils.formatRupiah(amount)} (${String.format(Locale.getDefault(), "%.0f", percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun DailyBreakdownRowCard(
    day: MonthlyDayRecord,
    monthName: String,
    onClick: () -> Unit
) {
    val isActive = day.motorCount > 0
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Date & Day name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d", day.dayOfMonth),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isActive && day.topWasherName.isNotBlank()) {
                        Text(
                            text = "Petugas: ${day.topWasherName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (!isActive) {
                        Text(
                            text = "Libur / Tidak ada transaksi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Right: Motor count & Revenue
            if (isActive) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${day.motorCount} Motor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = FormatUtils.formatRupiah(day.grossRevenue),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Kas Pemilik: ${FormatUtils.formatRupiah(day.ownerShare)}",
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
