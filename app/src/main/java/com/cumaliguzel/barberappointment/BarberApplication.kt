package com.cumaliguzel.barberappointment

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.cumaliguzel.barberappointment.data.BarberDatabase
import com.cumaliguzel.barberappointment.repository.AppointmentRepository
import com.cumaliguzel.barberappointment.repository.CompletedAppointmentRepository
import com.cumaliguzel.barberappointment.repository.OperationPriceRepository
import com.cumaliguzel.barberappointment.usecase.AppointmentUseCase
import com.cumaliguzel.barberappointment.usecase.NotificationUseCase
import com.cumaliguzel.barberappointment.usecase.StatusUpdateUseCase
import com.cumaliguzel.barberappointment.usecase.EarningsUseCase
import com.cumaliguzel.barberappointment.usecase.AppointmentCountUseCase
import com.cumaliguzel.barberappointment.usecase.OperationManagementUseCase
import com.cumaliguzel.barberappointment.usecase.StatisticsUseCase
import android.content.SharedPreferences
import com.cumaliguzel.barberappointment.usecase.OperationPriceUseCase

class BarberApplication : Application(), Configuration.Provider {
    // Veritabanı
    val database: BarberDatabase by lazy { BarberDatabase.getDatabase(this) }
    
    // Repository örnekleri
    val appointmentRepository by lazy { 
        AppointmentRepository(database.appointmentDao(), database.completedAppointmentDao()) 
    }
    
    val completedAppointmentRepository by lazy {
        CompletedAppointmentRepository(database.completedAppointmentDao())
    }
    
    val operationPriceRepository by lazy {
        OperationPriceRepository(database.operationPriceDao())
    }
    
    // SharedPreferences
    val prefs: SharedPreferences by lazy {
        getSharedPreferences("barber_prefs", MODE_PRIVATE)
    }
    
    // UseCase örnekleri
    val appointmentUseCase by lazy {
        AppointmentUseCase(appointmentRepository, completedAppointmentRepository)
    }
    
    val statusUpdateUseCase by lazy {
        StatusUpdateUseCase(this, appointmentRepository, completedAppointmentRepository, prefs)
    }
    
    val earningsUseCase by lazy {
        EarningsUseCase(appointmentUseCase)
    }
    
    val appointmentCountUseCase by lazy {
        AppointmentCountUseCase(appointmentUseCase)
    }
    
    val operationManagementUseCase by lazy {
        OperationManagementUseCase(operationPriceRepository)
    }
    
    val operationPriceUseCase by lazy {
        OperationPriceUseCase(operationPriceRepository)
    }
    
    val statisticsUseCase by lazy {
        StatisticsUseCase(appointmentUseCase)
    }
    
    // WorkManager örneği
    val workManager by lazy { WorkManager.getInstance(this) }
    
    // NotificationUseCase örneği
    val notificationUseCase by lazy { NotificationUseCase(this, workManager) }
    
    // ⚠️ Önemli: Configuration.Provider interface'ini düzgün şekilde implement et
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
} 