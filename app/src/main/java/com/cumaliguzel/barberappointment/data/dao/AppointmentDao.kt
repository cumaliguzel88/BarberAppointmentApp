package com.cumaliguzel.barberappointment.data.dao

import androidx.room.*
import com.cumaliguzel.barberappointment.data.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<Appointment>>
    
    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY time ASC")
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)
    
    @Update
    suspend fun updateAppointment(appointment: Appointment)
    
    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
    
    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Int): Appointment?
} 