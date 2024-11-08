package com.example.a20343426_assignment01.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentOrderScreen(navController: NavController) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFEDE7F6)
        )
    )

    val db = FirebaseFirestore.getInstance()
    val currentOrder = remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        try {
            val orderSnapshot = db.collection("orders")
                .whereEqualTo("status", "Current")
                .limit(1)
                .get()
                .await()

            if (!orderSnapshot.isEmpty) {
                val order = orderSnapshot.documents[0].toObject(Order::class.java)
                currentOrder.value = order

                order?.let {
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val deliveryTime = dateFormat.parse(order.time)
                    val currentTime = Calendar.getInstance().time
                    if (currentTime.after(deliveryTime)) {
                        db.collection("orders")
                            .document(order.id)
                            .update("status", "Fulfilled")
                            .await()
                    }
                }
            } else {
                Log.d("CurrentOrderScreen", "No current order found")
            }
        } catch (e: Exception) {
            Log.e("CurrentOrderScreen", "Error fetching current order: ${e.message}")
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

            if (currentOrder.value == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No current order available", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                val order = currentOrder.value!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CurrentOrderHeader()
                    Spacer(modifier = Modifier.height(50.dp))

                    OrderDetails(order)

                    Spacer(modifier = Modifier.height(32.dp))

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

data class Order(
    val id: String = "",
    val region: String = "",
    val address: String = "",
    val time: String = "",
    val meals: List<Meal> = emptyList(),
    val notes: String = "",
    val status: String = ""
)

data class Meal(
    val meal: String = "",
    val quantity: Int = 0
)

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
                Icon(Icons.Filled.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current Orders",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}