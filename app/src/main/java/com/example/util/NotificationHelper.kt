package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

/**
 * Utility helper to show native Android push notifications and trigger audio/vibration feedback
 * when new motor wash transactions arrive, milestones are reached, or IT tests notifications.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "lion_steam_transactions"
    private const val CHANNEL_NAME = "Transaksi Steam Motor"
    private const val CHANNEL_DESC = "Notifikasi transaksi masuk, target harian, dan informasi sistem"

    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        initChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // If permission is not granted, play default notification sound as fallback feedback
                try {
                    val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, alert)
                    r.play()
                } catch (_: Exception) {
                }
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 200, 100, 200))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notificationId, builder.build())
        } catch (_: Exception) {
        }
    }

    /**
     * Plays the default device notification chime immediately (works even when notifications
     * might be muted in system shade or while in app).
     */
    fun playChime(context: Context) {
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, alert)
            r.play()
        } catch (_: Exception) {
        }
    }

    /**
     * Notifies about a newly registered motor wash transaction.
     */
    fun notifyNewTransaction(context: Context, record: com.example.data.model.WashRecord) {
        val plate = record.licensePlate.ifBlank { "Motor" }
        val title = "Transaksi Baru: $plate"
        val washer = if (record.washerName.isNotBlank()) " oleh ${record.washerName}" else ""
        val priceFmt = "Rp %,d".format(record.totalPrice).replace(',', '.')
        val message = "${record.motorCount} unit ($priceFmt)$washer • Metode: ${record.paymentMethod}"
        showNotification(context, title, message)
    }
}
