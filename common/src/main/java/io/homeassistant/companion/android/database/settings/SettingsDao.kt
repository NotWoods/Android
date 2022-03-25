package io.homeassistant.companion.android.database.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Query("SELECT * FROM Settings WHERE id = :id")
    suspend fun get(id: Int): Setting?

    @Query("SELECT * FROM Settings WHERE id = :id")
    fun getSync(id: Int): Setting?

    @Query("SELECT * FROM Settings WHERE id = :id")
    fun getFlow(id: Int): Flow<Setting>
}
