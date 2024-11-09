package com.example.a20343426_assignment01.model

data class Order(
    var orderId: String = "",
    val userId: String = "",
    val region: String = "",
    val address: String = "",
    val time: String = "",
    val meals: List<Meal> = emptyList(),
    val notes: String = "",
    val status: String = "",
    val timestamp: Long = 0L
)

data class Meal(
    val meal: String = "",
    val option: String = "",
    val quantity: Int = 0
)

data class MealSelection(
    val meal: String,
    val option: String,
    val quantity: Int
)
