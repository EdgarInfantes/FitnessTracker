package dev.einfantesv.fitnesstracker.Screens.hidden

import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.einfantesv.fitnesstracker.R
import kotlinx.coroutines.tasks.await

@Composable
fun UserFriendCodeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var friendCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Cargar el código de usuario actual
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val snapshot = firestore.collection("User").document(uid).get().await()
            friendCode = snapshot.getLong("UserFriendCode")?.toString() ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Atrás"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Mi código de usuario", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = friendCode,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = {
                val clip = ClipData.newPlainText("FriendCode", friendCode)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar código")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Insertar código de amigo", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = inputCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    inputCode = it
                }
            },
            placeholder = { Text("Ej. 123456") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val inputCodeInt = inputCode.toIntOrNull()
                if (inputCodeInt == null || inputCodeInt.toString().length != 6) {
                    message = "Código inválido. Debe tener 6 dígitos."
                    return@Button
                }

                if (inputCode == friendCode) {
                    message = "No puedes agregarte a ti mismo."
                    return@Button
                }

                val currentUid = user?.uid ?: return@Button

                // Paso 1: Buscar usuario con ese código
                firestore.collection("User")
                    .whereEqualTo("UserFriendCode", inputCodeInt)
                    .get()
                    .addOnSuccessListener { result ->
                        val matchedUser = result.documents.firstOrNull { it.id != currentUid }

                        if (matchedUser == null) {
                            message = "El código ingresado no existe."
                        } else {
                            val otherUid = matchedUser.id

                            // Paso 2: Verificar si ya existe una relación
                            firestore.collection("Friend_relation")
                                .whereIn("uid_one", listOf(currentUid, otherUid))
                                .get()
                                .addOnSuccessListener { relations ->
                                    val alreadyExists = relations.documents.any {
                                        val u1 = it.getString("uid_one")
                                        val u2 = it.getString("uid_second")
                                        (u1 == currentUid && u2 == otherUid) ||
                                                (u1 == otherUid && u2 == currentUid)
                                    }

                                    if (alreadyExists) {
                                        message = "Ya tienes agregado a este amigo."
                                    } else {
                                        // Paso 3: Crear relación
                                        val relation = hashMapOf(
                                            "uid_one" to currentUid,
                                            "uid_second" to otherUid
                                        )

                                        firestore.collection("Friend_relation")
                                            .add(relation)
                                            .addOnSuccessListener {
                                                message = "Amigo agregado exitosamente."
                                                inputCode = ""
                                            }
                                            .addOnFailureListener {
                                                message = "Error al agregar amigo."
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    message = "Error al validar relación previa."
                                }
                        }
                    }
                    .addOnFailureListener {
                        message = "Error al buscar el código."
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotBlank()) {
            Text(message, color = Color.DarkGray, fontSize = 16.sp)
        }
    }
}
