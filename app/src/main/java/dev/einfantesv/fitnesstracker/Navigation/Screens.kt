package dev.einfantesv.fitnesstracker.Navigation

//Para la navegacion entre pantallas
sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
    object SignIn : Screens("register")
}