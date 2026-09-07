package ai.behavioralwell.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ai.behavioralwell.app.data.database.ConsentDao
import ai.behavioralwell.app.data.database.ConsentEntity
import ai.behavioralwell.app.data.database.TelemetryDao
import ai.behavioralwell.app.data.database.TelemetryEntity

@Database(
    entities = [TelemetryEntity::class, ConsentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun telemetryDao(): TelemetryDao
    abstract fun consentDao(): ConsentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "behavioral_well_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
