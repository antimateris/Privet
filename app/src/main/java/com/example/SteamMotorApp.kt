package com.example

import android.app.Application
import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging

class SteamMotorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.initChannel(this)
        initializeFirebase()
        setupFirebaseMessaging()
    }

    private fun initializeFirebase() {
        try {
            val apps = FirebaseApp.getApps(this)
            if (apps.isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:507656640500:android:2f53f7fe6b8260fcae8ba1")
                    .setApiKey("AIzaSyCN7hfhHO6VZQttNc2TKc6aHls1ZjSr_GU")
                    .setProjectId("lion-steam-motor-e8eb3")
                    .setStorageBucket("lion-steam-motor-e8eb3.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.i(TAG, "Firebase initialized with fallback FirebaseOptions")
            } else {
                Log.i(TAG, "Firebase already initialized by Google Services provider")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase: ${e.message}", e)
        }
    }

    private fun setupFirebaseMessaging() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all_devices")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Subscribed to FCM topic: all_devices")
                    } else {
                        Log.w(TAG, "Failed to subscribe to FCM topic", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error initializing FCM: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SteamMotorApp"
    }
}
