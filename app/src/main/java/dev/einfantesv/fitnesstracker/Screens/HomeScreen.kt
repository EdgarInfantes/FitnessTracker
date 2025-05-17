import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.einfantesv.fitnesstracker.Permissions.RequestActivityRecognitionPermission
import dev.einfantesv.fitnesstracker.StepCounterViewModel

@Composable
fun HomeScreen(navController: NavHostController, stepCounterViewModel: StepCounterViewModel) {
    var hasPermission by remember { mutableStateOf(false) }

    RequestActivityRecognitionPermission { granted ->
        hasPermission = granted
        if (granted) {
            stepCounterViewModel.startListening()
        } else {
            stepCounterViewModel.stopListening()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {
            Text(text = "Pasos hoy: ${stepCounterViewModel.stepCount.value}")
        } else {
            Text(text = "Necesitamos permiso para contar tus pasos.")
            //Boton para activar el permiso
            Button(onClick = {
                hasPermission = true
            }) {
                Text(text = "Activar permiso")
            }
        }
    }
}
