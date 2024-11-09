package com.example.a20343426_assignment01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.a20343426_assignment01.network.AppNavGraph
import com.example.a20343426_assignment01.screens.SplashScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    var showSplashScreen by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
    }

    if (showSplashScreen) {
        SplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        val startDestination = if (isLoggedIn) "home" else "login"
        AppNavGraph(navController = navController, startDestination = startDestination)
    }
}