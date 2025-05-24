package dev.einfantesv.fitnesstracker.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.einfantesv.fitnesstracker.Permissions.RequestActivityRecognitionPermission

@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {
    var checked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Necesitamos permiso para contar tus pasos.")
            Spacer(modifier = Modifier.height(16.dp))

            RequestActivityRecognitionPermission { granted ->
                if (granted && !checked) {
                    checked = true
                    onPermissionGranted()
                }
            }

            if (!checked) {
                Text("Por favor acepta el permiso para continuar", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
