package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.screens.home.DataUserScreen
import dev.einfantesv.fitnesstracker.screens.home.HomeScreen
import dev.einfantesv.fitnesstracker.screens.home.PrizesScreen
import dev.einfantesv.fitnesstracker.screens.home.ProfileScreen

@Composable
fun HomeNavigation(mainNavController: NavHostController, stepCounterViewModel: StepCounterViewModel, userSessionViewModel: UserSessionViewModel) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem.Data,
        BottomNavItem.Home,
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
            composable(BottomNavItem.Data.route) { DataUserScreen(mainNavController, stepCounterViewModel) }
            composable(BottomNavItem.Home.route) {
                HomeScreen(mainNavController, stepCounterViewModel, userSessionViewModel)
            }
            composable(BottomNavItem.Prizes.route) { PrizesScreen(mainNavController) }
            composable(BottomNavItem.Profile.route) { ProfileScreen(mainNavController, userSessionViewModel) }
            // Agregar más pantallas
        }
    }
}