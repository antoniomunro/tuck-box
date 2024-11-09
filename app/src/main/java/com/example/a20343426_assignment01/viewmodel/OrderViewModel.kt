package com.example.a20343426_assignment01.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a20343426_assignment01.model.Order
import com.example.a20343426_assignment01.model.MealSelection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel : ViewModel() {
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> get() = _currentOrder

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> get() = _orderHistory

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchCurrentOrder()
        fetchOrderHistory()
    }

    fun fetchCurrentOrder() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val orderSnapshot = db.collection("orders")
                    .whereEqualTo("userId", user.uid)
                    .whereEqualTo("status", "Current")
                    .limit(1)
                    .get()
                    .await()

                if (!orderSnapshot.isEmpty) {
                    val documentSnapshot = orderSnapshot.documents[0]
                    val order = documentSnapshot.toObject(Order::class.java)
                    order?.let {
                        it.orderId = it.orderId.ifEmpty { documentSnapshot.id }
                        val orderTimestamp = it.timestamp
                        val currentTimeMillis = System.currentTimeMillis()
                        val thirtyMinutesInMillis = 30 * 60 * 1000

                        if (currentTimeMillis - orderTimestamp >= thirtyMinutesInMillis) {
                            db.collection("orders")
                                .document(it.orderId)
                                .update("status", "Fulfilled")
                                .await()
                            _currentOrder.value = null
                        } else {
                            _currentOrder.value = it
                        }
                    }
                } else {
                    _currentOrder.value = null
                }
            } catch (e: Exception) {

            }
        }
    }

    fun fetchOrderHistory() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val orderSnapshot = db.collection("orders")
                    .whereEqualTo("userId", user.uid)
                    .whereEqualTo("status", "Fulfilled")
                    .get()
                    .await()

                if (!orderSnapshot.isEmpty) {
                    val orders = orderSnapshot.documents.mapNotNull {
                        val order = it.toObject(Order::class.java)
                        order?.orderId = it.id
                        order
                    }
                    _orderHistory.value = orders
                } else {
                    _orderHistory.value = emptyList()
                }
            } catch (e: Exception) {
            }
        }
    }
}
