package dev.einfantesv.fitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.Navigation.NavigationWrapper
import dev.einfantesv.fitnesstracker.Screens.util.SplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    //no tocar
    private lateinit var stepCounterViewModel: StepCounterViewModel
    private lateinit var userSessionViewModel: UserSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepCounterViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[StepCounterViewModel::class.java]

        userSessionViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[UserSessionViewModel::class.java]

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            MainAppContent(stepCounterViewModel, userSessionViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounterViewModel.stopListening()
    }
}

@Composable
fun MainAppContent(
    stepCounterViewModel: StepCounterViewModel,
    userSessionViewModel: UserSessionViewModel
) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.White,
            darkIcons = true
        )
    }

    var showSplash by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Mostrar Splash al iniciar
    LaunchedEffect(Unit) {
        delay(1500) // Splash de 1.5 segundos
        showSplash = false
    }

    // Esperar a que FirebaseAuth se actualice antes de decidir la pantalla inicial
    LaunchedEffect(showSplash) {
        if (!showSplash) {
            delay(300) // Espera corta para garantizar que FirebaseAuth.currentUser no sea null si ya está logueado
            val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
            startDestination = if (isUserLoggedIn) "home" else "login"
        }
    }

    when {
        showSplash -> SplashScreen()

        startDestination != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                NavigationWrapper(
                    stepCounterViewModel = stepCounterViewModel,
                    userSessionViewModel = userSessionViewModel,
                    startDestination = startDestination!!
                )
            }
        }
    }
}
