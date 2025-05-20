package dev.einfantesv.fitnesstracker.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.einfantesv.fitnesstracker.R
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Password con boton para mostrar contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                val image = if (showPassword)
                    Icons.Default.Visibility
                else
                    Icons.Default.VisibilityOff //Cambiar

                val description = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))

        // Olvidaste contraseña
        Text(
            text = "¿Olvidaste tu contraseña?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    // Aquí va la acción para recuperar contraseña
                },
            color = Color(0xFF7948DB)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón iniciar sesión con cuenta Fitness Track
        Button(
            onClick = {navController.navigate("home")},
            modifier = Modifier.fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7948DB),
                contentColor = Color.White
            )
        ) {
            Text("Iniciar sesión",
                style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Registro de usuario
        Row {
            Text(text = "¿No tienes cuenta?")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Regístrate",
                color = Color(0xFF7948DB),
                modifier = Modifier.clickable {
                    // Acción ir a registro
                    navController.navigate("register")
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botones alternativos (Google y Apple)
        Text(text = "O", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    // Acción inicio con Google
                },
                //Fondo blanco y color 7948DB
                modifier = Modifier.weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                //Loogo de Google
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Login with Google",
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                //Color negro y negrita
                Text(
                    "Continuar con Google",
                    color = Color(0xFF000000),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
