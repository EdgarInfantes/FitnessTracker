package dev.einfantesv.fitnesstracker.Navigation

//Para la navegacion entre pantallas
sealed class Screens(val route: String) {

    //Pantalla del Login
    object Login : Screens("login")

    //Pantalla del Home
    object Home : Screens("home")

    //Pantalla para el registro
    object Register : Screens("register")

    //Pantallas para la contraseña olvidada
    object ForgotPassword : Screens("forgot_password")
    object Verification : Screens("verification")
    object ResetPassword : Screens("reset_password")
    object PasswordChanged : Screens("password_changed")
}