package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.LoginScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel

@Composable
fun NavigationWrapper(stepCounterViewModel: StepCounterViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Home.route) {
        composable(Screens.Login.route) {
            LoginScreen(navController)
        }
        composable(Screens.Home.route) {
            HomeNavigation(navController, stepCounterViewModel)
        }
    }
}

