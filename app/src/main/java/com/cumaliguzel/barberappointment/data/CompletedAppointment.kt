package com.cumaliguzel.barberappointment.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_appointments")
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