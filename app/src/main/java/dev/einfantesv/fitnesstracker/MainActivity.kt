package dev.einfantesv.fitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import dev.einfantesv.fitnesstracker.Navigation.NavigationWrapper
import dev.einfantesv.fitnesstracker.Screens.util.SplashScreen
import dev.einfantesv.fitnesstracker.UserSessionViewModel
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

        userSessionViewModel = ViewModelProvider(this)[UserSessionViewModel::class.java]

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var splashVisible by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(3000)
                splashVisible = false
                delay(500)
                showSplash = false
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = splashVisible,
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(durationMillis = 500)
                    ),
                    enter = fadeIn()
                ) {
                    SplashScreen()
                }

                if (!showSplash) {
                    // Pasa el ViewModel para que HomeScreen pueda usarlo
                    NavigationWrapper(stepCounterViewModel, userSessionViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounterViewModel.stopListening() // Puedes decidir si parar aquí o dentro de HomeScreen
    }
}
