package dev.einfantesv.fitnesstracker.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GoalStepScreen(onComplete: () -> Unit) {
    var customGoal by remember { mutableStateOf(6000) }
    var selectedGoal by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Objetivo de pasos diarios", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        val options = listOf(
            8000 to "Promedio de 7 días",
            8500 to "Realista",
            10000 to "Desafiante",
            13000 to "Ambicioso"
        )

        options.forEach { (steps, label) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedGoal = steps
                        customGoal = steps
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedGoal == steps) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("$steps pasos")
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Objetivo personalizado")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { if (customGoal > 0) customGoal -= 500 }) {
                Icon(Icons.Default.Remove, contentDescription = "Menos")
            }
            Text("$customGoal pasos", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { customGoal += 500 }) {
                Icon(Icons.Default.Add, contentDescription = "Más")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = { onComplete() }, modifier = Modifier.fillMaxWidth()) {
            Text("Continuar")
        }
    }
}
