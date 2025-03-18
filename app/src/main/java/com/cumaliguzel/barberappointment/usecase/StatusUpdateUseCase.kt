package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StatusUpdateUseCase(private val appointmentRepository: AppointmentRepository) {

    suspend fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        if (newStatus == "Completed") {
            val existingCompletedAppointment = appointmentRepository.getCompletedAppointmentByOriginalId(appointment.id)
            if (existingCompletedAppointment == null) {
                val completedAppointment = CompletedAppointment(
                    originalAppointmentId = appointment.id,
                    name = appointment.name,
                    operation = appointment.operation,
                    date = appointment.date,
                    time = appointment.time,
                    price = appointment.price,
                    completedAt = LocalDateTime.now().toString()
                )
                appointmentRepository.safeInsertCompletedAppointment(completedAppointment)
            }
        }
        val updatedAppointment = appointment.copy(status = newStatus)
        appointmentRepository.updateAppointment(updatedAppointment)
    }

    suspend fun updateAppointmentStatuses(appointments: List<Appointment>) {
        appointments.forEach { appointment ->
            val newStatus = determineStatus(appointment)
            updateAppointmentStatus(appointment, newStatus)
        }
    }

    private fun determineStatus(appointment: Appointment): String {
        // Logic to determine the new status of the appointment
        // This is a placeholder and should be replaced with actual logic
        return if (LocalDateTime.now().isAfter(LocalDateTime.parse(appointment.date + "T" + appointment.time))) "Completed" else "Pending"
    }
} 