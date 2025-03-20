package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AppointmentNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "appointment_notifications"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "AppointmentNotification"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Bildirim worker'ı başlatıldı")

                // Bildirim verilerini al
                val appointmentId = inputData.getInt("appointmentId", -1)
                val customerName = inputData.getString("customerName") ?: "Müşteri"
                val operation = inputData.getString("operation") ?: "İşlem"
                val time = inputData.getString("time") ?: "??:??"
                val date = inputData.getString("date") ?: LocalDate.now().toString()

                Log.d(TAG, """
                    📋 Randevu bilgileri alındı:
                    ID: $appointmentId
                    Müşteri: $customerName
                    İşlem: $operation
                    Tarih: $date
                    Saat: $time
                """.trimIndent())

                // Randevu hala geçerli mi kontrol et
                val appointmentDao = AppDatabase.getDatabase(applicationContext).appointmentDao()
                val appointment = appointmentDao.getAppointmentById(appointmentId)

                if (appointment == null) {
                    Log.e(TAG, "❌ Randevu bulunamadı - ID: $appointmentId")
                    return@withContext Result.failure()
                }

                if (appointment.status == "Completed") {
                    Log.d(TAG, "ℹ️ Randevu zaten tamamlanmış - ID: $appointmentId")
                    return@withContext Result.success()
                }

                // Bildirim göster
                showNotification(appointmentId, customerName, operation, time)
                Log.d(TAG, "✅ Bildirim başarıyla gösterildi - ID: $appointmentId")

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "💥 Bildirim gösterilirken hata oluştu", e)
                Result.failure()
            }
        }
    }

    private fun showNotification(appointmentId: Int, customerName: String, operation: String, time: String) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Bildirim kanalı oluştur (Android 8.0 ve üzeri için)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Randevu Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Yaklaşan randevular için bildirimler"
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                    setShowBadge(true)
                    setBypassDnd(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "📢 Bildirim kanalı güncellendi")
            }

            // Bildirim oluştur
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ Yaklaşan Randevu")
                .setContentText("$customerName - $time")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("$customerName için $operation randevunuz yaklaşıyor.\nSaat: $time"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            // Bildirimi göster
            notificationManager.notify(appointmentId, notification)
            Log.d(TAG, """
                📱 Bildirim gösterildi:
                ID: $appointmentId
                Müşteri: $customerName
                Saat: $time
                İşlem: $operation
            """.trimIndent())

        } catch (e: Exception) {
            Log.e(TAG, "💥 Bildirim oluşturulurken hata oluştu", e)
            throw e
        }
    }
} 