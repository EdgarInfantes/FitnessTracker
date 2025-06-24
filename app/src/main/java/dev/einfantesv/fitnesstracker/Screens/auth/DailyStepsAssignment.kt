package dev.einfantesv.fitnesstracker.Screens.auth

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DailyStepsAssignment(
    navController: NavController,
    nombre: String,
    apellido: String,
    email: String,
    password: String
) {
    val context = LocalContext.current
    val options = listOf(
        Pair("8000 pasos", "Promedio de 7 días"),
        Pair("8500 pasos", "Realista"),
        Pair("10000 pasos", "Desafiante"),
        Pair("13000 pasos", "Ambicioso")
    )
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var customGoal by remember { mutableIntStateOf(6000) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Objetivo de pasos diarios",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { (cantidad, descripcion) ->
            val colorDescripcion = when (descripcion) {
                "Promedio de 7 días" -> Color.Black
                "Realista" -> Color(0xFF4CAF50)
                "Desafiante" -> Color(0xFFFFC107)
                "Ambicioso" -> Color(0xFFF44336)
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = if (selectedOption == cantidad) Color(0xFF7948DB) else Color.Gray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedOption = cantidad }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = cantidad,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorDescripcion
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(
                    width = 2.dp,
                    color = if (selectedOption == "custom") Color(0xFF7948DB) else Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    selectedOption = "custom"
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Objetivo personalizado",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (customGoal > 1000) customGoal -= 100
                        selectedOption = "custom"
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFDDDDDD))
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = "Zapatilla",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%,d".format(customGoal),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                        Text(
                            text = "Pasos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = {
                        customGoal += 100
                        selectedOption = "custom"
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFDDDDDD))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ActionButton(
            label = "Registrarse",
            onClick = {
                val dailyGoal = when (selectedOption) {
                    "custom" -> customGoal
                    else -> selectedOption?.split(" ")?.getOrNull(0)?.toIntOrNull()
                }

                if (dailyGoal == null) {
                    Toast.makeText(context, "Selecciona un objetivo", Toast.LENGTH_SHORT).show()
                    return@ActionButton
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val result = FirebaseAuthManager.registerUser(
                        nombre.trim(),
                        apellido.trim(),
                        email.trim(),
                        password,
                        dailyGoal
                    )

                    if (result.isSuccess) {
                        navController.navigate("login") {
                            popUpTo("dailySteps") { inclusive = true }
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}
