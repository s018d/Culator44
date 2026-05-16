package com.example.culator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val type: String, // "VIDEO" or "AUDIO"
    val timestamp: Long,
    val duration: Long,
    val fileSize: Long
)
