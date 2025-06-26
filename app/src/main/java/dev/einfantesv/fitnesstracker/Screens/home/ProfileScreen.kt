package dev.einfantesv.fitnesstracker.Screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.Permissions.rememberRequestMediaPermissions
import dev.einfantesv.fitnesstracker.R
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.Screens.util.asyncImgPerfil
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.clip
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.Screens.util.ActionButton
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseAuthManager.uploadProfileImage
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseUserManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.einfantesv.fitnesstracker.Screens.util.AnimatedSnackbar
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import kotlinx.coroutines.delay


@Composable
fun ProfileScreen(
    navController: NavHostController,
    userSessionViewModel: UserSessionViewModel
) {
    val context = LocalContext.current
    var showImageOptions by remember { mutableStateOf(false) }
    var showFullScreen by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val profileImageUrl by userSessionViewModel.profileImageUrl.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAvatarUrl by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var shouldDeleteAvatar by remember { mutableStateOf(false) }
    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color.Green) }
    val stepCounterViewModel: StepCounterViewModel = viewModel()
    val userData by userSessionViewModel.userData.collectAsState()
    val currentPrivacy = userData?.privacy ?: false
    var pendingPrivacyValue by remember { mutableStateOf(currentPrivacy) }


    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { profileImageUri = it }
    }

    val requestGalleryPermission = rememberRequestMediaPermissions { granted ->
        if (granted) pickImageLauncher.launch("image/*")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Headers("Administrar mi cuenta")
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.size(130.dp)) {

            asyncImgPerfil(
                profileImageUrl = profileImageUrl,
                profileImageUri = profileImageUri,
                selectedAvatarUrl = selectedAvatarUrl,
                shouldDeleteAvatar = shouldDeleteAvatar,
                size = 130
            )

            IconButton(
                onClick = { showImageOptions = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_24),
                    contentDescription = "Editar foto",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        ProfileOptionButton("Cambiar nombre y apellido") { navController.navigate("userFullName") }
        //ProfileOptionButton("Cambiar contraseña") {}
        //ProfileOptionButton("Cambiar correo") {}

        if (!currentPrivacy) {
            ProfileOptionButton("Agregar un nuevo amigo") { navController.navigate("userFriendCode") }
        }

        ProfileOptionButton("Cerrar sesión", R.drawable.baseline_logout_24, Color.Red) {
            userSessionViewModel.signOut(stepCounterViewModel)
            navController.navigate("login") {
                popUpTo(0) // Limpia el backstack
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cuenta Privada", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))

            Switch(
                checked = currentPrivacy,
                onCheckedChange = { newValue ->
                    pendingPrivacyValue = newValue
                    showPrivacyDialog = true
                }
            )
        }

// Diálogo de confirmación para cambio de privacidad
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Confirmar cambio de privacidad") },
                text = { Text("¿Estás seguro de modificar la privacidad de tu cuenta?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPrivacyDialog = false
                            uid?.let {
                                FirebaseUserManager.updatePrivacy(it, pendingPrivacyValue) { success ->
                                    if (success) {
                                        // Luego de actualizar la privacidad, actualiza también el estado de relaciones
                                        FirebaseUserManager.updateFriendRelationStates(it, !pendingPrivacyValue) { relationSuccess ->
                                            userSessionViewModel.loadUserData()
                                            snackbarMessage = if (relationSuccess) "Privacidad y relaciones actualizadas" else "Privacidad actualizada, pero ocurrió un error en relaciones"
                                            snackbarColor = if (relationSuccess) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                            snackbarVisible = true
                                        }
                                    } else {
                                        snackbarMessage = "Error al actualizar privacidad"
                                        snackbarColor = Color(0xFFF44336)
                                        snackbarVisible = true
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        //Boton para confirmar los cambios
        ActionButton(label = "Confirmar") {
            if (uid == null) return@ActionButton

            when {
                selectedImageUri != null -> {
                    uploadProfileImage(context, selectedImageUri!!) { success ->
                        if (success) {
                            Toast.makeText(context, "Imagen subida", Toast.LENGTH_SHORT).show()
                            userSessionViewModel.loadUserData()
                        } else {
                            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                selectedAvatarUrl != null -> {
                    FirebaseUserManager.updateProfileImageUrl(uid, selectedAvatarUrl!!) { success ->
                        if (success) {
                            snackbarMessage = "Cambios guardados correctamente"
                            snackbarColor = Color(0xFF4CAF50) // verde
                            userSessionViewModel.loadUserData()
                        } else {
                            snackbarMessage = "Error al guardar cambios"
                            snackbarColor = Color(0xFFF44336)
                        }
                        snackbarVisible = true

                    }

                }

                shouldDeleteAvatar -> {
                    FirebaseUserManager.deleteAvatar(uid) { success ->
                        if (success) {
                            snackbarMessage = "Cambios guardados correctamente"
                            snackbarColor = Color(0xFF4CAF50) // verde
                            userSessionViewModel.loadUserData()
                        } else {
                            snackbarMessage = "Error al guardar cambios"
                            snackbarColor = Color(0xFFF44336)
                        }
                        snackbarVisible = true
                    }
                }


            }

            // Resetear estado temporal
            selectedAvatarUrl = null
            selectedImageUri = null
            shouldDeleteAvatar = false
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Usamos el SnackBar
        AnimatedSnackbar(
            visible = snackbarVisible,
            message = snackbarMessage,
            backgroundColor = snackbarColor
        )

        //Cerramos el SnackBar
        LaunchedEffect(snackbarVisible) {
            if (snackbarVisible) {
                delay(2000)
                snackbarVisible = false
            }
        }

    }

    //Opciones para editar la foto de perfil
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Editar foto") },
            text = {
                Column {
                    TextButton(onClick = {
                        showImageOptions = false
                        showFullScreen = true
                    }) { Text("Ver Avatar") }

                    TextButton(onClick = {
                        showImageOptions = false
                        showAvatarPicker = true
                    }) { Text("Cambiar Avatar") }

                    TextButton(
                        onClick = {
                            selectedAvatarUrl = null
                            profileImageUri = null
                            shouldDeleteAvatar = true
                            showImageOptions = false
                        }
                    ) {
                        Text("Eliminar avatar", color = Color.Red)
                    }

                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
    //Ver Avatar
    if (showFullScreen && (profileImageUri != null || profileImageUrl != null || selectedAvatarUrl != null)) {
        AlertDialog(
            onDismissRequest = { showFullScreen = false },
            text = {
                AsyncImage(
                    model = selectedAvatarUrl ?: profileImageUri ?: profileImageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Muestra la foto de perfil (Opcion Ver Foto)
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Selecciona un avatar") },
            text = {
                AvatarPicker(
                    selectedAvatarUrl = selectedAvatarUrl,
                    onAvatarSelected = { url ->
                        selectedAvatarUrl = url
                        profileImageUri = null
                        showAvatarPicker = false
                    }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

}


//Funcion para opciones de edicion del perfil
@Composable
fun ProfileOptionButton(
    text: String,
    iconResId: Int? = null,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(70.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = textColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        color = textColor
                    )
                )
            }
            iconResId?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AvatarPicker(
    selectedAvatarUrl: String?,
    onAvatarSelected: (String) -> Unit
) {
    val avatarUrls = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        FirebaseGetDataManager.getAvatarUrls { urls ->
            avatarUrls.clear()
            avatarUrls.addAll(urls)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(avatarUrls) { url ->
            val isSelected = url == selectedAvatarUrl

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(
                        width = if (isSelected) 4.dp else 2.dp,
                        color = if (isSelected) Color(0xFF7948DB) else Color.LightGray,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable { onAvatarSelected(url) }
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
