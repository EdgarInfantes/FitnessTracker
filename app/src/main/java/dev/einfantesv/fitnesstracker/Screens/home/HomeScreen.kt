package dev.einfantesv.fitnesstracker.Screens.home

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.Permissions.rememberRequestActivityRecognitionPermission
import dev.einfantesv.fitnesstracker.R
import dev.einfantesv.fitnesstracker.RankingViewModel
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.Screens.util.asyncImgPerfil
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavHostController,
    stepCounterViewModel: StepCounterViewModel,
    userSessionViewModel: UserSessionViewModel
) {

    val userData by userSessionViewModel.userData.collectAsState()
    var hasPermission by remember { mutableStateOf(false) }
    val elapsedMinutes by stepCounterViewModel.elapsedMinutes
    val username = userData?.firstname ?: "Usuario"

    val profile = userData?.profileImageUrl


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(25.dp))
        //Texto de Hola Usuario


        HomeHeader(
            username = username,
            profileImageUrl = profile,
            profileImageUri = null
        )


        // Circulo de contar pasos
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

        Spacer(modifier = Modifier.height(50.dp))

        RankingAndAwardsSection()

    }
}

@Composable
fun RankingAndAwardsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Columna del Ranking
        Column(
            modifier = Modifier
                .weight(0.6f)
                .padding(end = 8.dp)
        ) {
            rankingToday()
        }

        // Columna de Awards
        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            awards("id")
        }
    }
}


@Composable
fun rankingToday(
    rankingViewModel: RankingViewModel = viewModel()
) {
    val topUids by rankingViewModel.ranking.collectAsState()

    Column {
        Headers(label = "Ranking del día", color = Color(0xFF7948DB))

        topUids.take(3).forEachIndexed { index, uid ->
            RankingUserCard(uid = uid, index = index)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun RankingUserCard(uid: String, index: Int) {
    var user by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(uid) {
        FirebaseGetDataManager.getUserByUid(uid) { fetchedUser ->
            user = fetchedUser
        }
    }

    user?.let {
        val placeIcon = when (index) {
            0 -> R.drawable.ic_first
            1 -> R.drawable.ic_second
            2 -> R.drawable.ic_third
            else -> null
        }

        RankingItem(
            name = "${it.firstname} ${it.lastname}",
            imageUrl = it.profileImageUrl,
            placeIcon = placeIcon
        )
    }
}
@Composable
fun RankingItem(name: String, imageUrl: String?, placeIcon: Int?) {
        val backgroundColor = Color(0xFFD1B4F8)
        val textColor = Color(0xFF5C2D91)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, shape = RoundedCornerShape(40.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "$name profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            placeIcon?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Ranking position icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
}

@Composable
fun awards(uid: String){
    Headers(label = "Premios", color = Color(0xFF7948DB))

    Spacer(modifier = Modifier.height(8.dp))

    // Seccion de premios
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

    }

    Spacer(modifier = Modifier.height(16.dp))

    // Botón de inventario
    Text(
        text = "-> Go and check your inventory",
        color = Color.Red,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun HomeHeader(username: String = "Edgar", profileImageUrl: String?, profileImageUri: Uri?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto combinado
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) { append("Hola, ") }
                withStyle(style = SpanStyle(color = Color(0xFF7948DB))) { append(username) }
            },
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold
        )

        // Imagen de perfil
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
        ) {
            asyncImgPerfil(
                profileImageUrl = profileImageUrl,
                profileImageUri = null,
                selectedAvatarUrl = null,
                size = 64
            )

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