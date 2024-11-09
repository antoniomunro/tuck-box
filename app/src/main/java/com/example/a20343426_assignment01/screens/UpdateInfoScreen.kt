package com.example.a20343426_assignment01.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.EmailAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateInfoScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var address by remember { mutableStateOf("") }
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            try {
                val document = firestore.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    userData = document.data
                    firstName = document.getString("firstName") ?: ""
                    lastName = document.getString("lastName") ?: ""
                    address = document.getString("address") ?: ""
                    mobileNumber = document.getString("mobileNumber") ?: ""
                    email = user.email ?: ""
                    userId = user.uid
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to load user data: ${e.message}")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("User not logged in")
            }
        }
        isLoading = false
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE7F6),
            Color(0xFFEDE7F6)
        )
    )

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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = gradientBrush)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UpdateInfoHeader(userData)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "User ID: $userId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        placeholder = { Text(text = firstName) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        placeholder = { Text(text = lastName) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text(text = mobileNumber) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text(text = email) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        placeholder = { Text(text = address) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password*") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF408AF0),
                            contentColor = Color.White),
                        onClick = {
                            scope.launch {
                                try {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        if (currentPassword.isNotEmpty()) {
                                            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                                            try {
                                                user.reauthenticate(credential).await()
                                                val userInfo = mutableMapOf<String, Any?>()
                                                if (firstName.isNotEmpty()) userInfo["firstName"] = firstName
                                                if (lastName.isNotEmpty()) userInfo["lastName"] = lastName
                                                if (address.isNotEmpty()) userInfo["address"] = address
                                                if (mobileNumber.isNotEmpty()) userInfo["mobileNumber"] = mobileNumber
                                                if (email.isNotEmpty() && email != user.email) {
                                                    user.updateEmail(email).await()
                                                    userInfo["email"] = email
                                                } else if (user.email != null) {
                                                    userInfo["email"] = user.email
                                                }

                                                if (userInfo.isNotEmpty()) {
                                                    firestore.collection("users").document(user.uid).set(userInfo).await()
                                                }
                                                navController.navigate("home")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Authentication failed: ${e.message}")
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar("Your current password must be entered to update")
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("User not logged in")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Update failed: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Update")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF04040),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Delete Account")
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(text = "Required*")
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Account") },
                        text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val user = auth.currentUser
                                            user?.let {
                                                firestore.collection("users").document(it.uid).delete().await()

                                                it.delete().await()

                                                navController.navigate("login") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Account deletion failed: ${e.message}")
                                        } finally {
                                            showDeleteDialog = false
                                        }
                                    }
                                }
                            ) {
                                Text("Yes, Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateInfoHeader(userData: Map<String, Any>?) {
    Card(
        shape = RoundedCornerShape(8.dp),
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
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Update Info",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
