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
    var selectedGenero by remember { mutableStateOf("Femenino") }
    var selectedNacimiento by remember { mutableStateOf("2000") }
    var selectedPeso by remember { mutableStateOf("70 kg") }
    var selectedAltura by remember { mutableStateOf("170 cm") }

    val generos = listOf("Femenino", "Masculino", "Otro")
    val anios = (1950..2025).map { it.toString() }
    val pesos = (30..200).map { "$it kg" }
    val alturas = (100..220).map { "$it cm" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Configuración", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Para calcular con precisión pasos, distancia y calorías")

        Spacer(modifier = Modifier.height(16.dp))

        DropdownWithIcon("Género", generos, selectedGenero, { selectedGenero = it }, painterResource(R.drawable.mujer))
        DropdownWithIcon("Año de nacimiento", anios, selectedNacimiento, { selectedNacimiento = it }, painterResource(R.drawable.pastel))
        DropdownWithIcon("Peso", pesos, selectedPeso, { selectedPeso = it }, painterResource(R.drawable.bascula))
        DropdownWithIcon("Altura", alturas, selectedAltura, { selectedAltura = it }, painterResource(R.drawable.altura))

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onContinue() }, modifier = Modifier.fillMaxWidth()) {
            Text("Continuar")
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

