package dev.einfantesv.fitnesstracker.Screens.home

import android.content.Context
import android.graphics.Bitmap
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
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.Permissions.rememberRequestCameraPermission
import dev.einfantesv.fitnesstracker.Permissions.rememberRequestMediaPermissions
import dev.einfantesv.fitnesstracker.R
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.UserSessionViewModel
import dev.einfantesv.fitnesstracker.Screens.util.asyncImgPerfil
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userSessionViewModel: UserSessionViewModel
) {
    val context = LocalContext.current
    var showImageOptions by remember { mutableStateOf(false) }
    var showFullScreen by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPrivate by remember { mutableStateOf(false) }
    val profileImageUrl by userSessionViewModel.profileImageUrl.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { profileImageUri = it }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = saveBitmapToCache(context, it)
            profileImageUri = uri
        }
    }

    val requestGalleryPermission = rememberRequestMediaPermissions { granted ->
        if (granted) pickImageLauncher.launch("image/*")
    }

    val requestCameraPermission = rememberRequestCameraPermission { granted ->
        if (granted) takePictureLauncher.launch(null)
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
                profileImageBase64 = if (profileImageUri == null) profileImageUrl else "",
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
        ProfileOptionButton("Cambiar nombre") {}
        ProfileOptionButton("Cambiar contraseña") {}
        ProfileOptionButton("Cambiar correo") {}
        ProfileOptionButton("Cerrar sesión", R.drawable.baseline_logout_24, Color.Red) {
            userSessionViewModel.clearUserEmail()
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
            Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
        }

        Spacer(modifier = Modifier.height(32.dp))

        //Boton para confirmar los cambios
        Button(
            onClick = { /* Confirmar acci\u00f3n */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7948DB))
        ) {
            Text("Confirmar", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    //Opciones para editar la foto de perfil
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Edit") },
            text = {
                Column {
                    TextButton(onClick = {
                        showImageOptions = false
                        showFullScreen = true
                    }) { Text("Ver foto") }

                    TextButton(onClick = {
                        requestCameraPermission()
                        showImageOptions = false
                    }) { Text("Tomar foto") }

                    TextButton(onClick = {
                        requestGalleryPermission()
                        showImageOptions = false
                    }) { Text("Elegir desde galería") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Muestra la foto de perfil (Opcion Ver Foto)
    if (showFullScreen && (profileImageUri != null || profileImageUrl != null)) {
        AlertDialog(
            onDismissRequest = { showFullScreen = false },
            text = {
                AsyncImage(
                    model = profileImageUri ?: profileImageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

//Funcion para guardar la foto localmente
fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "profile_photo.png")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
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