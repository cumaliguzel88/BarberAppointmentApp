package com.cumaliguzel.barberappointment.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.MainActivity
import com.cumaliguzel.barberappointment.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AppointmentNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "appointment_notifications"
        private const val TAG = "AppNotificationWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔔 Bildirim işçisi başladı")
        
        try {
            val notificationId = inputData.getInt("notificationId", 0)
            if (notificationId <= 0) {
                Log.e(TAG, "❌ Geçersiz bildirim ID: $notificationId")
                return@withContext Result.failure()
            }
            
            val appointmentId = inputData.getLong("appointmentId", 0L)
            val customerName = inputData.getString("customerName") ?: "Müşteri"
            val operation = inputData.getString("operation") ?: "Randevu"
            val time = inputData.getString("time") ?: ""
            val date = inputData.getString("date") ?: ""
            
            Log.d(TAG, "📋 Bildirim bilgileri alındı: ID=$notificationId, Müşteri=$customerName, Tarih=$date, Saat=$time")
            
            // Android 8.0+ için bildirim kanalını kontrol et
            createOrUpdateNotificationChannel()
            
            val title = "Randevu Hatırlatıcı"
            val message = "$customerName için $time randevunuz var"
            
            showNotification(notificationId, title, message, appointmentId)
            Log.d(TAG, "✅ Bildirim işlemi tamamlandı")
            
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim gösterilirken hata oluştu: ${e.message}", e)
            return@withContext Result.failure()
        }
    }

    private fun createOrUpdateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Mevcut kanalı kontrol et
                val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (existingChannel != null) {
                    Log.d(TAG, "ℹ️ Bildirim kanalı zaten mevcut")
                    return
                }
                
                // Bildirim kanalını yüksek önem düzeyi ile oluştur
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Randevu Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Yaklaşan randevular için bildirimler"
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                    setSound(
                        Settings.System.DEFAULT_NOTIFICATION_URI,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    importance = NotificationManager.IMPORTANCE_HIGH
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setBypassDnd(true) // Rahatsız Etmeyin modunu atla
                    setShowBadge(true) // Uygulama simgesinde bildirim rozeti göster
                }
                
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Bildirim kanalı oluşturuldu")
            } catch (e: Exception) {
                Log.e(TAG, "💥 Bildirim kanalı oluşturma hatası: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "ℹ️ Android 8.0 öncesi sürüm: Bildirim kanalı gerekmez")
        }
    }

    private fun showNotification(notificationId: Int, title: String, message: String, appointmentId: Long) {
        try {
            // Intent oluştur
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("appointmentId", appointmentId)
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                notificationId,
                intent,
                flags
            )

            // Yüksek öncelikli bildirim oluştur
            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setLights(Color.RED, 1000, 500)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setFullScreenIntent(pendingIntent, true)
            }

            // Bildirimi göster
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) 
                        == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notificationId, builder.build())
                    Log.d(TAG, "✅ Bildirim gösterildi - ID: $notificationId")
                } else {
                    Log.e(TAG, "❌ Bildirim izni eksik! Android 13+ izin gerekli.")
                }
            } else {
                // Android 13 öncesi sürümlerde bildirim göster
                notificationManager.notify(notificationId, builder.build())
                Log.d(TAG, "✅ Bildirim gösterildi - ID: $notificationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim gösterilirken hata: ${e.message}", e)
        }
    }
} 