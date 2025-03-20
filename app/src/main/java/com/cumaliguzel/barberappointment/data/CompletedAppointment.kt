package com.cumaliguzel.barberappointment.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_appointments",
    indices = [
        Index(value = ["originalAppointmentId"], unique = true),
        Index(value = ["date", "time", "name"], unique = true)
    ]
)
data class CompletedAppointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalAppointmentId: Int,
    val name: String,
    val operation: String,
    val date: String,
    val time: String,
    val price: Double,
    val completedAt: String // Timestamp when marked as completed
) 