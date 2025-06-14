package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.ForgotPasswordScreen
import dev.einfantesv.fitnesstracker.Screens.auth.LoginScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.PasswordChangedScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.ResetPasswordScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.VerificationScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.Screens.auth.RegisterScreen

@Composable
fun NavigationWrapper(
    stepCounterViewModel: StepCounterViewModel,
    userSessionViewModel: UserSessionViewModel,
    startDestination: String
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController, userSessionViewModel)
        }
        composable("home") {
            HomeNavigation(navController, stepCounterViewModel, userSessionViewModel)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(navController)
        }
        composable("verification") {
            VerificationScreen(navController)
        }
        composable("resetPassword") {
            ResetPasswordScreen(navController)
        }
        composable("passwordChanged") {
            PasswordChangedScreen(navController)
        }
    }
}