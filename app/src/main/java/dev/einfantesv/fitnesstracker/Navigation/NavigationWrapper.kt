package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.Screens.auth.DailyStepsAssignment
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.ForgotPasswordScreen
import dev.einfantesv.fitnesstracker.Screens.auth.LoginScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.PasswordChangedScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.ResetPasswordScreen
import dev.einfantesv.fitnesstracker.Screens.auth.resetPass.VerificationScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.Screens.auth.RegisterScreen
import dev.einfantesv.fitnesstracker.Screens.hidden.UserFriendCodeScreen
import dev.einfantesv.fitnesstracker.Screens.hidden.UserFullNameScreen

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
        composable("dailySteps") { navBackStackEntry ->
            val navControllerEntry = remember(navBackStackEntry) {
                navController.previousBackStackEntry
            }

            val savedStateHandle = navControllerEntry?.savedStateHandle

            val nombre = savedStateHandle?.get<String>("nombre") ?: ""
            val apellido = savedStateHandle?.get<String>("apellido") ?: ""
            val email = savedStateHandle?.get<String>("email") ?: ""
            val password = savedStateHandle?.get<String>("password") ?: ""

            DailyStepsAssignment(
                navController = navController,
                nombre = nombre,
                apellido = apellido,
                email = email,
                password = password
            )
        }
        composable("dailyStepsGoogle/{nombre}/{apellido}/{email}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val apellido = backStackEntry.arguments?.getString("apellido") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""

            DailyStepsAssignment(
                navController = navController,
                nombre = nombre,
                apellido = apellido,
                email = email,
                password = "" // ya no se necesita
            )
        }
        composable("userFriendCode") {
            UserFriendCodeScreen(navController)
        }
        composable("userFullName") {
            UserFullNameScreen(navController, userSessionViewModel)
        }

    }
}