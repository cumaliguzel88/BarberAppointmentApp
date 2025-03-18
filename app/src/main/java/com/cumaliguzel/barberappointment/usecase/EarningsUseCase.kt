package com.cumaliguzel.barberappointment.usecase

import com.cumaliguzel.barberappointment.data.Appointment
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

class EarningsUseCase(private val appointmentUseCase: AppointmentUseCase) {

    fun getWeeklyEarnings(): Flow<Double> = flow {
        try {
            // Geçerli tarihi al
            val currentDate = LocalDate.now()
            
            // Haftanın başlangıcını pazartesi olarak ayarla (geçerli haftanın pazartesi)
            val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            // Haftanın sonunu pazar olarak ayarla (pazartesiden 6 gün sonra)
            val endOfWeek = startOfWeek.plusDays(6)
            
            Log.d("EarningsUseCase", "Haftalık kazanç hesaplanıyor: $startOfWeek - $endOfWeek")
            
            // Belirtilen tarihler arasındaki tamamlanmış randevuları al ve fiyatlarını topla
            val weeklyEarnings = appointmentUseCase.getCompletedAppointmentsBetweenDates(
                startOfWeek.toString(),
                endOfWeek.toString()
            ).first().sumOf { it.price }
            
            emit(weeklyEarnings)
        } catch (e: Exception) {
            // İstisna türünü kontrol etme - mesaj içeriğinden varsayım yap
            if (e.message?.contains("composition") == true) {
                Log.i("EarningsUseCase", "Kompozisyon ile ilgili bir sorun oluştu: ${e.message}")
            } else {
                Log.e("EarningsUseCase", "Haftalık kazanç hesaplanırken hata oluştu", e)
            }
            emit(0.0)
        }
    }.flowOn(Dispatchers.IO)
     .catch { e ->
         // Akış seviyesinde hata yakalama
         if (e.message?.contains("composition") == true) {
             Log.i("EarningsUseCase", "Kompozisyon akışı ile ilgili bir sorun: ${e.message}")
         } else {
             Log.e("EarningsUseCase", "Haftalık kazanç akışında hata: ${e.message}")
         }
         emit(0.0)
     }

    fun getMonthlyEarnings(): Flow<Double> = flow {
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
            
            Log.d("EarningsUseCase", "Aylık kazanç hesaplanıyor: $startOfMonth - $endOfMonth")
            
            // Belirtilen tarihler arasındaki tamamlanmış randevuları al ve fiyatlarını topla
            val monthlyEarnings = appointmentUseCase.getCompletedAppointmentsBetweenDates(
                startOfMonth.toString(),
                endOfMonth.toString()
            ).first().sumOf { it.price }
            
            emit(monthlyEarnings)
        } catch (e: Exception) {
            // İstisna türünü kontrol etme - mesaj içeriğinden varsayım yap
            if (e.message?.contains("composition") == true) {
                Log.i("EarningsUseCase", "Kompozisyon ile ilgili bir sorun oluştu: ${e.message}")
            } else {
                Log.e("EarningsUseCase", "Aylık kazanç hesaplanırken hata oluştu", e)
            }
            emit(0.0)
        }
    }.flowOn(Dispatchers.IO)
     .catch { e ->
         // Akış seviyesinde hata yakalama
         if (e.message?.contains("composition") == true) {
             Log.i("EarningsUseCase", "Kompozisyon akışı ile ilgili bir sorun: ${e.message}")
         } else {
             Log.e("EarningsUseCase", "Aylık kazanç akışında hata: ${e.message}")
         }
         emit(0.0)
     }
} 