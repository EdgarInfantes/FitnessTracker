package dev.einfantesv.fitnesstracker.Screens.auth.resetPass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.Screens.util.ButtonScreen
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.Screens.util.textDescripResetPass

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        //Boton y Texto: Olvidaste tu contraseña?
        Headers("¿Olvidaste tu contraseña?", navController, true)

        Spacer(modifier = Modifier.height(16.dp))

        textDescripResetPass(
            "Ingrese su dirección de correo electrónico a continuación" +
                    " y le enviaremos un código de verificación para " +
                    "ayudarlo a restablecer sus contraseñas."
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Ingresa tu correo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        //ButtonScreen(navController, "verification", "Enviar código")
        ActionButton("Enviar Código") {
            emailError = email.isBlank()
            if (!emailError){
                navController.navigate("verification")
            }else{
                emailError = true
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
