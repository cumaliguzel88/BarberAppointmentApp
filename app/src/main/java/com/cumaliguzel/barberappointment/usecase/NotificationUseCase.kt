package com.cumaliguzel.barberappointment.usecase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
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
                        NotificationManager.IMPORTANCE_HIGH // Yüksek öncelikli
                    ).apply {
                        description = "Yaklaşan randevular için bildirimler"
                        enableLights(true)
                        enableVibration(true)
                        setShowBadge(true) // Uygulama ikonunda bildirim rozeti göster
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

    /**
     * Android 10 ve daha eski sürümlerde bildirim ayarlarını kontrol eder
     * @return Bildirimler etkinse true, değilse false
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                false
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Kullanıcıyı uygulama bildirim ayarlarına yönlendirir
     */
    fun openNotificationSettings() {
        val intent = Intent()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ için kanal ayarlarına git
            intent.action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
        } else {
            // Android 8.0 öncesi için uygulama bildirim ayarlarına git
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        
        // Intent'i yeni bir aktivite olarak başlat
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun scheduleNotification(appointment: Appointment) {
        try {
            // Benzersiz bir ID oluştur
            val notificationId = System.currentTimeMillis().toInt()
            
            // Randevu zaten tamamlanmışsa bildirim gönderme
            if (appointment.status == "Completed") {
                Log.d(TAG, "⚠️ Tamamlanmış randevu için bildirim planlanmadı: ${appointment.id}")
                return
            }

            // Bildirim izinlerini kontrol et ve gerekirse iste
            if (!areNotificationsEnabled()) {
                requestNotificationPermission()
                Log.w(TAG, "⚠️ Bildirim izinleri eksik, izin isteniyor")
                return
            }
            
            val notificationData = workDataOf(
                "notificationId" to notificationId,
                "appointmentId" to appointment.id,
                "customerName" to appointment.name,
                "operation" to appointment.operation,
                "time" to appointment.time,
                "date" to appointment.date
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

            val workRequest = OneTimeWorkRequestBuilder<AppointmentNotificationWorker>()
                .setInputData(notificationData)
                .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "notification_${appointment.id}_${notificationId}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, """
                🎯 Bildirim iş kaydı oluşturuldu:
                ID: $notificationId
                Müşteri: ${appointment.name}
                Tarih: ${appointment.date}
                Saat: ${appointment.time}
                Gecikme: $delayInSeconds saniye
            """.trimIndent())

        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim planlanırken hata oluştu", e)
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Randevu Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan randevular için bildirimler"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "📢 Bildirim kanalı oluşturuldu")
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