package com.example.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TestPushDialog(
    senderName: String,
    onSendPush: (title: String, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Pengumuman Lion Steam") }
    var message by remember { mutableStateOf("Halo! Ini adalah tes push notifikasi ke semua perangkat.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickTemplates = listOf(
        "Tes Suara & Getar" to "Uji coba push notifikasi dan nada dering sistem berjalan lancar.",
        "Pesanan Baru" to "Transaksi cuci motor baru telah masuk ke sistem.",
        "Target Tercapai" to "Selamat! Target omset hari ini telah tercapai.",
        "Pengumuman Toko" to "Perhatian seluruh petugas, mohon periksa kelengkapan peralatan steam."
    )

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
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Test Push Notifikasi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kirim notifikasi ke semua perangkat",
                            style = MaterialTheme.typography.bodySmall,
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notifikasi ini akan dikirimkan secara real-time ke semua HP/tablet yang terpasang aplikasi Steam Motor ini (Pengirim: $senderName).",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Quick Templates
                Text(
                    text = "Pilih Template Cepat:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTemplates.forEach { (tmplTitle, tmplMsg) ->
                        SuggestionChip(
                            onClick = {
                                title = tmplTitle
                                message = tmplMsg
                                errorMessage = null
                            },
                            label = { Text(tmplTitle, fontSize = 11.sp) }
                        )
                    }
                }

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Judul Notifikasi *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_title_input")
                )

                // Message input
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        errorMessage = null
                    },
                    label = { Text("Pesan Notifikasi *") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_message_input")
                )

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        errorMessage = "Judul notifikasi tidak boleh kosong"
                        return@Button
                    }
                    if (message.trim().isBlank()) {
                        errorMessage = "Pesan notifikasi tidak boleh kosong"
                        return@Button
                    }
                    onSendPush(title.trim(), message.trim())
                },
                modifier = Modifier.testTag("send_push_confirm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim ke Semua")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_push_button")
            ) {
                Text("Batal")
            }
        }
    )
}
