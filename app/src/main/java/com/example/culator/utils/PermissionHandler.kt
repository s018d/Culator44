package com.example.culator.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionHandler {
    val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
            add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }
    }.toTypedArray()

    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun RequestPermissions(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            onPermissionsGranted()
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (!PermissionHandler.hasAllPermissions(context)) {
            launcher.launch(PermissionHandler.REQUIRED_PERMISSIONS)
        } else {
            onPermissionsGranted()
        }
    }
}
