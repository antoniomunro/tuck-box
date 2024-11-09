package com.example.a20343426_assignment01.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
fun CurrentOrderScreen(navController: NavController, orderViewModel: OrderViewModel = viewModel()) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFEDE7F6), Color(0xFFEDE7F6))
    )
    val currentOrder by orderViewModel.currentOrder.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Current Order") },
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
            if (currentOrder == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No current order available",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                val order = currentOrder!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        CurrentOrderHeader()
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    item {
                        OrderDetails(order)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetails(order: Order) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(8.dp))
            Text("Notes: ${order.notes}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CurrentOrderHeader() {
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
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current Orders",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
