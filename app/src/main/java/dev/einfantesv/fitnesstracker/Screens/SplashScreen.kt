package dev.einfantesv.fitnesstracker.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.einfantesv.fitnesstracker.R

@Composable
fun SplashScreen() {
    val SplashBackgroundColor = Color.White
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo App",
            modifier = Modifier.size(250.dp)
        )
    }
}