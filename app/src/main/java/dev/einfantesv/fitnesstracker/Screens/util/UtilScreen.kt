package dev.einfantesv.fitnesstracker.Screens.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.einfantesv.fitnesstracker.R
import org.w3c.dom.Text


@Composable
fun BackButtonScreen(navController: NavController) {
    IconButton(
        onClick = { navController.popBackStack() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
            contentDescription = "Volver",
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
fun ButtonScreen(navController: NavController, screen: String, label : String){
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
fun BackTextUtil(navController: NavController, label: String){
    //Row boton regresar y texto
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Boton Regresar
        IconButton(
            onClick = { navController.popBackStack() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Volver",
                modifier = Modifier.size(30.dp),
            )
        }

        //Espaciador
        Spacer(modifier = Modifier.width(16.dp))

        //Texto
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun textDescripResetPass(label: String){
    Text(label,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF8391A1)
    )
}

@Composable
fun asyncImgPerfil(profileImageUrl: String, size: Int) {
    if (profileImageUrl.isNotEmpty()) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = "Imagen de perfil",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(2.dp, Color.Black, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
