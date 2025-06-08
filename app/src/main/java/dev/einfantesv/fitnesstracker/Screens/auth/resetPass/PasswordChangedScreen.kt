package dev.einfantesv.fitnesstracker.Screens.auth.resetPass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.Screens.util.ButtonScreen

@Composable
fun PasswordChangedScreen(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = Color(0xFF4CAF50), // verde
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Contraseña Cambiada", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        ActionButton("Volver al login") {
            navController.navigate("login")
        }

    }
}