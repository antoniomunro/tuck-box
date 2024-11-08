package com.example.a20343426_assignment01.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.a20343426_assignment01.R
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOrderScreen(navController: NavController) {
    val context = LocalContext.current
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFEDE7F6)
        )
    )

    var step by remember { mutableStateOf(1) }
    var selectedRegion by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var selectedMeals by remember { mutableStateOf<Map<String, MealSelection>>(emptyMap()) }
    var selectedTime by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }

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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(it)
        ) {
            PlaceOrderHeader()

            Spacer(modifier = Modifier.height(50.dp))

            when (step) {
                1 -> SelectDeliveryRegionScreen(
                    onNext = { region ->
                        if (region.isNotEmpty()) {
                            selectedRegion = region
                            step = 2
                        } else {
                            Toast.makeText(context, "Please select a delivery region", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { step = 0 }
                )
                2 -> SelectDeliveryAddressScreen(
                    onNext = { address ->
                        if (address.isNotEmpty()) {
                            deliveryAddress = address
                            step = 3
                        } else {
                            Toast.makeText(context, "Please enter a delivery address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { step = 1 }
                )
                3 -> SelectMealsScreen(
                    selectedMeals = selectedMeals,
                    onSelectionChange = { meals -> selectedMeals = meals },
                    onNext = {
                        if (selectedMeals.isNotEmpty()) {
                            step = 4
                        } else {
                            Toast.makeText(context, "Please select at least one meal", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { step = 2 }
                )
                4 -> SelectDeliveryTimeScreen(
                    onNext = { time ->
                        if (time.isNotEmpty()) {
                            selectedTime = time
                            step = 5
                        } else {
                            Toast.makeText(context, "Please select a delivery time", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { step = 3 }
                )
                5 -> ConfirmOrderScreen(
                    selectedRegion = selectedRegion,
                    deliveryAddress = deliveryAddress,
                    selectedMeals = selectedMeals,
                    selectedTime = selectedTime,
                    initialOrderNote = orderNote,
                    onConfirm = { note ->
                        saveOrderToFirebase(
                            selectedRegion,
                            deliveryAddress,
                            selectedMeals,
                            selectedTime,
                            note
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    popUpTo("placeOrder") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Order confirmation failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onCancel = {
                        navController.navigate("home")
                    },
                    onBack = { step = 4 }
                )
            }
        }
    }
}

private fun saveOrderToFirebase(
    region: String,
    address: String,
    meals: Map<String, MealSelection>,
    time: String,
    note: String,
    onComplete: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val order = hashMapOf(
        "region" to region,
        "address" to address,
        "meals" to meals.map { (mealName, mealSelection) ->
            hashMapOf(
                "meal" to mealSelection.meal,
                "option" to mealSelection.option,
                "quantity" to mealSelection.quantity
            )
        },
        "time" to time,
        "notes" to note,
        "status" to "Current"
    )

    db.collection("orders")
        .add(order)
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener {
            onComplete(false)
        }
}

@Composable
fun SelectDeliveryRegionScreen(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    val regions = listOf("Palmerston North", "Feilding", "Ashhurst", "Longburn")
    var selectedRegion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Delivery Region", style = MaterialTheme.typography.titleLarge)

        regions.forEach { region ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedRegion == region, onClick = { selectedRegion = region })
                Text(text = region)
            }
        }

        Row {
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onNext(selectedRegion) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
fun SelectDeliveryAddressScreen(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter Delivery Address", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Button(onClick = onBack) {
                Text(text = "Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { onNext(address) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White),) {
                Text(text = "Next")
            }
        }
    }
}

data class MealSelection(val meal: String, val option: String, val quantity: Int)

@Composable
fun SelectMealsScreen(
    selectedMeals: Map<String, MealSelection>,
    onSelectionChange: (Map<String, MealSelection>) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val mealImages = mapOf(
        "Green Salad Lunch" to R.drawable.green_salad_lunch,
        "Lamb Korma" to R.drawable.lamb_korma,
        "Open Chicken Sandwich" to R.drawable.open_chicken_sandwich,
        "Beef Noodle Salad" to R.drawable.beef_noodle_salad
    )

    val meals = listOf(
        "Green Salad Lunch" to listOf("None", "Ranch", "Vinaigrette"),
        "Lamb Korma" to listOf("Mild", "Med", "Hot"),
        "Open Chicken Sandwich" to listOf("White", "Rye", "Wholemeal"),
        "Beef Noodle Salad" to listOf("No Chili Flakes", "Regular Chili Flakes", "Extra Chili Flakes")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(150.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(meals) { (meal, options) ->
                var selectedOption by remember { mutableStateOf("") }
                var quantity by remember { mutableStateOf(1) }

                val imagePainter: Painter =
                    painterResource(id = mealImages[meal] ?: R.drawable.logo_red)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(text = meal, style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(painter = imagePainter, contentDescription = "Meal Image")

                    Text(text = "Select an option:")

                    options.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(text = option)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Quantity:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { if (quantity > 1) quantity-- },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent)
                            ) {
                            Text(text = "-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = quantity.toString())
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { quantity++ },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent)
                            ) {
                            Text(text = "+")
                        }
                        Button(
                            onClick = {
                                val newSelection = selectedMeals.toMutableMap()
                                newSelection[meal] = MealSelection(meal, selectedOption, quantity)
                                onSelectionChange(newSelection)
                            }
                        ) {
                            Text(text = "Add to Cart")
                        }
                    }
                }
            }
        }

        Row {
            Button(onClick = onBack) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onNext) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
fun SelectDeliveryTimeScreen(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTime by remember { mutableStateOf("") }
    val times = listOf("12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Delivery Time", style = MaterialTheme.typography.titleLarge)

        times.forEach { time ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedTime == time, onClick = { selectedTime = time })
                Text(text = time)
            }
        }

        Row {
            Button(onClick = onBack) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { if (selectedTime.isNotEmpty()) onNext(selectedTime) }) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
fun ConfirmOrderScreen(
    selectedRegion: String,
    deliveryAddress: String,
    selectedMeals: Map<String, MealSelection>,
    selectedTime: String,
    initialOrderNote: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    var orderNote by remember { mutableStateOf(initialOrderNote) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Confirm Order", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Region: $selectedRegion")
        Text(text = "Address: $deliveryAddress")
        Text(text = "Time: $selectedTime")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Meals:")
        selectedMeals.forEach { (mealName, mealSelection) ->
            Text(text = "${mealSelection.quantity}x $mealName - ${mealSelection.option}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = orderNote,
            onValueChange = { orderNote = it },
            label = { Text("Additional Notes") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = onBack) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onConfirm(orderNote) }) {
                Text(text = "Confirm Order")
            }
        }
    }
}

@Composable
fun PlaceOrderHeader() {
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
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Place Order",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}