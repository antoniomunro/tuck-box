package com.example.a20343426_assignment01.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.a20343426_assignment01.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_dark),
                contentDescription = "Tuck Box icon",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Create Account", fontSize = 30.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(text = "First Name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(text = "Last Name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text(text = "Mobile Number") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email Address") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(text = "Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = { deliveryAddress = it },
                label = { Text(text = "Delivery Address") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                onClick = {
                    when {
                        firstName.isBlank() -> scope.launch { snackbarHostState.showSnackbar("First Name is required") }
                        lastName.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Last Name is required") }
                        mobileNumber.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Mobile Number is required") }
                        email.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Email is required") }
                        password.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Password is required") }
                        confirmPassword.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Confirm Password is required") }
                        password != confirmPassword -> scope.launch { snackbarHostState.showSnackbar("Passwords do not match") }
                        deliveryAddress.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Delivery Address is required") }
                        else -> {
                            scope.launch {
                                authRegisterUsers(
                                    auth,
                                    email,
                                    password,
                                    firstName,
                                    lastName,
                                    mobileNumber,
                                    deliveryAddress,
                                    snackbarHostState,
                                    navController
                                )
                            }
                        }
                    }
                }) {
                Text(
                    text = "Register",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Already have an account?")

            TextButton(onClick = { navController.navigate("login") }) {
                Text(text = "Back to Login")
            }
        }
    }
}

suspend fun authRegisterUsers(
    auth: FirebaseAuth,
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    mobileNumber: String,
    deliveryAddress: String,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController
) {
    try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "mobileNumber" to mobileNumber,
            "email" to email,
            "address" to deliveryAddress
        )

        if (userId != null) {
            db.collection("users").document(userId).set(user).await()
            snackbarHostState.showSnackbar("Account created successfully!")
            navController.navigate("login")
        } else {
            throw Exception("User ID is null")
        }

    } catch (e: Exception) {
        Log.e("Registration", "Failed to create user", e)
        snackbarHostState.showSnackbar("Failed to create account: ${e.message}")
    }
}
