package com.cumaliguzel.barberappointment.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Appointment::class], version = 1, exportSchema = false)
abstract class BarberDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    
    companion object {
        @Volatile
        private var Instance: BarberDatabase? = null
        
        fun getDatabase(context: Context): BarberDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    BarberDatabase::class.java,
                    "barber_database"
                )
                .build()
                .also { Instance = it }
            }
        }
    }
} 