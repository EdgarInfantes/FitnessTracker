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
import com.google.firebase.auth.FirebaseAuth
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
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseAwards

@Composable
fun HomeScreen(
    navController: NavHostController,
    stepCounterViewModel: StepCounterViewModel = viewModel(),
    userSessionViewModel: UserSessionViewModel = viewModel()
) {
    val userData by userSessionViewModel.userData.collectAsState()
    var hasPermission by remember { mutableStateOf(false) }

    // Valores reactivos desde el ViewModel
    val stepCount by remember { derivedStateOf { stepCounterViewModel.stepCount } }
    val stepsToday by remember { derivedStateOf { stepCounterViewModel.stepsToday } }
    val elapsedMinutes by remember { derivedStateOf { stepCounterViewModel.elapsedMinutes } }
    val caloriesToday by remember { derivedStateOf { stepCounterViewModel.caloriesToday.toInt() } }
    val distanceToday by remember { derivedStateOf { stepCounterViewModel.distanceToday.toInt() } }

    val username = userData?.firstname ?: "Usuario"

    val profile = userData?.profileImageUrl
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    //val uid by userSessionViewModel.userUid.collectAsState() ?: ""
    //val uid by userSessionViewModel.userUid.collectAsState()

    LaunchedEffect(uid) {
        uid.let {
            stepCounterViewModel.onUserLogin(it)
        }

        FirebaseAwards.oneThousandStepsOneDay { unlocked ->
            if (unlocked) {
                println("Logro de 1000 pasos desbloqueado")
            }
        }

        FirebaseAwards.surpassMensualGoal { unlocked ->
            if (unlocked) {
                println("Meta mensual desbloqueada")
            }
        }

        FirebaseAwards.surpassDailyGoal { unlocked ->
            if (unlocked) {
                println("Meta diaria desbloqueada")
            }
        }

        FirebaseAwards.oneHundredMillionSteps { unlocked ->
            if (unlocked) {
                println("Logro de 100 millones de pasos desbloqueado")
            }
        }

        FirebaseAwards.fiveHundredStepsOneDay { unlocked ->
            if (unlocked) {
                println("Logro de 500 pasos desbloqueado")
            }
        }

        FirebaseAwards.tenThousandStepsOneWeek { unlocked ->
            if (unlocked) {
                println("Logro de 10000 pasos desbloqueado")
            }
        }

        FirebaseAwards.oneHundredStepsOneDay { unlocked ->
            if (unlocked) {
                println("Logro de 100 pasos desbloqueado")
            }
        }

        FirebaseAwards.oneMillionStepsInAMonth { unlocked ->
            if (unlocked) {
                println("Logro de 1 millón de pasos desbloqueado")
            }
        }

        FirebaseAwards.dailyGoalFiveDaysInARow { unlocked ->
            if (unlocked) {
                println("Logro de meta diaría 5 días seguidos desbloqueado")
            }
        }

        FirebaseAwards.makeAFriend { unlocked ->
            if (unlocked) {
                println("Logro de hacer un amigo desbloqueado")
            }
        }
    }

    val requestPermission = rememberRequestActivityRecognitionPermission { granted ->
        hasPermission = granted
        if (granted) stepCounterViewModel.startListening() else stepCounterViewModel.stopListening()
    }

    LaunchedEffect(Unit) {
        if (userSessionViewModel.userData.value == null) {
            userSessionViewModel.loadUserData()
        }
        requestPermission()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            var lastStepCount = stepCount
            while (true) {
                delay(5000)
                val currentStepCount = stepCounterViewModel.stepsToday

                if (currentStepCount > lastStepCount) {
                    FirebaseGetDataManager.saveStepsIfFirstWalkToday(
                        uid = uid,
                        steps = currentStepCount,
                        acTime = stepCounterViewModel.elapsedMinutes,
                        acCalories = stepCounterViewModel.caloriesToday,
                        acDistance = stepCounterViewModel.distanceToday
                    )
                    lastStepCount = currentStepCount
                }
            }
        }
    }

    fun formatElapsedTime(minutes: Double): String {
        val totalMinutes = minutes.toInt()
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${remainingMinutes}m" else "$remainingMinutes min"
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
        HomeHeader(username = username, profileImageUrl = profile, profileImageUri = null)

        if (hasPermission) {
            StepCircle(
                permissionGranted = true,
                stepCount = stepsToday,
                elapsedTime = formatElapsedTime(elapsedMinutes),
                calories = caloriesToday,
                distance = distanceToday
            )
        } else {
            StepCircle(
                permissionGranted = false,
                stepCount = 0,
                elapsedTime = formatElapsedTime(0.0),
                calories = 0,
                distance = 0
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { requestPermission() }) {
                Text(text = "Activar permiso")
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
        
        RankingAndAwardsSection(uid)

    }
}

@Composable
fun RankingAndAwardsSection(uid: String, rankingViewModel: RankingViewModel = viewModel()) {
    var user by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(uid) {
        FirebaseGetDataManager.getUserByUid(uid) {
            user = it
            if (it?.privacy == false) {
                rankingViewModel.loadFriendRanking()
            }
        }
    }

    user?.let {
        if (!it.privacy) {
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
                    rankingToday(rankingViewModel)
                }

                // Columna de Awards
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    awards(uid)
                }
            }
        } else {
            // Solo mostrar awards si la cuenta es privada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                awards(uid)
            }
        }
    }
}

@Composable
fun rankingToday(rankingViewModel: RankingViewModel = viewModel()) {
    val topUids by rankingViewModel.ranking.collectAsState()

    Column {
        Headers(label = "Ranking del día", color = Color(0xFF7948DB))

        Column {
            topUids.take(3).forEachIndexed { index, uid ->
                RankingUserCard(uid = uid, index = index)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun RankingUserCard(uid: String, index: Int) {
    var user by remember { mutableStateOf<UserModel?>(null) }
    var stepsToday by remember { mutableIntStateOf(0) }

    // Cargar datos del usuario
    LaunchedEffect(uid) {
        FirebaseGetDataManager.getUserByUid(uid) { fetchedUser ->
            user = fetchedUser
        }

        FirebaseGetDataManager.getTodaySteps(uid) { steps ->
            stepsToday = steps
        }
    }

    user?.let {
        val placeIcon = when (index) {
            0 -> R.drawable.ic_first
            1 -> R.drawable.ic_second
            2 -> R.drawable.ic_third
            else -> null
        }

        val firstNameOnly = it.firstname.split(" ").firstOrNull() ?: ""
        val lastInitial = it.lastname.split(" ").firstOrNull()?.firstOrNull()?.toString()?.uppercase() ?: ""

        RankingItem(
            name = "$firstNameOnly $lastInitial.",
            imageUrl = it.profileImageUrl,
            placeIcon = placeIcon,
            stepsToday = stepsToday
        )
    }
}

@Composable
fun RankingItem(name: String, imageUrl: String?, placeIcon: Int?, stepsToday: Int) {
    val backgroundColor = Color(0xFFF3EFFF)
    val textColor = Color(0xFF5C2D91)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(backgroundColor, shape = RoundedCornerShape(40.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        asyncImgPerfil(
            profileImageUrl = imageUrl,
            profileImageUri = null,
            selectedAvatarUrl = null,
            size = 38
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "$name ($stepsToday)",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1.5f)
        )

        Spacer(modifier = Modifier.width(2.dp)) // Espacio reducido

        placeIcon?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = "Ranking position icon",
                modifier = Modifier
                    .size(40.dp) // Aumentado el tamaño
                    .weight(0.6f),
                alignment = Alignment.CenterEnd
            )
        }
    }
}

@Composable
fun awards(uid: String) {
    var userAwards by remember { mutableStateOf<List<FirebaseAwards.Award>>(emptyList()) }

    LaunchedEffect(uid) {
        FirebaseAwards.getUnlockedAwardsForUser(uid) { awards ->
            userAwards = awards.take(4) // solo mostrar máximo 4
        }
    }

    Headers(label = "Premios", color = Color(0xFF7948DB))
    Spacer(modifier = Modifier.height(8.dp))

    val columns = 2
    val imageSize = 70.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in userAwards.chunked(columns)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                row.forEach { award ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = award.url,
                            contentDescription = "Premio desbloqueado",
                            modifier = Modifier
                                .size(imageSize)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                // Rellenar columnas vacías para mantener la uniformidad
                repeat(columns - row.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(username: String, profileImageUrl: String?, profileImageUri: Uri?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) { append("Hola, ") }
                withStyle(style = SpanStyle(color = Color(0xFF7948DB))) { append(username) }
            },
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp)
        ) {
            asyncImgPerfil(
                profileImageUrl = profileImageUrl,
                profileImageUri = profileImageUri,
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
    distance: Int
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
            Text(
                text = stepCount.toString(),
                fontSize = 62.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7948DB)
            )
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
