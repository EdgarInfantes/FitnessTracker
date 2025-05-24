package dev.einfantesv.fitnesstracker.Navigation

//Para la navegacion entre pantallas
sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
    object Register : Screens("register")
    object ForgotPassword : Screens("forgot_password")
}