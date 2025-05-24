package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.Screens.LoginScreen
import dev.einfantesv.fitnesstracker.screens.RegisterScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel

@Composable
fun NavigationWrapper(stepCounterViewModel: StepCounterViewModel, userSessionViewModel: UserSessionViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Login.route) {
        composable(Screens.Login.route) {
            LoginScreen(navController, userSessionViewModel)
        }
        composable(Screens.Home.route) {
            HomeNavigation(navController, stepCounterViewModel, userSessionViewModel)
        }
        composable(Screens.SignIn.route) {
            RegisterScreen(navController)
        }
    }
}