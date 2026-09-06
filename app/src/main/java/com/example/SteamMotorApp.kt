package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SteamMotorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
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

    companion object {
        private const val TAG = "SteamMotorApp"
    }
}
