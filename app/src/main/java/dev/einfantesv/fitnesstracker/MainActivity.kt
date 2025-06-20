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

    // Fondo claro → íconos oscuros
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.White,
            darkIcons = true // <- Esto es clave para que los íconos se vean NEGROS
        )
    }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500) // Duración del splash: 1.5 segundos
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val startDestination = if (isUserLoggedIn) "home" else "login"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            NavigationWrapper(
                stepCounterViewModel = stepCounterViewModel,
                userSessionViewModel = userSessionViewModel,
                startDestination = startDestination
            )
        }
    }

}
