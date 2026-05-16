package com.example.culator.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.culator.data.local.AppDatabase
import com.example.culator.data.model.RecordingEntity
import com.example.culator.data.repository.RecordingRepository
import com.example.culator.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordingService : LifecycleService() {

    private val CHANNEL_ID = "recording_channel"
    private val NOTIFICATION_ID = 1

    private var mediaRecorder: MediaRecorder? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    
    private var currentFile: File? = null
    private var recordingType: String = ""
    private var startTime: Long = 0

    private lateinit var repository: RecordingRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var cameraExecutor: ExecutorService

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()
            if (batteryPct < 5) {
                stopRecording()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        createNotificationChannel()
        val dao = AppDatabase.getDatabase(applicationContext).recordingDao()
        repository = RecordingRepository(dao)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action
        when (action) {
            ACTION_START_VIDEO -> startVideoRecording()
            ACTION_START_AUDIO -> startAudioRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startForegroundService(type: Int) {
        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Update")
            .setContentText("Checking for updates...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(0, "Cancel", stopPendingIntent) // Disguised Stop button
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startVideoRecording() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        } else 0
        
        startForegroundService(type)
        recordingType = "VIDEO"
        startTime = System.currentTimeMillis()
        currentFile = FileUtils.createVideoFile(this)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    videoCapture
                )
                startVideoCapture()
            } catch (e: Exception) {
                stopSelf()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startVideoCapture() {
        val fileOutputOptions = FileOutputOptions.Builder(currentFile!!).build()
        recording = videoCapture?.output
            ?.prepareRecording(this, fileOutputOptions)
            ?.start(ContextCompat.getMainExecutor(this)) { _ -> }
    }

    private fun startAudioRecording() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else 0

        startForegroundService(type)
        recordingType = "AUDIO"
        startTime = System.currentTimeMillis()
        currentFile = FileUtils.createAudioFile(this)

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try { stop() } catch (e: Exception) {}
            release()
        }
        mediaRecorder = null

        recording?.stop()
        recording = null

        saveRecordingToDb()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun saveRecordingToDb() {
        val file = currentFile ?: return
        val type = recordingType
        val duration = (System.currentTimeMillis() - startTime) / 1000

        serviceScope.launch {
            repository.insert(
                RecordingEntity(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    duration = duration,
                    fileSize = file.length()
                )
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Recording Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(batteryReceiver) } catch (e: Exception) {}
        cameraExecutor.shutdown()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        const val ACTION_START_VIDEO = "ACTION_START_VIDEO"
        const val ACTION_START_AUDIO = "ACTION_START_AUDIO"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
