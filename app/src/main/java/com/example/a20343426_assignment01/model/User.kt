package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class User(
    @PrimaryKey val userId: String,
    val userEmail: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val mobile: String
)
