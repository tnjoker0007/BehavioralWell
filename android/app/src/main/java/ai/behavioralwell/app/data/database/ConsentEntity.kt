package ai.behavioralwell.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_consent")
data class ConsentEntity(
    @PrimaryKey val userId: String,
    val keyboardEnabled: Boolean = true,
    val usageEnabled: Boolean = true,
    val motionEnabled: Boolean = true,
    val workEnabled: Boolean = true,
    val mobilityEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
