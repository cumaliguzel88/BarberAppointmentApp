package com.cumaliguzel.barberappointment.usecase

import android.util.Log
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class StatisticsUseCase(private val appointmentUseCase: AppointmentUseCase) {

    // Bugünün tamamlanmış randevu sayısını getir
    fun getTodayCompletedAppointmentsCount(): Flow<Int> = flow {
        try {
            val today = LocalDate.now().toString()
            val count = appointmentUseCase.getCompletedAppointmentsByDate(today).first().size
            emit(count)
        } catch (e: Exception) {
            Log.e("StatisticsUseCase", "Günlük tamamlanmış randevu sayısı hesaplanırken hata oluştu", e)
            emit(0)
        }
    }

    // Haftalık olarak her gün için tamamlanmış randevu sayılarını getir
    fun getWeeklyCompletedAppointmentsByDay(): Flow<List<Pair<LocalDate, Int>>> = flow {
        try {
            val currentDate = LocalDate.now()
            val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = startOfWeek.plusDays(6)

            Log.d("StatisticsUseCase", "Haftalık randevu istatistikleri hesaplanıyor: $startOfWeek - $endOfWeek")

            // Tüm haftalık tamamlanmış randevuları al
            val allCompletedAppointments = appointmentUseCase.getCompletedAppointmentsBetweenDates(
                startOfWeek.toString(),
                endOfWeek.toString()
            ).first()

            // Her gün için randevu sayısını hesapla
            val weeklyStats = mutableListOf<Pair<LocalDate, Int>>()
            var currentDay = startOfWeek

            while (!currentDay.isAfter(endOfWeek)) {
                val dayStr = currentDay.toString()
                val appointmentsForDay = allCompletedAppointments.count { it.date == dayStr }
                weeklyStats.add(Pair(currentDay, appointmentsForDay))
                currentDay = currentDay.plusDays(1)
            }

            emit(weeklyStats)
        } catch (e: Exception) {
            Log.e("StatisticsUseCase", "Haftalık randevu istatistikleri hesaplanırken hata oluştu", e)
            emit(emptyList())
        }
    }

    // Belirli bir gün için tamamlanmış randevu sayısını getir
    fun getCompletedAppointmentsForDate(date: LocalDate): Flow<Int> = flow {
        try {
            val count = appointmentUseCase.getCompletedAppointmentsByDate(date.toString()).first().size
            emit(count)
        } catch (e: Exception) {
            Log.e("StatisticsUseCase", "Belirli gün için tamamlanmış randevu sayısı hesaplanırken hata oluştu", e)
            emit(0)
        }
    }
} 