package com.cumaliguzel.barberappointment.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import android.util.Log

class AppointmentCountUseCase(private val appointmentUseCase: AppointmentUseCase) {

    fun getWeeklyAppointmentsCount(): Flow<Int> = flow {
        try {
            // Geçerli tarihi al
            val currentDate = LocalDate.now()
            
            // Haftanın başlangıcını pazartesi olarak ayarla (geçerli haftanın pazartesi)
            val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            // Haftanın sonunu pazar olarak ayarla (pazartesiden 6 gün sonra)
            val endOfWeek = startOfWeek.plusDays(6)
            
            Log.d("AppointmentCountUseCase", "Haftalık randevu sayısı hesaplanıyor: $startOfWeek - $endOfWeek")
            
            // Belirtilen tarihler arasındaki tamamlanmış randevuları al ve say
            val count = appointmentUseCase.getCompletedAppointmentsBetweenDates(
                startOfWeek.toString(),
                endOfWeek.toString()
            ).first().size
            
            emit(count)
        } catch (e: Exception) {
            // Hata türünü mesajından kontrol et
            if (e.message?.contains("composition") == true) {
                Log.i("AppointmentCountUseCase", "Kompozisyon ile ilgili sorun: ${e.message}")
            } else {
                Log.e("AppointmentCountUseCase", "Haftalık randevu sayısı hesaplanırken hata oluştu", e)
            }
            emit(0)
        }
    }.flowOn(Dispatchers.IO)
     .catch { e ->
         // Akış seviyesinde hata yakalama
         if (e.message?.contains("composition") == true) {
             Log.i("AppointmentCountUseCase", "Kompozisyon akışı ile ilgili sorun: ${e.message}")
         } else {
             Log.e("AppointmentCountUseCase", "Haftalık randevu sayısı akışında hata: ${e.message}")
         }
         emit(0)
     }

    fun getMonthlyAppointmentsCount(): Flow<Int> = flow {
        try {
            // Geçerli tarihi al
            val currentDate = LocalDate.now()
            
            // Ayın başlangıcını ayarla (ayın 1'i)
            val startOfMonth = currentDate.withDayOfMonth(1)
            
            // Ayın sonunu 30 olarak ayarla
            // Eğer ay 30 günden azsa, ayın son gününü kullan
            val lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
            val endDayOfMonth = if (lastDayOfMonth > 30) 30 else lastDayOfMonth
            val endOfMonth = currentDate.withDayOfMonth(endDayOfMonth)
            
            Log.d("AppointmentCountUseCase", "Aylık randevu sayısı hesaplanıyor: $startOfMonth - $endOfMonth")
            
            // Belirtilen tarihler arasındaki tamamlanmış randevuları al ve say
            val count = appointmentUseCase.getCompletedAppointmentsBetweenDates(
                startOfMonth.toString(),
                endOfMonth.toString()
            ).first().size
            
            emit(count)
        } catch (e: Exception) {
            // Hata türünü mesajından kontrol et
            if (e.message?.contains("composition") == true) {
                Log.i("AppointmentCountUseCase", "Kompozisyon ile ilgili sorun: ${e.message}")
            } else {
                Log.e("AppointmentCountUseCase", "Aylık randevu sayısı hesaplanırken hata oluştu", e)
            }
            emit(0)
        }
    }.flowOn(Dispatchers.IO)
     .catch { e ->
         // Akış seviyesinde hata yakalama
         if (e.message?.contains("composition") == true) {
             Log.i("AppointmentCountUseCase", "Kompozisyon akışı ile ilgili sorun: ${e.message}")
         } else {
             Log.e("AppointmentCountUseCase", "Aylık randevu sayısı akışında hata: ${e.message}")
         }
         emit(0)
     }
} 