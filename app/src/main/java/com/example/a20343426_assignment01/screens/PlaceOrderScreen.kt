package com.example.a20343426_assignment01.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.a20343426_assignment01.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) }
    var selectedRegion by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var selectedMeals by remember { mutableStateOf<Map<String, MealSelection>>(emptyMap()) }
    var selectedTime by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
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
            PlaceOrderHeader()

            Spacer(modifier = Modifier.height(50.dp))

            when (step) {
                1 -> SelectDeliveryRegionScreen(
                    onNext = { region: String ->
                        if (region.isNotEmpty()) {
                            selectedRegion = region
                            step = 2
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a delivery region")
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
                2 -> SelectDeliveryAddressScreen(
                    onNext = { address: String ->
                        if (address.isNotEmpty()) {
                            deliveryAddress = address
                            step = 3
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter a delivery address")
                            }
                        }
                    },
                    onBack = { step = 1 }
                )
                3 -> SelectMealsScreen(
                    selectedMeals = selectedMeals,
                    onSelectionChange = { meals: Map<String, MealSelection> -> selectedMeals = meals },
                    onNext = {
                        if (selectedMeals.isNotEmpty()) {
                            step = 4
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select at least one meal")
                            }
                        }
                    },
                    onBack = { step = 2 }
                )
                4 -> SelectDeliveryTimeScreen(
                    onNext = { time: String ->
                        if (time.isNotEmpty()) {
                            selectedTime = time
                            step = 5
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a delivery time")
                            }
                        }
                    },
                    onBack = { step = 3 }
                )
                5 -> SelectPaymentMethodScreen(
                    onNext = { paymentMethod: String ->
                        if (paymentMethod.isNotEmpty()) {
                            selectedPaymentMethod = paymentMethod
                            step = 6
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a payment method")
                            }
                        }
                    },
                    onBack = { step = 4 }
                )
                6 -> ConfirmOrderScreen(
                    selectedRegion = selectedRegion,
                    deliveryAddress = deliveryAddress,
                    selectedMeals = selectedMeals,
                    selectedTime = selectedTime,
                    selectedPaymentMethod = selectedPaymentMethod,
                    initialOrderNote = orderNote,
                    onConfirm = { note: String ->
                        orderNote = note
                        saveOrderToFirebase(
                            selectedRegion,
                            deliveryAddress,
                            selectedMeals,
                            selectedTime,
                            selectedPaymentMethod,
                            note
                        ) { success ->
                            if (success) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Order placed successfully!")
                                }
                                navController.navigate("home") {
                                    popUpTo("placeOrder") { inclusive = true }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Order confirmation failed")
                                }
                            }
                        }
                    },
                    onCancel = {
                        navController.navigate("home")
                    },
                    onBack = { step = 5 }
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
    paymentMethod: String,
    note: String,
    onComplete: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user == null) {
        onComplete(false)
        return
    }

    val order = hashMapOf(
        "userId" to user.uid,
        "region" to region,
        "address" to address,
        "meals" to meals.values.map { mealSelection ->
            hashMapOf(
                "meal" to mealSelection.meal,
                "option" to mealSelection.option,
                "quantity" to mealSelection.quantity
            )
        },
        "time" to time,
        "paymentMethod" to paymentMethod,
        "notes" to note,
        "status" to "Current",
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("orders")
        .add(order)
        .addOnSuccessListener { documentReference ->
            val orderId = documentReference.id
            db.collection("orders").document(orderId)
                .update("orderId", orderId)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Delivery Region", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        regions.forEach { region ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedRegion == region, onClick = { selectedRegion = region })
                Text(text = region)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { onBack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onNext(selectedRegion) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
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
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    var address by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        if (user != null) {
            try {
                val document = db.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    address = document.getString("address") ?: ""
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("User data not found.")
                    }
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to load address: ${e.message}")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("User not logged in.")
            }
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Delivery Address", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (address.isNotEmpty()) {
                            onNext(address)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter a delivery address.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Next")
                }
            }
        }
    }

    if (!isLoading) {
        SnackbarHost(hostState = snackbarHostState)
    }
}

data class MealSelection(val meal: String, val option: String, val quantity: Int)
data class MealData(val name: String, val price: Double, val options: List<String>, val imageRes: Int)

@Composable
fun SelectMealsScreen(
    selectedMeals: Map<String, MealSelection>,
    onSelectionChange: (Map<String, MealSelection>) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val mealData = mapOf(
        "Salads" to listOf(
            MealData(
                name = "Green Salad Lunch",
                price = 10.99,
                options = listOf("None", "Ranch", "Vinaigrette"),
                imageRes = R.drawable.green_salad_lunch
            ),
            MealData(
                name = "Beef Noodle Salad",
                price = 12.50,
                options = listOf("No Chili Flakes", "Regular Chili Flakes", "Extra Chili Flakes"),
                imageRes = R.drawable.beef_noodle_salad
            )
        ),
        "Curries" to listOf(
            MealData(
                name = "Lamb Korma",
                price = 15.99,
                options = listOf("Mild", "Medium", "Hot"),
                imageRes = R.drawable.lamb_korma
            ),
            MealData(
                name = "Chicken Curry",
                price = 14.50,
                options = listOf("Mild", "Kiwi Hot", "Hot"),
                imageRes = R.drawable.chicken_curry
            )
        ),
        "Sandwiches" to listOf(
            MealData(
                name = "Open Chicken Sandwich",
                price = 8.99,
                options = listOf("White", "Rye", "Wholemeal"),
                imageRes = R.drawable.open_chicken_sandwich
            )
        )
    )

    val mealPriceMap = mealData.flatMap { (_, meals) -> meals }.associateBy { it.name }

    var totalCost by remember { mutableDoubleStateOf(0.0) }

    fun getItemCount(): String {
        return selectedMeals.entries.joinToString(", ") { "${it.value.quantity}x ${it.value.meal}" }
    }

    fun updateTotalCost(meals: Map<String, MealSelection>) {
        totalCost = meals.values.sumOf { selection ->
            val meal = mealPriceMap[selection.meal]
            (meal?.price ?: 0.0) * selection.quantity
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(160.dp))
        Text(text = "Select Meals", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1F)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            mealData.forEach { (category, meals) ->
                item {
                    Text(text = category, style = MaterialTheme.typography.titleMedium)

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(meals) { meal ->
                            var selectedOption by remember { mutableStateOf(meal.options.first()) }
                            var quantity by remember { mutableIntStateOf(1) }

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .width(250.dp)
                                    .height(500.dp)
                                    .padding(4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = meal.imageRes),
                                        contentDescription = "${meal.name} Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),

                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = meal.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "\$${meal.price}", style = MaterialTheme.typography.bodyLarge)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = "Select an option:")
                                    meal.options.forEach { option ->
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
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (quantity > 1) quantity-- }
                                        ) {
                                            Text(
                                                text = "-",
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text(
                                            text = quantity.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        IconButton(
                                            onClick = { quantity++ }
                                        ) {
                                            Text(
                                                text = "+",
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }


                                    Button(
                                        onClick = {
                                            val newSelection = selectedMeals.toMutableMap()
                                            newSelection[meal.name] = MealSelection(
                                                meal.name,
                                                selectedOption,
                                                quantity
                                            )
                                            updateTotalCost(newSelection)
                                            onSelectionChange(newSelection)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Black,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Text(text = "Add to Cart")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomAppBar(
            containerColor = Color.Black,
            contentPadding = PaddingValues(16.dp)
        ) {
            Row {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Back")
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Next")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            modifier = Modifier .padding(start = 100.dp),
                            text = "Total: \$${"%.2f".format(totalCost)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Delivery Time", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        times.forEach { time ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedTime == time, onClick = { selectedTime = time })
                Text(text = time)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { if (selectedTime.isNotEmpty()) onNext(selectedTime) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Next")
            }
        }
    }
}

@Composable
fun SelectPaymentMethodScreen(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    val paymentMethods = listOf("Credit Card", "Cash on Delivery")
    var selectedMethod by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select Payment Method", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        paymentMethods.forEach { method ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                Text(text = method)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onNext(selectedMethod) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
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
    selectedPaymentMethod: String,
    initialOrderNote: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    var orderNote by remember { mutableStateOf(initialOrderNote) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(200.dp))

        Text(text = "Confirm Order", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Region: $selectedRegion")
        Text(text = "Address: $deliveryAddress")
        Text(text = "Delivery Time: $selectedTime")
        Text(text = "Payment Method: $selectedPaymentMethod")

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Meals:", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))
        selectedMeals.forEach { (_, mealSelection) ->
            Text(text = "${mealSelection.quantity}x ${mealSelection.meal} - ${mealSelection.option}")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = orderNote,
            onValueChange = { orderNote = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onConfirm(orderNote) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
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
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Place Order",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
