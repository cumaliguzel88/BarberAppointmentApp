package com.cumaliguzel.barberappointment.data

import android.util.Log
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
        // ID'ye göre kontrol et
        val existingById = getCompletedAppointmentByOriginalId(appointment.originalAppointmentId)
        if (existingById != null) {
            Log.d("CompletedAppointmentDao", "ID'ye göre randevu zaten var: ${appointment.originalAppointmentId}")
            return false
        }
        
        // Aynı gün, saat ve müşteriye göre kontrol et
        val existingSameDetails = getCompletedAppointmentByDetails(
            appointment.date, 
            appointment.time, 
            appointment.name
        )
        
        if (existingSameDetails != null) {
            Log.d("CompletedAppointmentDao", "Aynı detaylarla randevu zaten var: ${appointment.date}, ${appointment.time}, ${appointment.name}")
            return false
        }
        
        // Hiçbir benzer kayıt yoksa ekle
        insertCompletedAppointment(appointment)
        Log.d("CompletedAppointmentDao", "Yeni tamamlanmış randevu eklendi: ${appointment.originalAppointmentId}")
        return true
    }

    @Query("SELECT * FROM completed_appointments WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>>

    // Aynı tarih, saat ve müşteri adına göre randevu kontrolü için yeni metot
    @Query("""
        SELECT * FROM completed_appointments 
        WHERE date = :date AND time = :time AND name = :name
        LIMIT 1
    """)
    suspend fun getCompletedAppointmentByDetails(date: String, time: String, name: String): CompletedAppointment?
} 