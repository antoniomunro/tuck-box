package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "FoodExtraDetails",
    foreignKeys = [
        ForeignKey(
            entity = Food::class,
            parentColumns = ["foodId"],
            childColumns = ["foodFoodId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodExtraDetail(
    @PrimaryKey val foodDetailsId: String,
    val detailsName: String,
    val foodFoodId: String
)
