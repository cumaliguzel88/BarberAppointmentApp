package com.cumaliguzel.barberappointment.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val operation: String,
    val date: String,
    val time: String,
    val price: Double = 0.0,
    var status: String = "Pending"
) 