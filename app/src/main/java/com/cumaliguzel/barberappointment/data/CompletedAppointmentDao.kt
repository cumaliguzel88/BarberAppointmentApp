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
        try {
            Log.d("CompletedAppointmentDao", "🔍 Tamamlanan randevu kontrolleri başlıyor - ID: ${appointment.originalAppointmentId}")

            // 1. Original ID kontrolü
            val existingById = getCompletedAppointmentByOriginalId(appointment.originalAppointmentId)
            if (existingById != null) {
                Log.w("CompletedAppointmentDao", "⚠️ Bu randevu zaten tamamlanmış olarak kaydedilmiş - Original ID: ${appointment.originalAppointmentId}")
                return false
            }

            // 2. Aynı müşteri, tarih ve saat kontrolü
            val existingSameDetails = getCompletedAppointmentByDetails(
                appointment.date,
                appointment.time,
                appointment.name
            )
            if (existingSameDetails != null) {
                Log.w("CompletedAppointmentDao", """
                    ⚠️ Aynı detaylarla randevu mevcut:
                    Müşteri: ${appointment.name}
                    Tarih: ${appointment.date}
                    Saat: ${appointment.time}
                """.trimIndent())
                return false
            }

            // 3. Güvenli ekleme işlemi
            insertCompletedAppointment(appointment)
            Log.d("CompletedAppointmentDao", """
                ✅ Yeni tamamlanan randevu başarıyla eklendi:
                ID: ${appointment.originalAppointmentId}
                Müşteri: ${appointment.name}
                Tarih: ${appointment.date}
                Saat: ${appointment.time}
            """.trimIndent())
            return true
        } catch (e: Exception) {
            Log.e("CompletedAppointmentDao", "💥 Randevu eklenirken hata oluştu", e)
            throw e
        }
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