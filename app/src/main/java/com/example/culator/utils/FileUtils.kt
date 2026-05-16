package com.example.culator.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun createVideoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "VID_$timeStamp.mp4"
        val storageDir = File(context.filesDir, "secret_recordings")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, fileName)
    }

    fun createAudioFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUD_$timeStamp.m4a"
        val storageDir = File(context.filesDir, "secret_recordings")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, fileName)
    }
}
