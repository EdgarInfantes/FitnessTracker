package dev.einfantesv.fitnesstracker.Screens.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.einfantesv.fitnesstracker.R

@Composable
fun SplashScreen() {
    val SplashBackgroundColor = Color(0xFF7948DB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = "Logo App",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fitness Tracker",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 32.sp,
                    color = Color.White
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
