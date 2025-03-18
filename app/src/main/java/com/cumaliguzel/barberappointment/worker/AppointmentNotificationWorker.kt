package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.MainActivity
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel

class AppointmentNotificationWorker(
    private val context: Context,
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

        // Notification channel creation - only needed on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppointmentViewModel.NOTIFICATION_CHANNEL_ID,
                "Randevu Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan randevular için bildirimler"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent for opening the app when notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(applicationContext, appointmentId, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            // Android 6.0 (API 23) altı sürümler için FLAG_IMMUTABLE kullanılamaz, 
            // ancak Android 12+ hedefliyorsak lint hatası almamak için yine de flag belirtmeliyiz
            PendingIntent.getActivity(applicationContext, appointmentId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(applicationContext, AppointmentViewModel.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 5 Dakika Kaldı!")
            .setContentText("$customerName - $operation saat $time başlayacak")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For Android 7.1 and lower
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
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