package dev.einfantesv.fitnesstracker.Screens.hidden

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.R
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseUserManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFullNameScreen(
    navController: NavHostController,
    userSessionViewModel: UserSessionViewModel
) {
    val userData by userSessionViewModel.userData.collectAsState()
    val context = LocalContext.current

    var firstnameText by remember { mutableStateOf("") }
    var lastnameText by remember { mutableStateOf("") }

    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color.Green) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Nombre") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Nuevo nombre(s)", fontSize = 16.sp)
            OutlinedTextField(
                value = firstnameText,
                onValueChange = { firstnameText = it },
                placeholder = { Text(userData?.firstname ?: "Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Nuevo apellido(s)", fontSize = 16.sp)
            OutlinedTextField(
                value = lastnameText,
                onValueChange = { lastnameText = it },
                placeholder = { Text(userData?.lastname ?: "Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            ActionButton(label = "Guardar Cambios") {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    FirebaseUserManager.updateName(uid, firstnameText, lastnameText) { success ->
                        if (success) {
                            userSessionViewModel.refreshUserData()
                            snackbarMessage = "Nombre actualizado correctamente"
                            snackbarColor = Color(0xFF4CAF50)
                        } else {
                            snackbarMessage = "Error al actualizar nombre"
                            snackbarColor = Color(0xFFF44336)
                        }
                        snackbarVisible = true
                    }
                }
            }

            if (snackbarVisible) {
                Snackbar(
                    containerColor = snackbarColor,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text(text = snackbarMessage)
                }
            }

            LaunchedEffect(snackbarVisible) {
                if (snackbarVisible) {
                    delay(2000)
                    snackbarVisible = false
                }
            }
        }
    }
}
