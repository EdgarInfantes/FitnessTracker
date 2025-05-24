package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.Screens.ConfigurationScreen
import dev.einfantesv.fitnesstracker.Screens.GoalStepScreen
import dev.einfantesv.fitnesstracker.Screens.PermissionScreen
import dev.einfantesv.fitnesstracker.Screens.SplashScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel

@Composable
fun NavigationWrapper(stepCounterViewModel: StepCounterViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.Splash.route) {

        composable(Screens.Splash.route) {
            SplashScreen(onFinish = {
                navController.navigate(Screens.Permission.route) {
                    popUpTo(Screens.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screens.Permission.route) {
            PermissionScreen(onPermissionGranted = {
                navController.navigate(Screens.Configuration.route) {
                    popUpTo(Screens.Permission.route) { inclusive = true }
                }
            })
        }

        composable(Screens.Configuration.route) {
            ConfigurationScreen(onContinue = {
                navController.navigate(Screens.Goal.route) {
                    popUpTo(Screens.Configuration.route) { inclusive = true }
                }
            })
        }

        composable(Screens.Goal.route) {
            GoalStepScreen(onComplete = {
                navController.navigate(Screens.Home.route) {
                    popUpTo(Screens.Goal.route) { inclusive = true }
                }
            })
        }

        composable(Screens.Home.route) {
            HomeNavigation(navController, stepCounterViewModel)
        }
    }
}

