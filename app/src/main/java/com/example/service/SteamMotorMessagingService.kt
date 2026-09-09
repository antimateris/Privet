package com.example.service

import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service that receives push notifications from Google Play Services
 * even when the app process is closed, killed, or in the background.
 */
class SteamMotorMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Registration Token: $token")
        // Token can be used to send targeted notifications or subscribe to topics
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Check if message contains a notification payload (sent via Firebase Console / FCM API)
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Steam Motor"
            val body = notification.body ?: "Ada pembaruan data transaksi."
            NotificationHelper.showNotification(
                context = applicationContext,
                title = title,
                message = body
            )
            NotificationHelper.playChime(applicationContext)
            return
        }

        // 2. Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            val title = data["title"] ?: data["header"] ?: "Notifikasi Steam Motor"
            val message = data["message"] ?: data["body"] ?: data["content"] ?: "Ada transaksi atau informasi baru."
            NotificationHelper.showNotification(
                context = applicationContext,
                title = title,
                message = message
            )
            NotificationHelper.playChime(applicationContext)
        }
    }

    companion object {
        private const val TAG = "SteamMessagingService"
    }
}
