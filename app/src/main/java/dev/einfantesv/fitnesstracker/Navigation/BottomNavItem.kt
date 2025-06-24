package dev.einfantesv.fitnesstracker.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Perfil")
    object Data : BottomNavItem("data", Icons.Default.BarChart, "Datos")
    object Prizes : BottomNavItem("prizes", Icons.Default.CardGiftcard, "Premios")
}