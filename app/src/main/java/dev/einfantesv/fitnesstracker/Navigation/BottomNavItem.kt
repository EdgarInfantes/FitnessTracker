package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Profile : BottomNavItem("profile", Icons.Default.AccountBox, "Perfil")
    object Promotions : BottomNavItem("promotions", Icons.Default.Star, "Promociones")
}