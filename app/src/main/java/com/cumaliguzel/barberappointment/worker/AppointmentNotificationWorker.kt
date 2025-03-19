package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.MainActivity
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.data.AppDatabase
import com.cumaliguzel.barberappointment.viewmodel.AppointmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AppointmentNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "appointment_notifications"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DEBUG_TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Öncelikle foreground service olarak başlat (Android 9+ için önemli)
            setForeground(createForegroundInfo())
            
            // Bildirim içeriği
            val appointmentId = inputData.getInt("appointmentId", -1)
            val customerName = inputData.getString("customerName") ?: "Müşteri"
            val operation = inputData.getString("operation") ?: "İşlem"
            val time = inputData.getString("time") ?: "??:??"
            val date = inputData.getString("date") ?: LocalDate.now().toString()
            
            Log.d(DEBUG_TAG, "Bildirim hazırlanıyor: $customerName - $time, ID: $appointmentId")
            
            // Son bir kez daha kontrol et - randevu hala tamamlanmamış mı?
            val appointmentDao = AppDatabase.getDatabase(applicationContext).appointmentDao()
            if (appointmentDao == null) {
                Log.e(DEBUG_TAG, "❌ AppointmentDao oluşturulamadı")
                return@withContext Result.failure()
            }
            
            val appointment = appointmentDao.getAppointmentById(appointmentId)
            
            if (appointment == null) {
                Log.d(DEBUG_TAG, "Randevu bulunamadı: $appointmentId")
                return@withContext Result.failure()
            }
            
            if (appointment.status == "Completed") {
                Log.d(DEBUG_TAG, "Randevu zaten tamamlanmış: $appointmentId")
                return@withContext Result.success()
            }
            
            // Bildirim göster
            showNotification(appointmentId, customerName, operation, time, date)
            
            Result.success()
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "Bildirim işlemi sırasında hata oluştu", e)
            Result.failure()
        }
    }
    
    private fun createForegroundInfo(): ForegroundInfo {
        // Bildirim kanalını oluştur
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Randevu Hatırlatıcı")
            .setContentText("Yaklaşan randevular için hazırlanıyor...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
            
        return ForegroundInfo(FOREGROUND_NOTIFICATION_ID, notification)
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
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true) // Uygulama simgesinde bildirim rozeti göster
            }
            
            // Kanalı sisteme kaydet
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(DEBUG_TAG, "Bildirim kanalı oluşturuldu: $CHANNEL_ID")
        }
    }

    private fun showNotification(appointmentId: Int, customerName: String, operation: String, time: String, date: String) {
        // Bildirim kanalını oluştur
        createNotificationChannel()
        
        // Intent oluştur
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("appointmentId", appointmentId)
        }
        
        // FLAG_IMMUTABLE Android 12+ için gerekli, eski sürümlerde FLAG_UPDATE_CURRENT yeterli
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            appointmentId, 
            intent,
            pendingIntentFlag
        )
        
        // Varsayılan bildirim sesi
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Bildirim içeriği
        val title = "Randevu Hatırlatması"
        val content = "$customerName için $time randevusu 5 dakika içinde başlayacak - İşlem: $operation"
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Maksimum öncelik
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Alarm kategorisi en yüksek önceliği sağlar
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Kilit ekranında tam görünür
            .build()
        
        try {
            // NotificationManagerCompat.from(applicationContext).notify için gereken izin kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ için POST_NOTIFICATIONS izni kontrol et
                val context = applicationContext
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                        PackageManager.PERMISSION_GRANTED) {
                    Log.e(DEBUG_TAG, "POST_NOTIFICATIONS izni eksik")
                    return
                }
            }
            
            NotificationManagerCompat.from(applicationContext).notify(appointmentId, notification)
            Log.d(DEBUG_TAG, "📱 Bildirim gösterildi: $customerName [ID:$appointmentId] [Tarih:$date] [Saat:$time]")
        } catch (e: SecurityException) {
            Log.e(DEBUG_TAG, "Bildirim gösterilirken izin hatası oluştu", e)
        }
    }
} 