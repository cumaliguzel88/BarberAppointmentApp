package com.cumaliguzel.barberappointment.usecase

import android.util.Log
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import com.cumaliguzel.barberappointment.repository.CompletedAppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class AppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val completedAppointmentRepository: CompletedAppointmentRepository
) {
    companion object {
        private const val TAG = "AppointmentUseCase"
    }

    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        appointmentRepository.getAppointmentsByDate(date)
            .catch { e -> 
                Log.e(TAG, "💥 Randevuları alırken hata: ${e.message}", e)
                emit(emptyList())
            }

    fun getDailyEarnings(date: String): Flow<Double> =
        getCompletedAppointmentsByDate(date)
            .map { appointments ->
                appointments.sumOf { it.price }
            }
            .catch { e ->
                Log.e(TAG, "💥 Günlük kazanç hesaplanırken hata: ${e.message}", e)
                emit(0.0)
            }

    suspend fun getAppointmentById(id: Int): Appointment? {
        return try {
            appointmentRepository.getAppointmentById(id)
        } catch (e: Exception) {
            Log.e(TAG, "💥 ID ile randevu alınırken hata: $id", e)
            null
        }
    }

    fun getCompletedAppointmentsBetweenDates(startDate: String, endDate: String): Flow<List<CompletedAppointment>> =
        completedAppointmentRepository.getCompletedAppointmentsBetweenDates(startDate, endDate)
            .catch { e ->
                Log.e(TAG, "💥 Tarih aralığındaki tamamlanan randevuları alırken hata: $startDate - $endDate", e)
                emit(emptyList())
            }

    suspend fun insertAppointment(appointment: Appointment): Appointment {
        try {
            val newId = appointmentRepository.insertAppointment(appointment)
            // Yeni randevu nesnesi oluştur (ID ile güncellendi)
            val updatedAppointment = appointment.copy(id = newId.toInt())
            Log.d(TAG, "✅ Yeni randevu eklendi: ${updatedAppointment.name} (${updatedAppointment.date}, ${updatedAppointment.time}) ID: ${updatedAppointment.id}")
            return updatedAppointment
        } catch (e: Exception) {
            Log.e(TAG, "💥 Randevu eklenirken hata: ${e.message}", e)
            return appointment
        }
    }

    suspend fun updateAppointment(appointment: Appointment) {
        try {
            appointmentRepository.updateAppointment(appointment)
            Log.d(TAG, "✅ Randevu güncellendi: ID=${appointment.id}")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Randevu güncellenirken hata: ${e.message}", e)
        }
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        try {
            appointmentRepository.deleteAppointment(appointment)
            Log.d(TAG, "✅ Randevu silindi: ID=${appointment.id}")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Randevu silinirken hata: ${e.message}", e)
        }
    }

    fun getAllAppointments(): Flow<List<Appointment>> =
        appointmentRepository.getAllAppointments()
            .catch { e ->
                Log.e(TAG, "💥 Tüm randevuları alırken hata: ${e.message}", e)
                emit(emptyList())
            }

    suspend fun getCompletedAppointmentByOriginalId(originalId: Int): CompletedAppointment? {
        return try {
            completedAppointmentRepository.getCompletedAppointmentByOriginalId(originalId)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Original ID ile tamamlanan randevu alınırken hata: $originalId", e)
            null
        }
    }

    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> =
        completedAppointmentRepository.getCompletedAppointmentsByDate(date)
            .catch { e ->
                Log.e(TAG, "💥 Tarihe göre tamamlanan randevuları alırken hata: $date", e)
                emit(emptyList())
            }
} 