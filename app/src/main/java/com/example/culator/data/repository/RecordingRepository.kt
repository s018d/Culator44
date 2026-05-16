package com.example.culator.data.repository

import com.example.culator.data.local.RecordingDao
import com.example.culator.data.model.RecordingEntity
import kotlinx.coroutines.flow.Flow

class RecordingRepository(private val recordingDao: RecordingDao) {
    val allRecordings: Flow<List<RecordingEntity>> = recordingDao.getAllRecordings()

    suspend fun insert(recording: RecordingEntity) {
        recordingDao.insertRecording(recording)
    }

    suspend fun delete(id: Long) {
        recordingDao.deleteRecording(id)
    }
}
