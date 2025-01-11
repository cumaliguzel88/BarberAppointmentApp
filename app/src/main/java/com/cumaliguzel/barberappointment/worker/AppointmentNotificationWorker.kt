package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cumaliguzel.barberappointment.R

class AppointmentNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val customerName = inputData.getString(KEY_CUSTOMER_NAME) ?: return Result.failure()
        val operation = inputData.getString(KEY_OPERATION) ?: return Result.failure()
        val time = inputData.getString(KEY_TIME) ?: return Result.failure()

        createNotificationChannel()
        showNotification(customerName, operation, time)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming appointments"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(customerName: String, operation: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Upcoming Appointment")
            .setContentText("$customerName - $operation at $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_CUSTOMER_NAME = "customer_name"
        const val KEY_OPERATION = "operation"
        const val KEY_TIME = "time"
        private const val CHANNEL_ID = "appointment_reminders"
        private const val NOTIFICATION_ID = 1
    }
} 