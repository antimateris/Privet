package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdateInfo
import com.example.ui.theme.AppleCardSurface
import com.example.ui.theme.AppleGroupedBackground
import com.example.ui.theme.AppleHairlineBorder
import com.example.ui.theme.AppleLabel
import com.example.ui.theme.AppleSecondaryLabel
import com.example.ui.theme.AppleSubtleFill
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemRed
import com.example.util.FormatUtils

/**
 * Dialog Pusat Pembaruan Aplikasi (OTA In-App Updates):
 * 1. Seluruh pengguna (Kasir, Owner, IT) dapat melihat versi terbaru, riwayat perubahan,
 *    dan langsung mengunduh APK pembaruan hanya dengan satu sentuhan tanpa perlu upload manual.
 * 2. Akun Team IT (dengan verifikasi password) dapat merilis versi baru, memasukkan URL APK,
 *    dan mengirimkan broadcast notifikasi instan ke seluruh ponsel perangkat.
 */
@Composable
fun AppUpdateCenterDialog(
    currentVersionName: String,
    currentVersionCode: Long,
    updateInfo: AppUpdateInfo,
    isItAccount: Boolean,
    onDismiss: () -> Unit,
    onDownloadApk: (downloadUrl: String) -> Unit,
    onPushNewVersion: (
        versionCode: Long,
        versionName: String,
        downloadUrl: String,
        releaseNotes: String,
        isForceUpdate: Boolean,
        fileSizeMb: String,
        itPassword: String,
        onComplete: (Boolean, String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(if (updateInfo.hasNewVersion(currentVersionCode)) 0 else 0) }

    // Form inputs for IT release
    var nextVersionName by remember {
        mutableStateOf(
            if (updateInfo.latestVersionCode > currentVersionCode) {
                updateInfo.latestVersionName
            } else {
                "1.${currentVersionCode}.0"
            }
        )
    }
    var nextVersionCode by remember {
        mutableStateOf((maxOf(currentVersionCode, updateInfo.latestVersionCode) + 1).toString())
    }
    var apkDownloadUrl by remember { mutableStateOf(updateInfo.downloadUrl) }
    var fileSizeText by remember { mutableStateOf(if (updateInfo.fileSizeMb.isNotBlank()) updateInfo.fileSizeMb else "18 MB") }
    var releaseNotesText by remember {
        mutableStateOf(
            if (updateInfo.releaseNotes.isNotBlank()) updateInfo.releaseNotes else
                "• Pembaruan performa dan stabilitas sinkronisasi data\n• Desain tampilan iOS 18 Cupertino Modern\n• Perbaikan bug sistem & cetak struk"
        )
    }
    var isForceUpdateCheck by remember { mutableStateOf(updateInfo.isForceUpdate) }
    var itPasswordInput by remember { mutableStateOf("") }
    var itPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackSuccess by remember { mutableStateOf(false) }

    val hasNewVersion = updateInfo.hasNewVersion(currentVersionCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header iOS 18 Style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleSystemBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pusat Pembaruan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppleLabel
                            )
                            Text(
                                text = "OTA In-App Update Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppleSecondaryLabel
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = AppleSecondaryLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tabs: [1. Status Update] [2. Rilis Baru (IT)]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AppleSubtleFill
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 0) AppleCardSurface else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Status Update",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) AppleSystemBlue else AppleSecondaryLabel
                                )
                                if (hasNewVersion) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AppleSystemRed)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) AppleCardSurface else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (selectedTab == 1) AppleSystemOrange else AppleSecondaryLabel
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rilis IT (Admin)",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) AppleSystemOrange else AppleSecondaryLabel
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Banner if any
                if (feedbackMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFeedbackSuccess) Color(0xFFE8F8ED) else Color(0xFFFFEBEA)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFeedbackSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isFeedbackSuccess) AppleSystemGreen else AppleSystemRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMessage ?: "",
                                fontSize = 13.sp,
                                color = if (isFeedbackSuccess) Color(0xFF1B6B32) else AppleSystemRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // TAB 0: STATUS UPDATE (FOR ALL USERS)
                if (selectedTab == 0) {
                    // Current installed info pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = AppleGroupedBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleHairlineBorder)
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
                                    text = "Versi Terpasang Saat Ini",
                                    fontSize = 11.sp,
                                    color = AppleSecondaryLabel
                                )
                                Text(
                                    text = "v$currentVersionName (Build $currentVersionCode)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleLabel
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (hasNewVersion) AppleSystemOrange.copy(alpha = 0.15f) else AppleSystemGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (hasNewVersion) "Update Tersedia" else "Sudah Terbaru",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasNewVersion) AppleSystemOrange else AppleSystemGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!hasNewVersion) {
                        // NO NEW VERSION AVAILABLE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppleCardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppleHairlineBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(AppleSystemGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AppleSystemGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Aplikasi Anda Sudah Terkini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppleLabel
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Anda menggunakan versi resmi Lion Steam terbaru (v$currentVersionName). Saat Team IT merilis versi baru, notifikasi pembaruan akan otomatis muncul di sini.",
                                    fontSize = 12.sp,
                                    color = AppleSecondaryLabel,
                                    lineHeight = 18.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // NEW UPDATE AVAILABLE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AppleCardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AppleSystemBlue)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(AppleSystemBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RocketLaunch,
                                                contentDescription = null,
                                                tint = AppleSystemBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Versi Baru: ${updateInfo.latestVersionName}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleLabel
                                            )
                                            Text(
                                                text = "Build ${updateInfo.latestVersionCode}${if (updateInfo.fileSizeMb.isNotBlank()) " • ${updateInfo.fileSizeMb}" else ""}",
                                                fontSize = 12.sp,
                                                color = AppleSecondaryLabel
                                            )
                                        }
                                    }

                                    if (updateInfo.isForceUpdate) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AppleSystemRed.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Wajib Update",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleSystemRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (updateInfo.releasedBy.isNotBlank() || updateInfo.releaseTimestamp > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val dateStr = if (updateInfo.releaseTimestamp > 0) {
                                        FormatUtils.formatDateFull(updateInfo.releaseTimestamp)
                                    } else "Hari ini"
                                    Text(
                                        text = "Dirilis oleh: ${updateInfo.releasedBy} • $dateStr",
                                        fontSize = 11.sp,
                                        color = AppleSecondaryLabel
                                    )
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = AppleHairlineBorder
                                )

                                // Release notes
                                Text(
                                    text = "Apa Saja yang Baru:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleLabel
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = AppleGroupedBackground
                                ) {
                                    Text(
                                        text = if (updateInfo.releaseNotes.isNotBlank()) updateInfo.releaseNotes else "• Peningkatan kestabilan & performa aplikasi",
                                        fontSize = 12.sp,
                                        color = AppleLabel,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // PRIMARY ACTION: DOWNLOAD & INSTALL APK
                                Button(
                                    onClick = {
                                        onDownloadApk(updateInfo.downloadUrl)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("download_apk_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppleSystemBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unduh & Pasang Pembaruan",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Secondary Action: Copy Link
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Link Update APK", updateInfo.downloadUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Tautan download berhasil disalin!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = AppleSecondaryLabel
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Salin Tautan Download APK",
                                        fontSize = 12.sp,
                                        color = AppleLabel
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: RILIS IT (PUSH APK BARU)
                if (selectedTab == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppleCardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleHairlineBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AppleSystemOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Otorisasi Rilis Versi Baru (IT Only)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleLabel
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Unggah file APK baru ke hosting/drive Anda, lalu masukkan linknya di sini. Saat Anda klik Push, seluruh perangkat kasir & pemilik akan langsung menerima notifikasi pembaruan.",
                                fontSize = 11.sp,
                                color = AppleSecondaryLabel,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Version Name & Version Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = nextVersionName,
                                    onValueChange = { nextVersionName = it },
                                    label = { Text("Nama Versi", fontSize = 12.sp) },
                                    placeholder = { Text("misal: 1.2.0") },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = nextVersionCode,
                                    onValueChange = { nextVersionCode = it.filter { c -> c.isDigit() } },
                                    label = { Text("Build Code", fontSize = 12.sp) },
                                    placeholder = { Text("misal: 3") },
                                    modifier = Modifier.weight(0.8f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Download URL
                            OutlinedTextField(
                                value = apkDownloadUrl,
                                onValueChange = { apkDownloadUrl = it },
                                label = { Text("Tautan/URL File APK Baru", fontSize = 12.sp) },
                                placeholder = { Text("https://drive.google.com/... atau link APK") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = AppleSecondaryLabel
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // File size
                            OutlinedTextField(
                                value = fileSizeText,
                                onValueChange = { fileSizeText = it },
                                label = { Text("Ukuran File (Opsional)", fontSize = 12.sp) },
                                placeholder = { Text("misal: 18 MB") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Release notes
                            OutlinedTextField(
                                value = releaseNotesText,
                                onValueChange = { releaseNotesText = it },
                                label = { Text("Catatan Perubahan (Changelog)", fontSize = 12.sp) },
                                placeholder = { Text("• Penambahan fitur baru...\n• Perbaikan bug...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 3,
                                maxLines = 5
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch: Force Update
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppleGroupedBackground)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Wajibkan Pembaruan (Force Update)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppleLabel
                                    )
                                    Text(
                                        text = "Mengharuskan pengguna mengupdate agar data sinkron",
                                        fontSize = 10.sp,
                                        color = AppleSecondaryLabel
                                    )
                                }
                                Switch(
                                    checked = isForceUpdateCheck,
                                    onCheckedChange = { isForceUpdateCheck = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AppleSystemOrange)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // IT Password Authentication Input
                            OutlinedTextField(
                                value = itPasswordInput,
                                onValueChange = { itPasswordInput = it },
                                label = { Text("Password Akun Team IT (Wajib)", fontSize = 12.sp) },
                                placeholder = { Text("Masukkan sandi IT...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (itPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { itPasswordVisible = !itPasswordVisible }) {
                                        Icon(
                                            imageVector = if (itPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = AppleSecondaryLabel
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // BUTTON: PUSH RELEASE TO ALL DEVICES
                            Button(
                                onClick = {
                                    val vCode = nextVersionCode.toLongOrNull() ?: 0L
                                    if (vCode <= 0) {
                                        feedbackMessage = "Kode versi harus berupa angka positif!"
                                        isFeedbackSuccess = false
                                        return@Button
                                    }
                                    if (apkDownloadUrl.isBlank()) {
                                        feedbackMessage = "Tautan/URL file APK wajib diisi!"
                                        isFeedbackSuccess = false
                                        return@Button
                                    }
                                    if (itPasswordInput.isBlank()) {
                                        feedbackMessage = "Password Team IT wajib diisi untuk otorisasi!"
                                        isFeedbackSuccess = false
                                        return@Button
                                    }

                                    isSubmitting = true
                                    feedbackMessage = null
                                    onPushNewVersion(
                                        vCode,
                                        nextVersionName,
                                        apkDownloadUrl,
                                        releaseNotesText,
                                        isForceUpdateCheck,
                                        fileSizeText,
                                        itPasswordInput
                                    ) { success, msg ->
                                        isSubmitting = false
                                        isFeedbackSuccess = success
                                        feedbackMessage = msg
                                        if (success) {
                                            itPasswordInput = ""
                                            selectedTab = 0
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("push_release_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppleSystemOrange),
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Push & Rilis ke Semua Orang",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
