package dev.einfantesv.fitnesstracker.Screens.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.einfantesv.fitnesstracker.R


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