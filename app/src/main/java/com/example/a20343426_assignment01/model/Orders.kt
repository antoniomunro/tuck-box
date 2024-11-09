package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "Orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userUserId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodExtraDetail::class,
            parentColumns = ["foodDetailsId"],
            childColumns = ["foodExtraDetailsFoodDetailsId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = City::class,
            parentColumns = ["cityId"],
            childColumns = ["cityCityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimeSlot::class,
            parentColumns = ["timeSlotId"],
            childColumns = ["timeSlotsTimeSlotId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Orders(
    @PrimaryKey val orderId: String,
    val orderDate: String,
    val quantity: Int,
    val foodExtraDetailsFoodDetailsId: String,
    val cityCityId: String,
    val timeSlotsTimeSlotId: String,
    val userUserId: String
)
