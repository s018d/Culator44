package com.example.culator.ui.recordings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.culator.data.local.AppDatabase
import com.example.culator.data.model.RecordingEntity
import com.example.culator.data.repository.RecordingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class RecordingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecordingRepository

    val recordings: StateFlow<List<RecordingEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).recordingDao()
        repository = RecordingRepository(dao)
        recordings = repository.allRecordings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun deleteRecording(recording: RecordingEntity) {
        viewModelScope.launch {
            repository.delete(recording.id)
            val file = File(recording.filePath)
            if (file.exists()) file.delete()
        }
    }
}
