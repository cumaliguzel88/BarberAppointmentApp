package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppointmentUseCase(val repository: AppointmentRepository) {

    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        repository.getAppointmentsByDate(date)

    fun getDailyEarnings(date: String): Flow<Double> =
        repository.getAppointmentsByDate(date)
            .map { appointments ->
                appointments
                    .filter { it.status == "Completed" }
                    .sumOf { it.price }
            }

    suspend fun getAppointmentById(id: Int): Appointment? =
        repository.getAppointmentById(id)

    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>> =
        repository.getCompletedAppointmentsBetweenDates(startDate, endDate)

    suspend fun insertAppointment(appointment: Appointment) {
        repository.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        repository.updateAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        repository.deleteAppointment(appointment)
    }

    fun getAllAppointments(): Flow<List<Appointment>> =
        repository.getAllAppointments()

    suspend fun insertCompletedAppointment(appointment: CompletedAppointment) {
        repository.insertCompletedAppointment(appointment)
    }

    suspend fun getCompletedAppointmentByOriginalId(originalId: Int): CompletedAppointment? =
        repository.getCompletedAppointmentByOriginalId(originalId)

    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> =
        repository.getCompletedAppointmentsByDate(date)

    // Additional appointment-related logic can be added here
} 