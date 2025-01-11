package com.cumaliguzel.barberappointment.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Appointment::class, OperationPrice::class, CompletedAppointment::class], 
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun operationPriceDao(): OperationPriceDao
    abstract fun completedAppointmentDao(): CompletedAppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS operation_prices (
                        operation TEXT NOT NULL PRIMARY KEY,
                        price REAL NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS completed_appointments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        originalAppointmentId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        date TEXT NOT NULL,
                        time TEXT NOT NULL,
                        price REAL NOT NULL,
                        completedAt TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS idx_original_appointment_id 
                    ON completed_appointments(originalAppointmentId)
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barber_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 