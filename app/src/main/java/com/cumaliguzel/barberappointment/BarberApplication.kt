package com.cumaliguzel.barberappointment

import android.app.Application
import com.cumaliguzel.barberappointment.data.BarberDatabase

class BarberApplication : Application() {
    val database: BarberDatabase by lazy { BarberDatabase.getDatabase(this) }
} 