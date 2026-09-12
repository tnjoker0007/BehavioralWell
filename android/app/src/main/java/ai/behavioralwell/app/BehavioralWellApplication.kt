package ai.behavioralwell.app

import android.app.Application
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.core.database.AppDatabase
import ai.behavioralwell.app.core.security.TokenStorage

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

        if (BuildConfig.DEBUG) {
            ai.behavioralwell.app.dev.LiveTelemetryDebugPublisher.startStreaming(
                this,
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
            )
        }
    }

    companion object {
        lateinit var instance: BehavioralWellApplication
            private set
    }
}
