package com.cumaliguzel.barberappointment.data.dao

import android.util.Log
import androidx.room.*
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedAppointmentDao {
    companion object {
        private const val TAG = "CompletedAppointmentDao"
    }
    
    @Query("SELECT * FROM completed_appointments WHERE date = :date ORDER BY time ASC")
    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletedAppointment(appointment: CompletedAppointment): Long

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
    
    @Query("""
        SELECT EXISTS(SELECT 1 FROM completed_appointments 
        WHERE originalAppointmentId = :originalId)
    """)
    suspend fun hasCompletedAppointmentWithOriginalId(originalId: Int): Boolean
    
    @Query("""
        SELECT EXISTS(SELECT 1 FROM completed_appointments 
        WHERE date = :date AND time = :time AND name = :name)
    """)
    suspend fun hasCompletedAppointmentWithDetails(date: String, time: String, name: String): Boolean
    
    @Query("SELECT * FROM completed_appointments WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>>
    
    suspend fun safeInsertCompletedAppointment(appointment: CompletedAppointment): Boolean {
        try {
            Log.d(TAG, "🔍 Tamamlanan randevu kontrolleri başlıyor - ID: ${appointment.originalAppointmentId}")

            // 1. Original ID kontrolü
            val hasWithOriginalId = hasCompletedAppointmentWithOriginalId(appointment.originalAppointmentId)
            if (hasWithOriginalId) {
                Log.w(TAG, "⚠️ Bu randevu zaten tamamlanmış olarak kaydedilmiş - Original ID: ${appointment.originalAppointmentId}")
                return false
            }

            // 2. Aynı müşteri, tarih ve saat kontrolü
            val hasWithDetails = hasCompletedAppointmentWithDetails(
                appointment.date,
                appointment.time,
                appointment.name
            )
            if (hasWithDetails) {
                Log.w(TAG, """
                    ⚠️ Aynı detaylarla randevu mevcut:
                    Müşteri: ${appointment.name}
                    Tarih: ${appointment.date}
                    Saat: ${appointment.time}
                """.trimIndent())
                return false
            }

            // 3. Güvenli ekleme işlemi
            val result = insertCompletedAppointment(appointment)
            val success = result > 0
            
            if (success) {
                Log.d(TAG, """
                    ✅ Yeni tamamlanan randevu başarıyla eklendi:
                    ID: ${appointment.originalAppointmentId}
                    Müşteri: ${appointment.name}
                    Tarih: ${appointment.date}
                    Saat: ${appointment.time}
                """.trimIndent())
            } else {
                Log.w(TAG, "⚠️ Randevu eklenemedi, muhtemelen zaten mevcut")
            }
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "💥 Randevu eklenirken hata oluştu", e)
            return false
        }
    }
} 