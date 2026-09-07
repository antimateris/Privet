package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MaintenanceLockScreen
import com.example.ui.WashDashboardScreen
import com.example.ui.WashViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: WashViewModel = viewModel()
        val maintenanceStatus by viewModel.maintenanceStatus.collectAsStateWithLifecycle()

        Crossfade(targetState = maintenanceStatus.enabled, label = "maintenance_lock") { isLocked ->
          if (isLocked) {
            MaintenanceLockScreen(
              status = maintenanceStatus,
              onUnlock = { itPassword ->
                viewModel.setMaintenanceMode(
                  enabled = false,
                  message = maintenanceStatus.message,
                  itPassword = itPassword
                )
              }
            )
          } else {
            WashDashboardScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}

