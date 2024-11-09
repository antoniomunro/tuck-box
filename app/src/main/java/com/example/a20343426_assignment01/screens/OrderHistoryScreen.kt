package com.example.a20343426_assignment01.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.a20343426_assignment01.model.Order
import com.example.a20343426_assignment01.viewmodel.OrderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel()) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFEDE7F6)
        )
    )

    val fulfilledOrders by orderViewModel.orderHistory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(paddingValues)
        ) {
            if (fulfilledOrders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrderHistoryHeader()
                    Spacer(modifier = Modifier.height(50.dp))
                    Text("No fulfilled orders available", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        OrderHistoryHeader()
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                    items(fulfilledOrders) { order ->
                        FulfilledOrderDetails(order)
                    }
                }
            }
        }
    }
}

@Composable
fun FulfilledOrderDetails(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.bodyLarge)
            Text("Region: ${order.region}", style = MaterialTheme.typography.bodyLarge)
            Text("Address: ${order.address}", style = MaterialTheme.typography.bodyLarge)
            Text("Delivery Time: ${order.time}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Meals:", style = MaterialTheme.typography.bodyLarge)
            order.meals.forEach { meal ->
                Text(
                    "- ${meal.quantity}x ${meal.meal} - ${meal.option}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (order.notes.isNotEmpty()) {
                Text("Notes: ${order.notes}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun OrderHistoryHeader() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Order History",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
