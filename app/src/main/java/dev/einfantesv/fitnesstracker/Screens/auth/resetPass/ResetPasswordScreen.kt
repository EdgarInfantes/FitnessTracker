package dev.einfantesv.fitnesstracker.Screens.auth.resetPass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.Screens.util.ButtonScreen
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.Screens.util.textDescripResetPass

@Composable
fun ResetPasswordScreen(navController: NavController) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var coincidenPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Headers("Crear Nueva Contraseña", navController, true)

        Spacer(modifier = Modifier.height(16.dp))

        textDescripResetPass(
            "Su nueva contraseña debe ser " +
                    "única respecto a las utilizadas anteriormente."
        )

        Spacer(modifier = Modifier.height(16.dp))

        //TextFiel para ingresar la contraseña
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        //TextFiel para confirmar la contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionButton("Restablecer contraseña") {
            newPasswordError = newPassword.isBlank()
            confirmPasswordError = confirmPassword.isBlank()
            coincidenPassword = newPassword != confirmPassword
            if (!newPasswordError && !confirmPasswordError && !coincidenPassword) {
                navController.navigate("password_changed")
            } else {
                newPasswordError = true
                confirmPasswordError = true
                coincidenPassword = false
            }

        }
    }
}