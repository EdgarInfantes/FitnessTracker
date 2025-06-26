package dev.einfantesv.fitnesstracker.Screens.util

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.R

@Composable
fun ButtonScreen(navController: NavController, screen: String, label : String){
    /**
     * Composable reutilizable para los botones.
     *
     *
     * @param navController Controlador de Navegacion.
     * @param screen Nombre de la pantalla a redirigir.
     * @param label Texto del boton.
     */
    Button(
        onClick = {
            navController.navigate(screen)
        },
        modifier = Modifier.fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7948DB),
            contentColor = Color.White
        )
    ) {
        Text(label,
            style = MaterialTheme
                .typography
                .titleMedium.copy(fontSize = 18.sp)
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
    onClick: () -> Unit
)
{
    /**
     * Botón reutilizable con estilo uniforme y lógica personalizada.
     *
     * @param label Texto visible del botón.
     * @param onClick Acción a ejecutar al presionar.
     * @param modifier Modificador opcional (tamaño, padding, etc.)
     */
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7948DB),
            contentColor = Color.White
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
        )
    }
}

@Composable
fun textDescripResetPass(label: String){
    /**
     * Composable reutilizable para mostrar la descripción de las pantallas de la seccion Recuperar Contraseña.
     *
     * @param label Descripcion a mostrar.
     */
    Text(label,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF8391A1)
    )
}

@Composable
fun asyncImgPerfil(
    profileImageUrl: String?,
    profileImageUri: Uri?,
    selectedAvatarUrl: String? = null,
    size: Int,
    shouldDeleteAvatar: Boolean = false
) {
    val imageModel: Any? = when {
        shouldDeleteAvatar -> null
        profileImageUri != null -> profileImageUri
        !selectedAvatarUrl.isNullOrBlank() -> selectedAvatarUrl
        !profileImageUrl.isNullOrBlank() && profileImageUrl.startsWith("http") -> profileImageUrl
        else -> null
    }

    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
            contentDescription = "Imagen de perfil",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(2.dp, Color.Black, CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Imagen por defecto",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            tint = Color.Gray
        )
    }
}

@Composable
fun AnimatedSnackbar(
    visible: Boolean,
    message: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(vertical = 12.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



@Composable
fun Headers(
    label: String,
    navController: NavController? = null,
    showBackButton: Boolean = false,
    color: Color = Color.Black
) {
    /**
     * Composable reutilizable para mostrar el encabezado de cada pantalla.
     *
     * Muestra un título centrado horizontalmente dentro de una Card.
     * Opcionalmente incluye un botón de retroceso si `showBackButton` es verdadero.
     *
     * @param label Título del encabezado.
     * @param navController Controlador de navegación, requerido si se usa el botón "Back".
     * @param showBackButton Si es verdadero, muestra un botón para retroceder.
     * @param color Color del texto del título.
     */

    if (showBackButton && navController != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "Volver",
                    modifier = Modifier.size(30.dp),
                )
            }
            //28
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.width(48.dp))
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun coloresDegradados(colors: List<Color>): Brush {
    /**
     * Retorna un Brush con un degradado horizontal a partir de una lista de colores.
     *
     * Esta función es útil para aplicar fondos con gradientes personalizados en componentes
     * como botones, tarjetas o cualquier contenedor que soporte `background(Brush)`.
     *
     * @param colors Lista de colores que definirán el degradado, en el orden en que deben aplicarse.
     * @return Un `Brush` de tipo `horizontalGradient` para ser usado en fondos.
     */

    return Brush.horizontalGradient(colors)
}
