package com.example.a20343426_assignment01.network

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.a20343426_assignment01.screens.CurrentOrderScreen
import com.example.a20343426_assignment01.screens.ForgotPasswordScreen
import com.example.a20343426_assignment01.screens.HomeScreen
import com.example.a20343426_assignment01.screens.LoginScreen
import com.example.a20343426_assignment01.screens.OrderHistoryScreen
import com.example.a20343426_assignment01.screens.PlaceOrderScreen
import com.example.a20343426_assignment01.screens.RegisterScreen
import com.example.a20343426_assignment01.screens.UpdateInfoScreen
import com.example.a20343426_assignment01.viewmodel.OrderViewModel

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("placeOrder") {
            PlaceOrderScreen(navController = navController)
        }
        composable("updateInfo") {
            UpdateInfoScreen(navController = navController)
        }
        composable("currentOrder") {
            CurrentOrderScreen(navController = navController)
        }
        composable("orderHistory") {
            OrderHistoryScreen(navController = navController)
        }
    }
}