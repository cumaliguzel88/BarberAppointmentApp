package com.cumaliguzel.barberappointment.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        val appointmentId = inputData.getInt("appointmentId", -1)
        val customerName = inputData.getString("customerName") ?: return Result.failure()
        val operation = inputData.getString("operation") ?: return Result.failure()
        val time = inputData.getString("time") ?: return Result.failure()

        showNotification(appointmentId, customerName, operation, time)
        return Result.success()
    }

    private fun showNotification(appointmentId: Int, customerName: String, operation: String, time: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, AppointmentViewModel.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yaklaşan Randevu")
            .setContentText("$customerName - $operation saat $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(appointmentId, notification)
    }
} 