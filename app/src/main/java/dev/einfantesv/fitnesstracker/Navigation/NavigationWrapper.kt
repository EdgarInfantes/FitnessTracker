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
import dev.einfantesv.fitnesstracker.screens.auth.RegisterScreen


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
        composable(Screens.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screens.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
        composable(Screens.Verification.route) {
            VerificationScreen(navController)
        }
        composable(Screens.ResetPassword.route) {
            ResetPasswordScreen(navController)
        }
        composable(Screens.PasswordChanged.route) {
            PasswordChangedScreen(navController)
        }

    }
}