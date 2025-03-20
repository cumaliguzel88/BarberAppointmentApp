package com.cumaliguzel.barberappointment.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cumaliguzel.barberappointment.data.dao.AppointmentDao
import com.cumaliguzel.barberappointment.data.dao.CompletedAppointmentDao


@Database(
    entities = [
        Appointment::class, 
        CompletedAppointment::class, 
        OperationPrice::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BarberDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun completedAppointmentDao(): CompletedAppointmentDao
    abstract fun operationPriceDao(): OperationPriceDao

    companion object {
        @Volatile
        private var INSTANCE: BarberDatabase? = null

        fun getDatabase(context: Context): BarberDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarberDatabase::class.java,
                    "barber_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 