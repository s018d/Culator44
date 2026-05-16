package com.example.culator.ui.recordings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.culator.data.model.RecordingEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsListScreen(viewModel: RecordingsViewModel) {
    val recordings by viewModel.recordings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Recordings") }) }
    ) { padding ->
        if (recordings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recordings found.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(recordings) { recording ->
                    RecordingItem(
                        recording = recording,
                        onDelete = { viewModel.deleteRecording(recording) },
                        onPlay = { playRecording(context, recording) },
                        onShare = { shareRecording(context, recording) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingItem(
    recording: RecordingEntity,
    onDelete: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(recording.timestamp))
    val durationText = "${recording.duration / 60}:${String.format("%02d", recording.duration % 60)}"

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recording.fileName, style = MaterialTheme.typography.titleMedium)
                Text(text = "$dateString | ${recording.type}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Size: ${recording.fileSize / 1024} KB | Dur: $durationText", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onPlay) { Icon(Icons.Default.PlayArrow, "Play") }
                IconButton(onClick = onShare) { Icon(Icons.Default.Share, "Share") }
                IconButton(onClick = onDelete) { 
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) 
                }
            }
        }
    }
}

private fun playRecording(context: Context, recording: RecordingEntity) {
    val file = File(recording.filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "com.example.culator.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, if (recording.type == "VIDEO") "video/*" else "audio/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

private fun shareRecording(context: Context, recording: RecordingEntity) {
    val file = File(recording.filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "com.example.culator.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (recording.type == "VIDEO") "video/*" else "audio/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Recording"))
}
