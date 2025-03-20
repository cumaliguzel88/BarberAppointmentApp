package com.cumaliguzel.barberappointment.repository

import android.util.Log
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.data.dao.AppointmentDao
import com.cumaliguzel.barberappointment.data.dao.CompletedAppointmentDao
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val completedAppointmentDao: CompletedAppointmentDao
) {
    
    companion object {
        private const val TAG = "AppointmentRepository"
    }
    
    fun getAllAppointments(): Flow<List<Appointment>> = 
        appointmentDao.getAllAppointments()
    
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByDate(date)
    
    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment)
    }
    
    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment)
    }
    
    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.deleteAppointment(appointment)
    }
    
    suspend fun getAppointmentById(id: Int): Appointment? =
        appointmentDao.getAppointmentById(id)

    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> =
        completedAppointmentDao.getCompletedAppointmentsByDate(date)

    suspend fun insertCompletedAppointment(appointment: CompletedAppointment) =
        completedAppointmentDao.insertCompletedAppointment(appointment)

    suspend fun getCompletedAppointmentByOriginalId(originalId: Int): CompletedAppointment? =
        completedAppointmentDao.getCompletedAppointmentByOriginalId(originalId)

    suspend fun safeInsertCompletedAppointment(appointment: CompletedAppointment): Boolean =
        completedAppointmentDao.safeInsertCompletedAppointment(appointment)

    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>> =
        completedAppointmentDao.getCompletedAppointmentsBetweenDates(startDate, endDate)

    suspend fun updateAppointmentStatus(appointmentId: Long, newStatus: String) {
        try {
            val appointment = getAppointmentById(appointmentId.toInt())
            if (appointment != null) {
                val updatedAppointment = appointment.copy(status = newStatus)
                updateAppointment(updatedAppointment)
                Log.d(TAG, "✅ Randevu durumu güncellendi: ID=$appointmentId, Yeni Durum=$newStatus")
            } else {
                Log.e(TAG, "❌ Durum güncellenemedi: Randevu bulunamadı (ID=$appointmentId)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Durum güncellenirken hata: ${e.message}", e)
        }
    }
} 