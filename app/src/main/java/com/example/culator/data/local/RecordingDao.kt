package com.example.culator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.culator.data.model.RecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Insert
    suspend fun insertRecording(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)
}
