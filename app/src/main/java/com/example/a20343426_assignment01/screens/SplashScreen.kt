package com.example.a20343426_assignment01.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20343426_assignment01.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(3000)
        onTimeout()
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.floating_pizza),
                    contentDescription = "Floating Pizza",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                        .offset(y = (-140).dp)
                )
                Text(
                    text = "Tuck Box",
                    fontSize = 50.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
