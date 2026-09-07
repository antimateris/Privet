package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MaintenanceStatusData
import com.example.ui.CompanyProfile

/**
 * Control Center khusus Tim IT & Support:
 * 1. Kontrol Mode Maintenance (ON/OFF & Pesan) - Khusus IT
 * 2. Tes Notifikasi Push Android secara langsung
 * 3. Kustomisasi Teks & Branding Perusahaan / Aplikasi
 */
@Composable
fun ItControlCenterDialog(
    maintenanceStatus: MaintenanceStatusData,
    companyProfile: CompanyProfile,
    onDismiss: () -> Unit,
    onSetMaintenance: (enabled: Boolean, message: String, itPassword: String) -> Pair<Boolean, String>,
    onPushBroadcast: (title: String, message: String, onComplete: (Boolean, String) -> Unit) -> Unit,
    onUpdateCompanyProfile: (companyName: String, divisionName: String, address: String, phone: String) -> Unit
) {
    val context = LocalContext.current

    // Maintenance State
    var maintenanceEnabled by remember { mutableStateOf(maintenanceStatus.enabled) }
    var maintenanceMessage by remember { mutableStateOf(maintenanceStatus.message) }
    var itPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var maintenanceFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Test Notification State
    var testNotifTitle by remember { mutableStateOf("Tes Notifikasi IT Support") }
    var testNotifMessage by remember { mutableStateOf("Sistem Push Notifikasi Lion Steam Motor berfungsi dengan baik!") }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    // Company / App Text State
    var compName by remember { mutableStateOf(companyProfile.companyName) }
    var divName by remember { mutableStateOf(companyProfile.divisionName) }
    var compAddress by remember { mutableStateOf(companyProfile.companyAddress) }
    var compPhone by remember { mutableStateOf(companyProfile.companyPhone) }
    var profileFeedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "IT Control & Dev Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Akses Khusus Tim IT / Developer",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6366F1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
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
                // Section 1: Maintenance Mode
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (maintenanceEnabled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = if (maintenanceEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mode Maintenance (Kunci Semua HP)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = maintenanceEnabled,
                                onCheckedChange = {
                                    maintenanceEnabled = it
                                    maintenanceFeedback = null
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.error,
                                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        }

                        Text(
                            text = if (maintenanceEnabled) "⚠️ Perangkat lain akan terkunci sampai dinonaktifkan."
                            else "Aplikasi berjalan normal di semua perangkat kasir & owner.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (maintenanceEnabled) {
                            OutlinedTextField(
                                value = maintenanceMessage,
                                onValueChange = { maintenanceMessage = it },
                                label = { Text("Pesan Maintenance untuk User") },
                                placeholder = { Text("Contoh: Pemeliharaan server sistem...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }

                        OutlinedTextField(
                            value = itPasswordInput,
                            onValueChange = {
                                itPasswordInput = it
                                maintenanceFeedback = null
                            },
                            label = { Text("Password Akun IT") },
                            placeholder = { Text("Masukkan password IT untuk konfirmasi") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6366F1))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        maintenanceFeedback?.let { (success, msg) ->
                            Surface(
                                color = if (success) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = if (success) Color(0xFF166534) else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (itPasswordInput.isBlank()) {
                                    maintenanceFeedback = Pair(false, "Masukkan password IT Support terlebih dahulu!")
                                    return@Button
                                }
                                val result = onSetMaintenance(maintenanceEnabled, maintenanceMessage, itPasswordInput)
                                maintenanceFeedback = result
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maintenanceEnabled) MaterialTheme.colorScheme.error else Color(0xFF6366F1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (maintenanceEnabled) "Kunci & Aktifkan Maintenance" else "Buka Kunci (Set Normal)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Section 2: Push Notification Testing
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Push Notifikasi ke Semua Perangkat",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Notifikasi ini akan ditembakkan ke SEMUA HP yang login (Kasir, Manager, Owner, IT lain) secara real-time, bukan cuma ke perangkat ini.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = testNotifTitle,
                            onValueChange = { testNotifTitle = it },
                            label = { Text("Judul Notifikasi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = testNotifMessage,
                            onValueChange = { testNotifMessage = it },
                            label = { Text("Isi Pesan Notifikasi") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        testFeedback?.let { msg ->
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                // pushBroadcastNotification already fires locally (with chime) on
                                // this device AND pushes to every other device on this branch.
                                onPushBroadcast(testNotifTitle, testNotifMessage) { success, resultMsg ->
                                    testFeedback = resultMsg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim ke Semua Perangkat", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section 3: App & Company Text Customization
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ubah Teks & Informasi Perusahaan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Kustomisasi nama usaha, divisi, alamat, dan nomor kontak yang tampil di seluruh laporan & struk.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = compName,
                            onValueChange = { compName = it },
                            label = { Text("Nama Usaha / PT") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = divName,
                            onValueChange = { divName = it },
                            label = { Text("Nama Divisi / Layanan") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = compAddress,
                            onValueChange = { compAddress = it },
                            label = { Text("Alamat Usaha") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = compPhone,
                            onValueChange = { compPhone = it },
                            label = { Text("Nomor Telepon / WA Usaha") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        profileFeedback?.let { msg ->
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onUpdateCompanyProfile(compName, divName, compAddress, compPhone)
                                profileFeedback = "Teks profil & informasi perusahaan berhasil diperbarui!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simpan Perubahan Teks", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tutup Panel IT")
            }
        }
    )
}
