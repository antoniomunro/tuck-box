package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Foods")
data class Food(
    @PrimaryKey val foodId: String,
    val foodName: String,
    val foodExtraChoice: String
)
