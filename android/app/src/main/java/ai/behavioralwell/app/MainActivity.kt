package ai.behavioralwell.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ai.behavioralwell.app.core.design.BehavioralWellTheme
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.data.services.TelemetryForegroundService
import ai.behavioralwell.app.navigation.MainNavigation

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: SensorPermissionManager

    private val runtimePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val activityRec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        } else true

        Log.d("MainActivityPermissions", "Permissions granted: location=${fineLocation || coarseLocation}, activity=$activityRec")

        checkUsageAccessPermission()
        startBackgroundTelemetryService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = SensorPermissionManager(this)

        requestAllPermissions()

        setContent {
            BehavioralWellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        runtimePermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun checkUsageAccessPermission() {
        if (!permissionManager.hasUsageAccessPermission()) {
            Toast.makeText(
                this,
                "BehavioralWell requires Usage Access to monitor app usage habits. Opening settings...",
                Toast.LENGTH_LONG
            ).show()
            try {
                permissionManager.openUsageAccessSettings()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to open usage access settings", e)
            }
        }
    }

    private fun startBackgroundTelemetryService() {
        try {
            TelemetryForegroundService.start(this)
            Log.d("MainActivity", "Started TelemetryForegroundService")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start TelemetryForegroundService", e)
        }
    }
}
