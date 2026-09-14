package ai.behavioralwell.app

import android.app.Application
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.core.database.AppDatabase
import ai.behavioralwell.app.core.security.TokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BehavioralWellApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var tokenStorage: TokenStorage
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        tokenStorage = TokenStorage(this)
        ApiConfig.init(this)
        ApiConfig.resetToDefault()

        val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        appScope.launch {
            try {
                val req = ai.behavioralwell.app.data.models.DeviceRegistrationRequest(
                    deviceId = ai.behavioralwell.app.core.device.DeviceIdManager.getDeviceId(this@BehavioralWellApplication),
                    deviceModel = ai.behavioralwell.app.core.device.DeviceIdManager.getDeviceModel(),
                    androidVersion = ai.behavioralwell.app.core.device.DeviceIdManager.getAndroidVersion(),
                    appVersion = ai.behavioralwell.app.core.device.DeviceIdManager.getAppVersion()
                )
                ai.behavioralwell.app.core.network.RetrofitClient.apiService.registerDevice(req)
                android.util.Log.d("BehavioralWellApp", "Auto-registered deviceId=${req.deviceId} model=${req.deviceModel}")
            } catch (e: Exception) {
                android.util.Log.w("BehavioralWellApp", "Device auto-registration skipped or offline: ${e.message}")
            }
        }

        if (BuildConfig.DEBUG) {
            ai.behavioralwell.app.dev.LiveTelemetryDebugPublisher.startStreaming(this, appScope)
        }
    }

    companion object {
        lateinit var instance: BehavioralWellApplication
            private set
    }
}
