package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.einfantesv.fitnesstracker.HomeScreen
import dev.einfantesv.fitnesstracker.ProfileScreen
import dev.einfantesv.fitnesstracker.StepCounterViewModel

@Composable
fun HomeNavigation(mainNavController: NavHostController, stepCounterViewModel: StepCounterViewModel) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Promotions,
        BottomNavItem.Profile,
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
                HomeScreen(mainNavController, stepCounterViewModel)
            }
            composable(BottomNavItem.Profile.route) { ProfileScreen(mainNavController) }
            // Agregar más pantallas
        }
    }
}
