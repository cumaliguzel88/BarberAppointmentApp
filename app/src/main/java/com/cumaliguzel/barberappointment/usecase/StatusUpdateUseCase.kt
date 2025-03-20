package com.cumaliguzel.barberappointment.usecase

import android.content.Context
import android.util.Log
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import com.cumaliguzel.barberappointment.repository.CompletedAppointmentRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.content.SharedPreferences

class StatusUpdateUseCase(
    private val context: Context,
    private val appointmentRepository: AppointmentRepository,
    private val completedAppointmentRepository: CompletedAppointmentRepository,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val TAG = "StatusUpdateUseCase"
    }

    suspend fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        try {
            Log.d(TAG, "🔄 Durum güncellemesi başlatıldı - ID: ${appointment.id}, Yeni Durum: $newStatus")
            
            // İşlem ID'si oluştur (işlemin daha önce yapılıp yapılmadığını takip etmek için)
            val operationId = "status_update_${appointment.id}_${newStatus}"
            
            // Daha önce bu işlem yapıldı mı kontrol et
            if (prefs.getBoolean(operationId, false)) {
                Log.d(TAG, "ℹ️ Bu durum güncellemesi zaten yapılmış - ID: ${appointment.id}, Durum: $newStatus")
                return
            }
            
            // Eğer Completed durumu ise, tamamlanan randevular tablosuna ekle
            if (newStatus == "Completed") {
                // Daha önce tamamlanmış mı kontrol et
                val existingAppointment = completedAppointmentRepository.getCompletedAppointmentByOriginalId(appointment.id)
                
                if (existingAppointment == null) {
                    // Tamamlanmış randevu ekle
                    val completedAppointment = CompletedAppointment(
                        originalAppointmentId = appointment.id,
                        name = appointment.name,
                        operation = appointment.operation,
                        date = appointment.date,
                        time = appointment.time,
                        price = appointment.price,
                        completedAt = LocalDateTime.now().toString()
                    )

                    // Eklerken thread-safe metodu kullan
                    val inserted = completedAppointmentRepository.safeInsertCompletedAppointment(completedAppointment)
                    
                    if (inserted) {
                        Log.d(TAG, "✅ Randevu tamamlandı olarak kaydedildi - ID: ${appointment.id}")
                        // İşlemi kaydı - başarılı
                        prefs.edit().putBoolean(operationId, true).apply()
                    } else {
                        Log.w(TAG, "⚠️ Randevu tamamlandı olarak kaydedilemedi - ID: ${appointment.id}")
                        return  // Tamamlanan randevu eklenemedi, durumu değiştirme
                    }
                } else {
                    Log.d(TAG, "ℹ️ Randevu zaten tamamlanmış - ID: ${appointment.id}")
                    // İşlem kaydı - zaten mevcut
                    prefs.edit().putBoolean(operationId, true).apply()
                }
            }

            // Randevunun durumunu güncelle
            val updatedAppointment = appointment.copy(status = newStatus)
            appointmentRepository.updateAppointment(updatedAppointment)
            Log.d(TAG, "✅ Randevu durumu güncellendi - ID: ${appointment.id}, Yeni durum: $newStatus")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Durum güncellenirken hata: ${e.message}", e)
        }
    }

    suspend fun updateAppointmentStatuses(appointments: List<Appointment>) {
        appointments.forEach { appointment ->
            try {
                val newStatus = determineStatus(appointment)
                if (appointment.status != newStatus) {
                    updateAppointmentStatus(appointment, newStatus)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Toplu güncelleme hatası - ID: ${appointment.id}", e)
            }
        }
    }

    private fun determineStatus(appointment: Appointment): String {
        try {
            // Önce standart format ile dene (HH:mm:ss)
            val dateTimeStr = "${appointment.date}T${appointment.time}"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            
            val appointmentDateTime = try {
                LocalDateTime.parse(dateTimeStr, formatter)
            } catch (e: DateTimeParseException) {
                // HH:mm formatını dene
                try {
                    val simpleTimeStr = appointment.time.split(":").take(2).joinToString(":")
                    val simpleFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                    LocalDateTime.parse("${appointment.date}T$simpleTimeStr", simpleFormatter)
                } catch (e2: Exception) {
                    Log.e(TAG, "💥 Tarih formatı çözümlenemedi: $dateTimeStr", e2)
                    return appointment.status // Hata durumunda mevcut durumu koru
                }
            }
            
            return if (LocalDateTime.now().isAfter(appointmentDateTime)) "Completed" else "Pending"
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Durum belirleme hatası", e)
            return appointment.status // Hata durumunda mevcut durumu koru
        }
    }
} 