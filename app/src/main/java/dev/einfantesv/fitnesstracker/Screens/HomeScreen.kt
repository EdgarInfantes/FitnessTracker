package dev.einfantesv.fitnesstracker.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.Permissions.RequestActivityRecognitionPermission
import dev.einfantesv.fitnesstracker.R
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavHostController, stepCounterViewModel: StepCounterViewModel, userSessionViewModel: UserSessionViewModel) {
    var hasPermission by remember { mutableStateOf(false) }
    var elapsedMinutes by remember { mutableStateOf(0) }
    val email by userSessionViewModel.userEmail.collectAsState(initial = null)

    val profileImageUrl = when (email.orEmpty()) {
        "dirtyyr2012@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic.wikia.nocookie.net%2Fficcion-sin-limites%2Fimages%2F5%2F50%2FJOEL.webp%2Frevision%2Flatest%2Fscale-to-width-down%2F1200%3Fcb%3D20220416041602%26path-prefix%3Des&f=1&nofb=1&ipt=cb58cd4c3f6a55df0864d5ff84ba4f4528e0ba1c62873aa897c036e63e0e281a"
        "melva.66.2002@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic.wikia.nocookie.net%2Falfondohaysitio%2Fimages%2F8%2F8d%2FTeresa_(AFHS10).png%2Frevision%2Flatest%2Fscale-to-width-down%2F1200%3Fcb%3D20230512183136%26path-prefix%3Des&f=1&nofb=1&ipt=43dcb505b33a1ebc70b10d28d3e315e45dc6d940f60e2b1aac71c218b83c0687"
        "alxmeza63@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.pinimg.com%2Foriginals%2F76%2Fe7%2F48%2F76e7484504c6ae8efd1a4df5cdd282f5.jpg&f=1&nofb=1&ipt=1859c5713cf98d3d0fd2f04e4551aeab46c59786d68af1624dc37df079439b94"
        else -> ""
    }

    // Solicitar permiso solo una vez (o cuando cambia hasPermission)
    RequestActivityRecognitionPermission { granted ->
        hasPermission = granted
        if (granted) {
            stepCounterViewModel.startListening()
        } else {
            stepCounterViewModel.stopListening()
        }
    }


    // Contador de tiempo activo (minutos)
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            while (true) {
                delay(60_000) // 1 minuto
                elapsedMinutes++
            }
        } else {
            elapsedMinutes = 0
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
        // Imagen perfil arriba a la derecha con fondo blanco y borde negro redondo
        if (profileImageUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White, CircleShape) // fondo blanco circular
                    .border(2.dp, Color.Black, CircleShape) // borde negro circular
                    .clip(CircleShape) // recorte circular
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Column intacto, sin cambios
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
                // Aquí ranking u otros elementos
            } else {
                StepCircle(
                    permissionGranted = false,
                    stepCount = 0,
                    elapsedTime = formatElapsedTime(elapsedMinutes),
                    calories = 0,
                    distance = 0
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { hasPermission = true }) {
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