package com.cumaliguzel.barberappointment.usecase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.worker.AppointmentNotificationWorker
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class NotificationUseCase(private val context: Context, private val workManager: WorkManager) {

    companion object {
        private const val TAG = "NotificationUseCase"
        const val NOTIFICATION_CHANNEL_ID = "appointment_notifications"
    }

    /**
     * Bildirim kanalı oluşturur. Bu metod sadece Android O (API 26) ve üzeri sürümlerde 
     * bildirim kanalı oluşturur, daha eski sürümlerde herhangi bir işlem yapmaz.
     */
    fun createNotificationChannel(channelId: String) {
        // Bildirim kanalları Android 8.0 (API 26) ve üzerinde gereklidir
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Kanal zaten mevcut mu kontrol et
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    Log.d(TAG, "Bildirim kanalı oluşturuluyor: $channelId")
                    val channel = NotificationChannel(
                        channelId,
                        "Randevu Bildirimleri",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Yaklaşan randevular için bildirimler"
                        enableLights(true)
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                } else {
                    Log.d(TAG, "Bildirim kanalı zaten mevcut: $channelId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Bildirim kanalı oluşturulurken hata: ${e.message}")
            }
        } else {
            Log.d(TAG, "Android 8.0 altında bildirim kanalı gerekmez")
        }
    }

    fun scheduleNotification(appointment: Appointment) {
        try {
            val notificationData = workDataOf(
                "appointmentId" to appointment.id,
                "customerName" to appointment.name,
                "operation" to appointment.operation,
                "time" to appointment.time
            )

            // Temiz zaman formatını al (milisaniye olmadan)
            val cleanTime = getCleanTime(appointment.time)

            val appointmentDateTime = try {
                LocalDateTime.parse(
                    "${appointment.date}T$cleanTime",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Tarih parse hatası: ${appointment.date}T${appointment.time}", e)
                return
            }

            val notificationTime = appointmentDateTime.minusMinutes(5)
            val currentTime = LocalDateTime.now()

            if (notificationTime.isBefore(currentTime)) {
                Log.d(TAG, "Bildirim zamanı geçmiş: ${appointment.name}")
                return
            }

            val delayInSeconds = ChronoUnit.SECONDS.between(currentTime, notificationTime)

            val notificationWork = OneTimeWorkRequestBuilder<AppointmentNotificationWorker>()
                .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                .setInputData(notificationData)
                .addTag("notification_${appointment.id}")
                .build()

            workManager.enqueueUniqueWork(
                "notification_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )

            Log.d(TAG, "Bildirim planlandı: ${appointment.name}")
            Log.d(TAG, "Randevu saati: ${appointmentDateTime}")
            Log.d(TAG, "Bildirim saati: ${notificationTime}")
            Log.d(TAG, "Kalan süre: ${delayInSeconds} saniye")

        } catch (e: Exception) {
            Log.e(TAG, "Bildirim planlanırken hata: ${e.message}", e)
        }
    }

    private fun getCleanTime(time: String): String {
        return try {
            // Önce milisaniye veya saniye kısmını temizleyelim
            val parts = time.split(":")
            if (parts.size >= 2) {
                // Sadece saat ve dakika kısmını alalım (HH:mm)
                "${parts[0]}:${parts[1]}"
            } else {
                // Tam bir saat:dakika:saniye formatında değilse, olduğu gibi dönelim
                time
            }
        } catch (e: Exception) {
            Log.e(TAG, "Zaman temizlenirken hata: $time", e)
            time // Hata durumunda orijinal değeri döndür
        }
    }

    fun cancelNotification(appointment: Appointment) {
        try {
            workManager.cancelAllWorkByTag("notification_${appointment.id}")
            Log.d(TAG, "Bildirim iptal edildi: ${appointment.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim iptal edilirken hata: ${e.message}", e)
        }
    }
} 