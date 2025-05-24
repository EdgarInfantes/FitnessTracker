package dev.einfantesv.fitnesstracker.Navigation

//Para la navegacion entre pantallas
sealed class Screens(val route: String) {
    object Splash : Screens("splash")
    object Permission : Screens("permission")
    object Configuration : Screens("configuration")
    object Goal : Screens("goal")
    object Home : Screens("home")
}
