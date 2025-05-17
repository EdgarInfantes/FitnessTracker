package dev.einfantesv.fitnesstracker.Permissions

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest

@Composable
fun RequestActivityRecognitionPermission(
    onPermissionResult: (granted: Boolean) -> Unit
) {
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            permissionGranted = isGranted
            onPermissionResult(isGranted)
        }
    )

    LaunchedEffect(Unit) {
        permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Menor API no necesita permiso
        }

        if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            onPermissionResult(true)
        }
    }
}