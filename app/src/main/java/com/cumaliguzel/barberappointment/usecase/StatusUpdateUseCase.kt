package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import java.time.LocalDateTime
import android.util.Log

class StatusUpdateUseCase(private val appointmentRepository: AppointmentRepository) {

    suspend fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        try {
            Log.d("StatusUpdateUseCase", "🔄 Randevu durumu güncelleme başladı - ID: ${appointment.id}")

            if (appointment.status == newStatus) {
                Log.d("StatusUpdateUseCase", "ℹ️ Randevu zaten '$newStatus' durumunda - ID: ${appointment.id}")
                return
            }

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
                    if (inserted) {
                        Log.d("StatusUpdateUseCase", "✅ Randevu tamamlandı olarak kaydedildi - ID: ${appointment.id}")
                    } else {
                        Log.w("StatusUpdateUseCase", "⚠️ Randevu tamamlandı olarak kaydedilemedi - ID: ${appointment.id}")
                        return
                    }
                } else {
                    Log.d("StatusUpdateUseCase", "ℹ️ Randevu zaten tamamlanmış - ID: ${appointment.id}")
                }
            }

            val updatedAppointment = appointment.copy(status = newStatus)
            appointmentRepository.updateAppointment(updatedAppointment)
            Log.d("StatusUpdateUseCase", "✅ Randevu durumu güncellendi - ID: ${appointment.id}, Yeni durum: $newStatus")

        } catch (e: Exception) {
            Log.e("StatusUpdateUseCase", "💥 Randevu durumu güncellenirken hata oluştu - ID: ${appointment.id}", e)
            throw e
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