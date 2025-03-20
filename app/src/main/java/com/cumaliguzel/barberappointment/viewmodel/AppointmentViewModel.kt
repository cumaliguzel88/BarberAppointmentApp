package com.cumaliguzel.barberappointment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cumaliguzel.barberappointment.data.AppDatabase
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import com.cumaliguzel.barberappointment.repository.OperationPriceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import java.time.LocalDate
import android.util.Log
import com.cumaliguzel.barberappointment.usecase.AppointmentUseCase
import com.cumaliguzel.barberappointment.usecase.OperationPriceUseCase
import com.cumaliguzel.barberappointment.usecase.NotificationUseCase
import com.cumaliguzel.barberappointment.usecase.EarningsUseCase
import com.cumaliguzel.barberappointment.usecase.AppointmentCountUseCase
import com.cumaliguzel.barberappointment.usecase.StatusUpdateUseCase
import com.cumaliguzel.barberappointment.usecase.OperationManagementUseCase
import com.cumaliguzel.barberappointment.usecase.StatisticsUseCase
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay as coroutinesDelay
import kotlinx.coroutines.CancellationException

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val appointmentUseCase: AppointmentUseCase
    private val operationPriceUseCase: OperationPriceUseCase
    private val notificationUseCase: NotificationUseCase
    private val earningsUseCase: EarningsUseCase
    private val appointmentCountUseCase: AppointmentCountUseCase
    private val statusUpdateUseCase: StatusUpdateUseCase
    private val operationManagementUseCase: OperationManagementUseCase
    private val statisticsUseCase: StatisticsUseCase
    private val workManager: WorkManager
    private val _operationPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val context = application.applicationContext
    
    // İstatistikler için selected date state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private var autoUpdateJob: kotlinx.coroutines.Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        appointmentUseCase = AppointmentUseCase(
            AppointmentRepository(
                database.appointmentDao(),
                database.completedAppointmentDao()
            )
        )
        operationPriceUseCase = OperationPriceUseCase(
            OperationPriceRepository(database.operationPriceDao())
        )
        workManager = WorkManager.getInstance(context)
        notificationUseCase = NotificationUseCase(context, workManager)
        earningsUseCase = EarningsUseCase(appointmentUseCase)
        appointmentCountUseCase = AppointmentCountUseCase(appointmentUseCase)
        statusUpdateUseCase = StatusUpdateUseCase(appointmentUseCase.repository)
        operationManagementUseCase = OperationManagementUseCase(operationPriceUseCase.operationPriceRepository)
        statisticsUseCase = StatisticsUseCase(appointmentUseCase)
        
        viewModelScope.launch {
            operationPriceUseCase.getAllOperationPrices().collect { prices ->
                _operationPrices.value = prices
            }
        }
        
        notificationUseCase.createNotificationChannel(NOTIFICATION_CHANNEL_ID)
        startAutoUpdateTimer()
    }

    val appointments: Flow<List<Appointment>> = appointmentUseCase.getAllAppointments()
        .map { appointments ->
            try {
                updateAppointmentStatuses(appointments)
                appointments
            } catch (e: Exception) {
                Log.e("AppointmentViewModel", "Error updating appointment statuses", e)
                appointments
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val operationPrices: StateFlow<Map<String, Double>> = _operationPrices.asStateFlow()

    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        appointmentUseCase.getAppointmentsByDate(date)

    fun getDailyEarnings(date: String): Flow<Double> =
        appointmentUseCase.getDailyEarnings(date)

    suspend fun getAppointmentById(id: Int): Appointment? =
        appointmentUseCase.getAppointmentById(id)

    private suspend fun getOperationPrice(operation: String): Double {
        return operationPriceUseCase.getOperationPrice(operation)
    }

    fun saveOperationPrices(prices: Map<String, Double>) {
        viewModelScope.launch {
            try {
                operationPriceUseCase.saveOperationPrices(prices)
            } catch (e: Exception) {
                Log.e("AppointmentViewModel", "Error saving operation prices", e)
            }
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val price = getOperationPrice(appointment.operation)
            val appointmentWithPrice = appointment.copy(price = price)
            appointmentUseCase.insertAppointment(appointmentWithPrice)
            notificationUseCase.scheduleNotification(appointmentWithPrice)
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val price = getOperationPrice(appointment.operation)
            val appointmentWithPrice = appointment.copy(price = price)
            appointmentUseCase.updateAppointment(appointmentWithPrice)
            notificationUseCase.scheduleNotification(appointmentWithPrice)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentUseCase.deleteAppointment(appointment)
            notificationUseCase.cancelNotification(appointment)
        }
    }

    private fun updateAppointmentStatuses(appointments: List<Appointment>) {
        viewModelScope.launch {
            try {
                val currentDateTime = LocalDateTime.now()
                appointments.forEach { appointment ->
                    try {
                        // Tarih ve zaman bilgisini birleştir
                        val dateTimeString = "${appointment.date}T${appointment.time}"
                        
                        // Milisaniye içeren formatı işleyecek daha iyi bir yaklaşım
                        val appointmentDateTime = when {
                            dateTimeString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) -> {
                                // Sadece saat ve dakika içeren format: HH:mm
                                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                            }
                            dateTimeString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) -> {
                                // Saniye içeren format: HH:mm:ss
                                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                            }
                            dateTimeString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}")) -> {
                                // Milisaniye içeren format: HH:mm:ss.SSS...
                                try {
                                    LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                } catch (e: Exception) {
                                    // ISO formatı ile ayrıştırma başarısız olursa
                                    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                                    LocalDateTime.parse(dateTimeString.substring(0, Math.min(23, dateTimeString.length)), pattern)
                                }
                            }
                            else -> {
                                // Diğer tüm durumlar için en basit format ile dene
                                Log.w("AppointmentViewModel", "Bilinmeyen zaman formatı: $dateTimeString, basit format deneniyor")
                                LocalDateTime.parse("${appointment.date}T${appointment.time.split('.')[0]}", 
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                            }
                        }
                        
                        val autoCompleteTime = appointmentDateTime.plusMinutes(31)
                        val newStatus = if (currentDateTime.isAfter(autoCompleteTime)) {
                            "Completed"
                        } else {
                            "Pending"
                        }
                        
                        if (appointment.status != newStatus) {
                            if (newStatus == "Completed") {
                                val completedAppointment = CompletedAppointment(
                                    originalAppointmentId = appointment.id,
                                    name = appointment.name,
                                    operation = appointment.operation,
                                    date = appointment.date,
                                    time = appointment.time,
                                    price = appointment.price,
                                    completedAt = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                )
                                appointmentUseCase.insertCompletedAppointment(completedAppointment)
                            }
                            appointmentUseCase.updateAppointment(appointment.copy(status = newStatus))
                        }
                    } catch (e: Exception) {
                        Log.e("AppointmentViewModel", "Error updating appointment status: ${appointment.id}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("AppointmentViewModel", "Error in updateAppointmentStatuses", e)
            }
        }
    }

    private fun startAutoUpdateTimer() {
        autoUpdateJob?.cancel() // Varolan işi iptal et
        
        autoUpdateJob = viewModelScope.launch {
            try {
                while (isActive) {
                    try {
                        val appointments = appointmentUseCase.getAllAppointments().first()
                        updateAppointmentStatuses(appointments)
                        coroutinesDelay(5 * 60 * 1000L) // 5 dakika bekle
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            Log.d("AppointmentViewModel", "🔄 Otomatik güncelleme normal şekilde durduruldu")
                            break
                        } else {
                            Log.e("AppointmentViewModel", "❌ Otomatik güncelleme hatası", e)
                            coroutinesDelay(30 * 1000L) // Hata durumunda 30 saniye bekle
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("AppointmentViewModel", "💥 Kritik güncelleme hatası", e)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel temizlendiğinde auto update job'ı iptal et
        autoUpdateJob?.cancel()
        Log.d("AppointmentViewModel", "ViewModel cleared, auto update job cancelled")
    }

    fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        viewModelScope.launch {
            statusUpdateUseCase.updateAppointmentStatus(appointment, newStatus)
        }
    }

    // Add function to check completion status
    private suspend fun isAppointmentCompleted(appointmentId: Int): Boolean {
        return appointmentUseCase.getCompletedAppointmentByOriginalId(appointmentId) != null
    }

    // Add function to get completed appointments for a date
    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> {
        return appointmentUseCase.getCompletedAppointmentsByDate(date)
    }

    fun deleteOperation(operation: String) {
        viewModelScope.launch {
            operationManagementUseCase.deleteOperation(operation)
        }
    }

    fun updateOperationPrice(operation: String, price: Double) {
        viewModelScope.launch {
            operationManagementUseCase.updateOperationPrice(operation, price)
        }
    }

    fun getWeeklyEarnings(): Flow<Double> = earningsUseCase.getWeeklyEarnings()

    fun getMonthlyEarnings(): Flow<Double> = earningsUseCase.getMonthlyEarnings()

    fun getWeeklyAppointmentsCount(): Flow<Int> = appointmentCountUseCase.getWeeklyAppointmentsCount()

    fun getMonthlyAppointmentsCount(): Flow<Int> = appointmentCountUseCase.getMonthlyAppointmentsCount()

    // İstatistikler için fonksiyonlar
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun getTodayCompletedAppointmentsCount(): Flow<Int> = 
        statisticsUseCase.getTodayCompletedAppointmentsCount()
        
    fun getWeeklyCompletedAppointmentsByDay(): Flow<List<Pair<LocalDate, Int>>> = 
        statisticsUseCase.getWeeklyCompletedAppointmentsByDay()
        
    fun getCompletedAppointmentsForDate(date: LocalDate): Flow<Int> = 
        statisticsUseCase.getCompletedAppointmentsForDate(date)

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "appointment_notifications"
    }
} 