package ai.behavioralwell.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsentDao {

    @Query("SELECT * FROM user_consent WHERE userId = :userId LIMIT 1")
    fun getConsent(userId: String): Flow<ConsentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConsent(consent: ConsentEntity)
}
