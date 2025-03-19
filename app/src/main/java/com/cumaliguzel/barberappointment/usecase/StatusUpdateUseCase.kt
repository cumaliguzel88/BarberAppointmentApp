package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import java.time.LocalDateTime
import android.util.Log

class StatusUpdateUseCase(private val appointmentRepository: AppointmentRepository) {

    suspend fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        if (appointment.status != newStatus) {
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
                    val inserted = appointmentRepository.safeInsertCompletedAppointment(completedAppointment)
                    if (!inserted) {
                        Log.d("StatusUpdateUseCase", "Randevu zaten tamamlanmış olarak kaydedilmiş: ${appointment.id}")
                    } else {
                        Log.d("StatusUpdateUseCase", "Randevu tamamlandı olarak kaydedildi: ${appointment.id}")
                    }
                } else {
                    Log.d("StatusUpdateUseCase", "Bu randevu zaten tamamlanmış: ${appointment.id}")
                }
            }
            
            val updatedAppointment = appointment.copy(status = newStatus)
            appointmentRepository.updateAppointment(updatedAppointment)
            Log.d("StatusUpdateUseCase", "Randevu durumu güncellendi: ${appointment.id}, Yeni durum: $newStatus")
        } else {
            Log.d("StatusUpdateUseCase", "Randevu zaten '$newStatus' durumunda: ${appointment.id}")
        }
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