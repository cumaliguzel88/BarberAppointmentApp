package com.cumaliguzel.barberappointment

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.cumaliguzel.barberappointment.data.BarberDatabase

class BarberApplication : Application(), Configuration.Provider {
    val database: BarberDatabase by lazy { BarberDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        Log.d("BarberApplication", "Application started")
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
} 