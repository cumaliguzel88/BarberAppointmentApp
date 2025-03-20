package com.cumaliguzel.barberappointment.repository

import android.util.Log
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.data.dao.CompletedAppointmentDao
import kotlinx.coroutines.flow.Flow

class CompletedAppointmentRepository(
    private val completedAppointmentDao: CompletedAppointmentDao
) {
    companion object {
        private const val TAG = "CompletedAppointmentRepo"
    }
    
    suspend fun insertCompletedAppointment(appointment: CompletedAppointment): Long {
        return completedAppointmentDao.insertCompletedAppointment(appointment)
    }
    
    fun getAllCompletedAppointments(): Flow<List<CompletedAppointment>> {
        return completedAppointmentDao.getAllCompletedAppointments()
    }
    
    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> {
        return completedAppointmentDao.getCompletedAppointmentsByDate(date)
    }
    
    suspend fun getCompletedAppointmentByOriginalId(originalId: Int): CompletedAppointment? {
        return completedAppointmentDao.getCompletedAppointmentByOriginalId(originalId)
    }
    
    suspend fun hasCompletedAppointmentWithOriginalId(originalId: Int): Boolean {
        return completedAppointmentDao.hasCompletedAppointmentWithOriginalId(originalId)
    }
    
    suspend fun hasCompletedAppointmentWithDetails(date: String, time: String, name: String): Boolean {
        return completedAppointmentDao.hasCompletedAppointmentWithDetails(date, time, name)
    }
    
    suspend fun safeInsertCompletedAppointment(appointment: CompletedAppointment): Boolean {
        try {
            Log.d(TAG, "🔄 Güvenli ekleme işlemi başlatılıyor: ${appointment.name}")
            return completedAppointmentDao.safeInsertCompletedAppointment(appointment)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Tamamlanan randevu eklenirken hata: ${e.message}", e)
            return false
        }
    }
    
    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>> {
        return completedAppointmentDao.getCompletedAppointmentsBetweenDates(startDate, endDate)
    }
} 