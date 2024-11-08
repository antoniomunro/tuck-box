package com.example.a20343426_assignment01.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(navController: NavController) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFEDE7F6)
        )
    )

    val db = FirebaseFirestore.getInstance()
    val fulfilledOrders = remember { mutableStateOf<List<PreviousOrder>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val orderSnapshot = db.collection("orders")
                .whereEqualTo("status", "Fulfilled")
                .get()
                .await()

            if (!orderSnapshot.isEmpty) {
                val orders = orderSnapshot.documents.mapNotNull { it.toObject(PreviousOrder::class.java) }
                fulfilledOrders.value = orders
            } else {
                Log.d("OrderHistoryScreen", "No fulfilled orders found")
            }
        } catch (e: Exception) {
            Log.e("OrderHistoryScreen", "Error fetching fulfilled orders: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(paddingValues)
        ) {

            if (fulfilledOrders.value.isEmpty()) {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrderHistoryHeader()
                    Spacer(modifier = Modifier.height(50.dp))
                    fulfilledOrders.value.forEach { order ->
                        FulfilledOrderDetails(order)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FulfilledOrderDetails(order: PreviousOrder) {
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
            Text("Order ID: ${order.id}", style = MaterialTheme.typography.bodyLarge)
            Text("Region: ${order.region}", style = MaterialTheme.typography.bodyLarge)
            Text("Address: ${order.address}", style = MaterialTheme.typography.bodyLarge)
            Text("Delivery Time: ${order.time}", style = MaterialTheme.typography.bodyLarge)

            Text("Meals:", style = MaterialTheme.typography.bodyLarge)
            order.meals.forEach { meal ->
                Text("- ${meal.meal} (${meal.quantity}x)", style = MaterialTheme.typography.bodyMedium)
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
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Order History",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
data class PreviousOrder(
    val id: String = "",
    val region: String = "",
    val address: String = "",
    val time: String = "",
    val meals: List<PreviousMeal> = emptyList(),
    val notes: String = "",
    val status: String = ""
)

data class PreviousMeal(
    val meal: String = "",
    val quantity: Int = 0
)


