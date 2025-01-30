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
import com.cumaliguzel.barberappointment.data.OperationPrice
import java.util.concurrent.TimeUnit
import java.time.Duration
import com.cumaliguzel.barberappointment.worker.AppointmentNotificationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.cumaliguzel.barberappointment.data.CompletedAppointment
import kotlinx.coroutines.Delay
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import android.util.Log
import java.time.temporal.ChronoUnit

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppointmentRepository
    private val operationPriceRepository: OperationPriceRepository
    private val workManager: WorkManager
    private val _operationPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val context = application.applicationContext

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppointmentRepository(
            database.appointmentDao(),
            database.completedAppointmentDao()
        )
        operationPriceRepository = OperationPriceRepository(database.operationPriceDao())
        workManager = WorkManager.getInstance(context)
        
        viewModelScope.launch {
            operationPriceRepository.getAllOperationPrices().collect { prices ->
                _operationPrices.value = prices.associate { it.operation to it.price }
            }
        }
        
        createNotificationChannel()
        startAutoUpdateTimer()
    }

    val appointments: Flow<List<Appointment>> = repository.getAllAppointments()
        .map { appointments ->
            updateAppointmentStatuses(appointments)
            appointments
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val operationPrices: StateFlow<Map<String, Double>> = _operationPrices.asStateFlow()

    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> =
        repository.getAppointmentsByDate(date)

    fun getDailyEarnings(date: String): Flow<Double> =
        repository.getAppointmentsByDate(date)
            .map { appointments ->
                appointments
                    .filter { it.status == "Completed" }
                    .sumOf { it.price }
            }

    suspend fun getAppointmentById(id: Int): Appointment? =
        repository.getAppointmentById(id)

    private suspend fun getOperationPrice(operation: String): Double {
        return operationPriceRepository.getOperationPrice(operation)?.price ?: 0.0
    }

    fun saveOperationPrices(prices: Map<String, Double>) {
        viewModelScope.launch {
            try {
                prices.forEach { (operation, price) ->
                    operationPriceRepository.insertOperationPrice(
                        OperationPrice(operation = operation, price = price)
                    )
                }
                // Update the StateFlow with all prices, including existing ones
                _operationPrices.value = operationPriceRepository.getAllOperationPrices()
                    .first()
                    .associate { it.operation to it.price }
            } catch (e: Exception) {
                // Handle error - you might want to add error handling here
                e.printStackTrace()
            }
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val price = getOperationPrice(appointment.operation)
            val appointmentWithPrice = appointment.copy(price = price)
            repository.insertAppointment(appointmentWithPrice)
            scheduleNotification(appointmentWithPrice)
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            val price = getOperationPrice(appointment.operation)
            val appointmentWithPrice = appointment.copy(price = price)
            repository.updateAppointment(appointmentWithPrice)
            scheduleNotification(appointmentWithPrice)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            // Only delete from appointments table, keep completed record if exists
            repository.deleteAppointment(appointment)
            cancelNotification(appointment)
        }
    }

    private fun updateAppointmentStatuses(appointments: List<Appointment>) {
        viewModelScope.launch {
            val currentDateTime = LocalDateTime.now()
            appointments.forEach { appointment ->
                try {
                    val appointmentDateTime = LocalDateTime.parse(
                        "${appointment.date}T${appointment.time}",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                    )
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
                            repository.insertCompletedAppointment(completedAppointment)
                        }
                        repository.updateAppointment(appointment.copy(status = newStatus))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun startAutoUpdateTimer() {
        viewModelScope.launch {
            while (true) {
                try {
                    val appointments = repository.getAllAppointments().first()
                    updateAppointmentStatuses(appointments)
                    // 5 dakika bekle
                    kotlinx.coroutines.delay(5 * 60 * 1000L)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Randevu Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan randevular için bildirimler"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification(appointment: Appointment) {
        try {
            // Bildirim verilerini hazırla
            val notificationData = workDataOf(
                "appointmentId" to appointment.id,
                "customerName" to appointment.name,
                "operation" to appointment.operation,
                "time" to appointment.time
            )

            // Zaman bilgisindeki fazladan ":00" bilgisini kaldır
            val cleanTime = appointment.time.split(":").take(2).joinToString(":")

// Randevu zamanını parse et
            val appointmentDateTime = try {
                LocalDateTime.parse(
                    "${appointment.date}T$cleanTime",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                )
            } catch (e: Exception) {
                Log.e("Notification", "Tarih parse hatası: ${appointment.date}T${appointment.time}", e)
                return
            }


            // Bildirim zamanını hesapla (randevu saatinden 5 dakika önce)
            val notificationTime = appointmentDateTime.minusMinutes(5)
            val currentTime = LocalDateTime.now()
            
            // Eğer bildirim zamanı geçmişse bildirim gönderme
            if (notificationTime.isBefore(currentTime)) {
                Log.d("Notification", "Bildirim zamanı geçmiş: ${appointment.name}")
                return
            }

            // Bildirime kalan süreyi hesapla
            val delayInSeconds = ChronoUnit.SECONDS.between(currentTime, notificationTime)

            // Work request oluştur
            val notificationWork = OneTimeWorkRequestBuilder<AppointmentNotificationWorker>()
                .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                .setInputData(notificationData)
                .addTag("notification_${appointment.id}")
                .build()

            // Work Manager'a işi ekle
            workManager.enqueueUniqueWork(
                "notification_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )

            // Debug için log
            Log.d("Notification", "Bildirim planlandı: ${appointment.name}")
            Log.d("Notification", "Randevu saati: ${appointmentDateTime}")
            Log.d("Notification", "Bildirim saati: ${notificationTime}")
            Log.d("Notification", "Kalan süre: ${delayInSeconds} saniye")
            
        } catch (e: Exception) {
            Log.e("Notification", "Bildirim planlanırken hata: ${e.message}")
        }
    }

    private fun cancelNotification(appointment: Appointment) {
        workManager.cancelAllWorkByTag("appointment_notification_${appointment.id}")
    }

    fun updateAppointmentStatus(appointment: Appointment, newStatus: String) {
        viewModelScope.launch {
            if (newStatus == "Completed") {
                // Check if the appointment is already completed
                val existingCompletedAppointment = repository.getCompletedAppointmentByOriginalId(appointment.id)
                
                if (existingCompletedAppointment == null) {
                    // Only create CompletedAppointment if it doesn't exist
                    val completedAppointment = CompletedAppointment(
                        originalAppointmentId = appointment.id,
                        name = appointment.name,
                        operation = appointment.operation,
                        date = appointment.date,
                        time = appointment.time,
                        price = appointment.price,
                        completedAt = LocalDateTime.now().toString()
                    )
                    val inserted = repository.safeInsertCompletedAppointment(completedAppointment)
                    if (!inserted) {
                        // If insertion failed due to duplicate, just update the status
                        val updatedAppointment = appointment.copy(status = newStatus)
                        repository.updateAppointment(updatedAppointment)
                        return@launch
                    }
                }
            }
            
            // Update original appointment status
            val updatedAppointment = appointment.copy(status = newStatus)
            repository.updateAppointment(updatedAppointment)
        }
    }

    // Add function to check completion status
    private suspend fun isAppointmentCompleted(appointmentId: Int): Boolean {
        return repository.getCompletedAppointmentByOriginalId(appointmentId) != null
    }

    // Add function to get completed appointments for a date
    fun getCompletedAppointmentsByDate(date: String): Flow<List<CompletedAppointment>> {
        return repository.getCompletedAppointmentsByDate(date)
    }

    fun deleteOperation(operation: String) {
        viewModelScope.launch {
            try {
                operationPriceRepository.deleteOperationPrice(operation)
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    fun updateOperationPrice(operation: String, price: Double) {
        viewModelScope.launch {
            try {
                operationPriceRepository.updateOperationPrice(operation, price)
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    fun getWeeklyEarnings(): Flow<Double> = flow {
        val currentDate = LocalDate.now()
        val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = startOfWeek.plusDays(6)
        
        val weeklyEarnings = repository.getCompletedAppointmentsBetweenDates(
            startOfWeek.toString(),
            endOfWeek.toString()
        ).first().sumOf { it.price }
        
        emit(weeklyEarnings)
    }

    fun getMonthlyEarnings(): Flow<Double> = flow {
        try {
            val currentDate = LocalDate.now()
            val startOfMonth = currentDate.withDayOfMonth(1)
            val endOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth())
            
            val monthlyEarnings = repository.getCompletedAppointmentsBetweenDates(
                startOfMonth.toString(),
                endOfMonth.toString()
            ).first().sumOf { it.price }
            
            emit(monthlyEarnings)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(0.0)
        }
    }

    fun getWeeklyAppointmentsCount(): Flow<Int> = flow {
        val currentDate = LocalDate.now()
        val startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = startOfWeek.plusDays(6)
        
        val count = repository.getCompletedAppointmentsBetweenDates(
            startOfWeek.toString(),
            endOfWeek.toString()
        ).first().size
        
        emit(count)
    }

    fun getMonthlyAppointmentsCount(): Flow<Int> = flow {
        try {
            val currentDate = LocalDate.now()
            val startOfMonth = currentDate.withDayOfMonth(1)
            val endOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth())
            
            val count = repository.getCompletedAppointmentsBetweenDates(
                startOfMonth.toString(),
                endOfMonth.toString()
            ).first().size
            
            emit(count)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(0)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "appointment_notifications"
    }
} 