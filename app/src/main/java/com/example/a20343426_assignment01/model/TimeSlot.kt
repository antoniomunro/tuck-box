package com.example.a20343426_assignment01.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TimeSlots")
data class TimeSlot(
    @PrimaryKey val timeSlotId: String,
    val timeSlot: String
)
