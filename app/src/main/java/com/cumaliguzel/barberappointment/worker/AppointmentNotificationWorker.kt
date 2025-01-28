package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel

class AppointmentNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            val appointmentId = inputData.getInt("appointmentId", -1)
            val customerName = inputData.getString("customerName") ?: return Result.failure()
            val operation = inputData.getString("operation") ?: return Result.failure()
            val time = inputData.getString("time") ?: return Result.failure()

            showNotification(appointmentId, customerName, operation, time)
            Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Bildirim gönderilemedi: ${e.message}")
            Result.failure()
        }
    }

    private fun showNotification(appointmentId: Int, customerName: String, operation: String, time: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, AppointmentViewModel.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 5 Dakika Kaldı!")
            .setContentText("$customerName - $operation saat $time başlayacak")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(1000, 500, 1000)) // 1 sn titreşim, 0.5 sn durma, 1 sn titreşim
            .build()

        try {
            notificationManager.notify(appointmentId, notification)
            Log.d("NotificationWorker", "Bildirim başarıyla gönderildi: $customerName için $time")
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Bildirim gönderilirken hata: ${e.message}")
        }
    }
} 