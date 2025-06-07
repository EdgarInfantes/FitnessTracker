package dev.einfantesv.fitnesstracker.Screens.auth.resetPass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.Screens.util.BackTextUtil
import dev.einfantesv.fitnesstracker.Screens.util.ButtonScreen
import dev.einfantesv.fitnesstracker.Screens.util.textDescripResetPass

@Composable
fun VerificationScreen(navController: NavController) {
    var code by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Boton y Texto Codigo de Verificacion
        BackTextUtil(navController, "Código de Verificación")

        Spacer(modifier = Modifier.height(16.dp))

        textDescripResetPass(
            "Introduzca el código de verificación " +
                    "enviamos a su dirección de correo electrónico.",
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    code = it
                }
            },
            label = { Text("Ingresa el código") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        ButtonScreen(navController, "reset_password", "Verificar")

        Spacer(modifier = Modifier.height(16.dp))
    }
}

