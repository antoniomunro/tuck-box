package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cities")
data class City(
    @PrimaryKey val cityId: String,
    val cityName: String
)
