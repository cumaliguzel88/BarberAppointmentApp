package com.cumaliguzel.barberappointment.usecase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cumaliguzel.barberappointment.data.Appointment
import com.cumaliguzel.barberappointment.worker.AppointmentNotificationWorker
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class NotificationUseCase(private val context: Context, private val workManager: WorkManager) {

    companion object {
        private const val TAG = "NotificationUseCase"
        const val NOTIFICATION_CHANNEL_ID = "appointment_notifications"
    }

    /**
     * Bildirim kanalı oluşturur. Bu metod sadece Android O (API 26) ve üzeri sürümlerde 
     * bildirim kanalı oluşturur, daha eski sürümlerde herhangi bir işlem yapmaz.
     */
    fun createNotificationChannel() {
        try {
            // Android 8.0 ve üzeri için bildirim kanalı gerekmekte
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Mevcut kanalı kontrol et
                val existingChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
                if (existingChannel != null) {
                    Log.d(TAG, "Bildirim kanalı zaten mevcut: $NOTIFICATION_CHANNEL_ID")
                    return
                }
                
                // Yüksek öncelikli bildirim kanalı oluştur
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Randevu Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Yaklaşan randevular için bildirimler"
                }
                
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Bildirim kanalı oluşturuldu: $NOTIFICATION_CHANNEL_ID")
            } else {
                Log.d(TAG, "Android 8.0 altında bildirim kanalı gerekmiyor")
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim kanalı oluşturulurken hata: ${e.message}", e)
        }
    }

    /**
     * Android 10 ve daha eski sürümlerde bildirim ayarlarını kontrol eder
     * @return Bildirimler etkinse true, değilse false
     */
    fun areNotificationsEnabled(): Boolean {
        // Android 13+ için bildirim izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            val hasPermission = permissionStatus == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d(TAG, if (hasPermission) "✅ Bildirim izni mevcut (Android 13+)" else "⚠️ Bildirim izni yok (Android 13+)")
            return hasPermission
        }
        
        // Android 8+ için bildirim kanalı kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            val isChannelEnabled = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
            
            val result = areNotificationsEnabled && isChannelEnabled
            Log.d(TAG, if (result) "✅ Bildirimler etkin (Android 8+)" else "⚠️ Bildirimler devre dışı (Android 8+)")
            return result
        }
        
        // Android 8 öncesi için genel bildirim kontrolü
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        Log.d(TAG, if (enabled) "✅ Bildirimler etkin" else "⚠️ Bildirimler devre dışı")
        return enabled
    }

    /**
     * Kullanıcıyı uygulama bildirim ayarlarına yönlendirir
     */
    fun openNotificationSettings() {
        try {
            val intent = Intent()
            // Android 8+ için kanal ayarlarına yönlendir
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                // Android 8 öncesi için genel uygulama ayarlarına yönlendir
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d(TAG, "🔔 Bildirim ayarları açıldı")
        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim ayarları açılırken hata: ${e.message}", e)
        }
    }

    fun scheduleNotification(appointment: Appointment) {
        try {
            // Pozitif unique ID oluştur
            val notificationId = abs(appointment.id.hashCode() + System.currentTimeMillis().toInt())
            
            Log.d(TAG, "🔄 Bildirim planlama başladı: ${appointment.name}, ID: $notificationId")
            
            // Tamamlanmış randevular için bildirim oluşturma
            if (appointment.status == "Completed") {
                Log.d(TAG, "ℹ️ Tamamlanmış randevu için bildirim gönderilmedi: ${appointment.name}")
                return
            }
            
            // Bildirim kanalını oluştur
            createNotificationChannel()
            
            // Randevu zamanını parse et
            val appointmentDateTime = parseAppointmentDateTime(appointment)
                ?: return // Parse hatası durumunda çık
            
            // Bildirim için hatırlatma zamanı (5 dakika önce)
            val notificationTime = appointmentDateTime.minusMinutes(5)
            val currentTime = LocalDateTime.now()
            
            // Geçmiş zamanlı randevular için bildirim gönderme
            if (notificationTime.isBefore(currentTime)) {
                Log.d(TAG, "⚠️ Randevu bildirimi için zaman geçmiş: ${appointment.date} ${appointment.time}")
                return
            }
            
            // Gecikmeli bildirim için süre hesapla
            val delayInSeconds = ChronoUnit.SECONDS.between(currentTime, notificationTime)
            
            // Bildirim verisini hazırla
            val notificationData = workDataOf(
                "notificationId" to notificationId,
                "appointmentId" to appointment.id,
                "customerName" to appointment.name,
                "operation" to appointment.operation,
                "time" to appointment.time,
                "date" to appointment.date
            )
            
            // Bildirim worker'ı oluştur
            val notificationWork = OneTimeWorkRequestBuilder<AppointmentNotificationWorker>()
                .setInputData(notificationData)
                .setInitialDelay(delayInSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            // Worker'ı benzersiz bir id ile kaydet
            workManager.enqueueUniqueWork(
                "appointment_notification_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
            
            Log.d(TAG, """
                🎯 Bildirim iş kaydı oluşturuldu:
                ID: ${appointment.id}
                Müşteri: ${appointment.name} 
                Tarih: ${appointment.date}
                Saat: ${appointment.time}
                Gecikme: $delayInSeconds saniye
            """.trimIndent())
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim planlanırken hata: ${e.message}", e)
        }
    }
    
    private fun parseAppointmentDateTime(appointment: Appointment): LocalDateTime? {
        return try {
            // Tarih formatı: "2023-07-30"
            // Saat formatı: "14:30:00"
            LocalDateTime.parse(
                "${appointment.date}T${appointment.time}",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
        } catch (e: Exception) {
            try {
                // Alternatif format: "2023-07-30" ve "14:30"
                LocalDateTime.parse(
                    "${appointment.date}T${appointment.time}:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                )
            } catch (e2: Exception) {
                Log.e(TAG, "💥 Randevu tarihi/saati parse edilemedi: ${appointment.date}T${appointment.time}", e2)
                null
            }
        }
    }

    fun cancelNotification(appointmentId: Int) {
        workManager.cancelUniqueWork("appointment_notification_$appointmentId")
        Log.d(TAG, "🚫 Bildirim iptal edildi: $appointmentId")
    }
} 