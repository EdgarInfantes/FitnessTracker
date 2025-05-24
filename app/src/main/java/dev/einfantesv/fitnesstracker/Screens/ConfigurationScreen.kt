package dev.einfantesv.fitnesstracker.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.einfantesv.fitnesstracker.R


@Composable
fun ConfigurationScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    var selectedGenero by remember { mutableStateOf("Femenino") }
    var nacimiento by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }

    val generos = listOf("Femenino", "Masculino", "Otro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Configuración", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Para calcular con precisión pasos, distancia y calorías")

        Spacer(modifier = Modifier.height(16.dp))

        // Género con dropdown
        DropdownWithIcon("Género", generos, selectedGenero, { selectedGenero = it }, painterResource(R.drawable.mujer))

        // Año de nacimiento, peso y altura como input numérico
        InputWithIcon(
            label = "Año de nacimiento (1950 - 2025)",
            value = nacimiento,
            onValueChange = { nacimiento = it },
            icon = painterResource(R.drawable.pastel),
            range = 1950..2025,
            errorMessage = "Año inválido"
        )

        InputWithIcon(
            label = "Peso (30 - 200 kg)",
            value = peso,
            onValueChange = { peso = it },
            icon = painterResource(R.drawable.bascula),
            range = 30..200,
            errorMessage = "Peso inválido"
        )

        InputWithIcon(
            label = "Altura (100 - 220 cm)",
            value = altura,
            onValueChange = { altura = it },
            icon = painterResource(R.drawable.altura),
            range = 100..220,
            errorMessage = "Altura inválida"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val nacimientoVal = nacimiento.toIntOrNull()
            val pesoVal = peso.toIntOrNull()
            val alturaVal = altura.toIntOrNull()

            if (nacimientoVal !in 1950..2025 || pesoVal !in 30..200 || alturaVal !in 100..220) {
                Toast.makeText(context, "Revisa los campos ingresados", Toast.LENGTH_SHORT).show()
            } else {
                onContinue()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Continuar")
        }
    }
}

@Composable
fun InputWithIcon(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: Painter,
    range: IntRange,
    errorMessage: String
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        onValueChange(it)
                        val number = it.toIntOrNull()
                        showError = number != null && number !in range
                    }
                },
                label = { Text(label) },
                isError = showError,
                modifier = Modifier.fillMaxWidth()
            )
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DropdownWithIcon(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    icon: Painter
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedItem)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = {
                        onItemSelected(item)
                        expanded = false
                    })
                }
            }
        }
    }
}

