package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.MainActivity
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel

class AppointmentNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "appointment_notifications"
        private const val DEBUG_TAG = "NotificationWorker"
    }

    override fun doWork(): Result {
        return try {
            val appointmentId = inputData.getInt("appointmentId", -1)
            val customerName = inputData.getString("customerName") ?: return Result.failure()
            val operation = inputData.getString("operation") ?: return Result.failure()
            val time = inputData.getString("time") ?: return Result.failure()

            // Debug log ekleyelim
            Log.d(DEBUG_TAG, "doWork çağrıldı: $appointmentId, $customerName, $operation, $time")
            
            // Bildirim kanalını oluştur 
            createNotificationChannel()
            
            // Bildirimi göster
            val success = showNotification(appointmentId, customerName, operation, time)
            
            if (success) {
                Log.d(DEBUG_TAG, "Bildirim başarıyla gönderildi")
                Result.success()
            } else {
                Log.e(DEBUG_TAG, "Bildirim gönderilemedi")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "Bildirim gönderilemedi: ${e.message}", e)
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        // Android 8.0+ için kanal oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Randevu Bildirimleri"
            val descriptionText = "Yaklaşan randevular için bildirimler"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            
            // Kanalı sisteme kaydet
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(DEBUG_TAG, "Bildirim kanalı oluşturuldu: $CHANNEL_ID")
        }
    }

    private fun showNotification(appointmentId: Int, customerName: String, operation: String, time: String): Boolean {
        try {
            // Ana aktiviteye gidecek intent
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            // Android sürümüne göre PendingIntent oluştur
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(context, appointmentId, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(context, appointmentId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            // Bildirimi oluştur
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ 5 Dakika Kaldı!")
                .setContentText("$customerName - $operation saat $time başlayacak")
                .setPriority(NotificationCompat.PRIORITY_MAX) // Maksimum öncelik
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(1000, 500, 1000)) // Titreşim paterni
            
            // Bildirim göster
            val notificationManager = NotificationManagerCompat.from(context)
            
            // Bildirimi göster
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ için izin kontrolü yapmamız gerekiyor
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(appointmentId, builder.build())
                    Log.d(DEBUG_TAG, "Bildirim gönderildi (API 33+): $customerName için $time")
                    return true
                } else {
                    Log.e(DEBUG_TAG, "Bildirim izni yok (API 33+)")
                    return false
                }
            } else {
                // Android 13 altı için
                notificationManager.notify(appointmentId, builder.build())
                Log.d(DEBUG_TAG, "Bildirim gönderildi: $customerName için $time")
                return true
            }
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "Bildirim gönderilirken hata: ${e.message}", e)
            return false
        }
    }
} 