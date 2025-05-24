package dev.einfantesv.fitnesstracker.Screens.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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