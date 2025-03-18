package com.cumaliguzel.barberappointment.usecase

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class StatisticsUseCase(private val appointmentUseCase: AppointmentUseCase) {

    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Günlük hesaplamalar için önbellek
    private val dailyStatsCache: MutableMap<String, Flow<Int>> = mutableMapOf()
    
    // Haftalık veri önbelleği
    @OptIn(ExperimentalCoroutinesApi::class)
    private var weeklyStatsCache: Flow<List<Pair<LocalDate, Int>>>? = null
    private var weeklyCacheDate: LocalDate? = null

    // Bugünün tamamlanmış randevu sayısını getir
    fun getTodayCompletedAppointmentsCount(): Flow<Int> {
        val today = LocalDate.now().toString()
        // Cache'te varsa cache'ten döndür
        return dailyStatsCache.getOrPut(today) {
            flow {
                try {
                    val count = appointmentUseCase.getCompletedAppointmentsByDate(today).first().size
                    emit(count)
                } catch (e: Exception) {
                    Log.e("StatisticsUseCase", "Günlük tamamlanmış randevu sayısı hesaplanırken hata oluştu", e)
                    emit(0)
                }
            }.flowOn(Dispatchers.IO)
             .catch { e ->
                 Log.e("StatisticsUseCase", "Günlük veri akışında hata: ${e.message}")
                 emit(0)
             }
        }
    }

    // Haftalık olarak her gün için tamamlanmış randevu sayılarını getir
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWeeklyCompletedAppointmentsByDay(): Flow<List<Pair<LocalDate, Int>>> {
        val currentDate = LocalDate.now()
        
        // Cache geçerli değilse yeniden hesapla
        if (weeklyStatsCache == null || weeklyCacheDate != currentDate) {
            weeklyCacheDate = currentDate
            weeklyStatsCache = calculateWeeklyStats().flowOn(Dispatchers.IO)
                .catch { e ->
                    Log.e("StatisticsUseCase", "Haftalık veri akışında hata: ${e.message}")
                    emit(emptyList())
                }
                .shareIn(
                    cacheScope,
                    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                    replay = 1
                )
        }
        
        return weeklyStatsCache!!
    }
    
    private fun calculateWeeklyStats(): Flow<List<Pair<LocalDate, Int>>> = flow {
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
    fun getCompletedAppointmentsForDate(date: LocalDate): Flow<Int> {
        val dateStr = date.toString()
        // Cache'te varsa cache'ten döndür
        return dailyStatsCache.getOrPut(dateStr) {
            flow {
                try {
                    val count = appointmentUseCase.getCompletedAppointmentsByDate(dateStr).first().size
                    emit(count)
                } catch (e: Exception) {
                    Log.e("StatisticsUseCase", "Belirli gün için tamamlanmış randevu sayısı hesaplanırken hata oluştu", e)
                    emit(0)
                }
            }.flowOn(Dispatchers.IO)
             .catch { e ->
                 Log.e("StatisticsUseCase", "Günlük veri akışında hata: ${e.message}")
                 emit(0)
             }
        }
    }
    
    // Haftalık ve günlük cache'i temizle
    fun clearCache() {
        weeklyStatsCache = null
        weeklyCacheDate = null
        dailyStatsCache.clear()
    }
} 