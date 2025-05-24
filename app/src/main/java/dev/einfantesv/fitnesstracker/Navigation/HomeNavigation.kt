package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.screens.ProfileScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.screens.DataUserScreen
import dev.einfantesv.fitnesstracker.screens.HomeScreen
import dev.einfantesv.fitnesstracker.screens.PrizesScreen

@Composable
fun HomeNavigation(mainNavController: NavHostController, stepCounterViewModel: StepCounterViewModel, userSessionViewModel: UserSessionViewModel) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Data,
        BottomNavItem.Prizes,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = { BottomBar(navController = bottomNavController, items = items) }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(mainNavController, stepCounterViewModel, userSessionViewModel)
            }
            composable(BottomNavItem.Data.route) { DataUserScreen(mainNavController) }
            composable(BottomNavItem.Prizes.route) { PrizesScreen(mainNavController) }
            composable(BottomNavItem.Profile.route) { ProfileScreen(mainNavController) }
            // Agregar más pantallas
        }
    }
}
