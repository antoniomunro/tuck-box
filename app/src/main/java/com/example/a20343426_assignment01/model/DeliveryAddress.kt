package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "DeliveryAddresses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DeliveryAddress(
    @PrimaryKey val addressId: String,
    val address: String,
    val userUserId: String
)
