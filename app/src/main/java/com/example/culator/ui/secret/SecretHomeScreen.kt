package com.example.culator.ui.secret

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.culator.services.RecordingService
import com.example.culator.utils.RequestPermissions

@Composable
fun SecretHomeScreen(
    onNavigateToRecordings: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    RequestPermissions {
        permissionsGranted = true
    }

    if (!permissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permissions are required to use this feature.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Secret Dashboard", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                val intent = Intent(context, RecordingService::class.java).apply {
                    action = RecordingService.ACTION_START_VIDEO
                }
                context.startForegroundService(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Record Video (Hidden)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, RecordingService::class.java).apply {
                    action = RecordingService.ACTION_START_AUDIO
                }
                context.startForegroundService(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Record Audio (Hidden)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, RecordingService::class.java).apply {
                    action = RecordingService.ACTION_STOP
                }
                context.stopService(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Stop Recording")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToRecordings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "View Recordings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Settings")
        }
    }
}
