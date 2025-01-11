package com.cumaliguzel.barberappointment.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_prices")
data class OperationPrice(
    @PrimaryKey
    val operation: String,
    val price: Double
) 