package com.cumaliguzel.barberappointment.repository

import com.cumaliguzel.barberappointment.data.*
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val completedAppointmentDao: CompletedAppointmentDao
) {
    
    fun getAllAppointments(): Flow<List<Appointment>> = 
        appointmentDao.getAllAppointments()
    
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByDate(date)
    
    suspend fun insertAppointment(appointment: Appointment) {
        appointmentDao.insertAppointment(appointment)
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
} 