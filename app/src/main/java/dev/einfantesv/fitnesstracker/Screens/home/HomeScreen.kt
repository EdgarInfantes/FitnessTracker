package dev.einfantesv.fitnesstracker.Screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.einfantesv.fitnesstracker.Permissions.rememberRequestActivityRecognitionPermission
import dev.einfantesv.fitnesstracker.Screens.util.asyncImgPerfil
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavHostController,
    stepCounterViewModel: StepCounterViewModel,
    userSessionViewModel: UserSessionViewModel
) {
    val email by userSessionViewModel.userEmail.collectAsState(initial = null)
    val profileImageUrl by userSessionViewModel.profileImageUrl.collectAsState()
    var hasPermission by remember { mutableStateOf(false) }
    //var elapsedMinutes by remember { mutableStateOf(0) } Banco
    val elapsedMinutes by stepCounterViewModel.elapsedMinutes

    // Solicitud de permiso reutilizable
    val requestPermission = rememberRequestActivityRecognitionPermission { granted ->
        hasPermission = granted
        if (granted) {
            stepCounterViewModel.startListening()
        } else {
            stepCounterViewModel.stopListening()
        }
    }

    // Lanzar una vez al inicio
    LaunchedEffect(Unit) {
        requestPermission()
    }

    // Contador de minutos solo si el usuario camina
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            var lastStepCount = stepCounterViewModel.stepCount.value
            while (true) {
                delay(60_000) // Espera 1 minuto
                val currentStepCount = stepCounterViewModel.stepCount.value
                if (currentStepCount > lastStepCount) {
                    //elapsedMinutes++
                    lastStepCount = currentStepCount
                }
            }
        } else {
            //elapsedMinutes = 0
        }
    }

    fun formatElapsedTime(minutes: Int): String {
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            "${h}h ${m}m"
        } else {
            "${minutes} min"
        }
    }

    fun calculateCalories(steps: Int): Int {
        return (steps * 0.04).toInt()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de perfil
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            asyncImgPerfil(profileImageUrl, 80)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasPermission) {
                StepCircle(
                    permissionGranted = true,
                    stepCount = stepCounterViewModel.stepCount.value,
                    elapsedTime = formatElapsedTime(elapsedMinutes),
                    calories = calculateCalories(stepCounterViewModel.stepCount.value),
                    distance = 0
                )
            } else {
                StepCircle(
                    permissionGranted = false,
                    stepCount = 0,
                    elapsedTime = formatElapsedTime(elapsedMinutes),
                    calories = 0,
                    distance = 0
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { requestPermission() }) {
                    Text(text = "Activar permiso")
                }
            }
        }
    }
}


@Composable
fun StepCircle(
    permissionGranted: Boolean,
    stepCount: Int,
    elapsedTime: String,
    calories: Int,
    distance: Int = 0
) {
    Box(
        modifier = Modifier.size(325.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8f
            val diameter = size.minDimension
            val radius = diameter / 2 - strokeWidth / 2

            drawCircle(
                color = Color(0xFFF6F1F3),
                radius = radius,
                center = center
            )

            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            drawCircle(
                color = Color(0xFF7948DB),
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, pathEffect = pathEffect),
                center = center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.DirectionsWalk,
                contentDescription = "Persona caminando",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF7948DB)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (permissionGranted) {
                Text(
                    text = "$stepCount",
                    fontSize = 62.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7948DB)
                )
            } else {
                Text(
                    text = "0",
                    fontSize = 62.sp,
                    color = Color(0xFF7948DB),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "PASOS DIARIOS",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(icon = Icons.Filled.LocalFireDepartment, label = "$calories cal", color = Color(0xFFDF80ED))
                InfoItem(icon = Icons.Filled.AccessTime, label = elapsedTime, color = Color(0xFF2624A0))
                InfoItem(icon = Icons.Filled.LocationOn, label = "$distance km", color = Color(0xFFFE7331))
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentSize()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 14.sp, color = color)
    }
}