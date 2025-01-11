package com.cumaliguzel.barberappointment.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedAppointmentDao {
    @Query("SELECT * FROM completed_appointments WHERE date = :date ORDER BY time ASC")
    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletedAppointment(appointment: CompletedAppointment)

    @Query("SELECT * FROM completed_appointments")
    fun getAllCompletedAppointments(): Flow<List<CompletedAppointment>>

    @Delete
    suspend fun deleteCompletedAppointment(appointment: CompletedAppointment)

    @Query("""
        SELECT * FROM completed_appointments 
        WHERE originalAppointmentId = :originalId 
        LIMIT 1
    """)
    suspend fun getCompletedAppointmentByOriginalId(originalId: Int): CompletedAppointment?

    @Transaction
    suspend fun safeInsertCompletedAppointment(appointment: CompletedAppointment): Boolean {
        val existing = getCompletedAppointmentByOriginalId(appointment.originalAppointmentId)
        if (existing == null) {
            insertCompletedAppointment(appointment)
            return true
        }
        return false
    }
} 