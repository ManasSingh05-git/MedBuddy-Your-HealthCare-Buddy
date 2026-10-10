package com.manas.medbuddy.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.manas.medbuddy.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medicine"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val timeLabel = intent.getStringExtra(EXTRA_TIME_LABEL) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, System.currentTimeMillis().toInt())

        showNotification(context, medicineName, dosage, timeLabel, notificationId)
    }

    private fun showNotification(
        context: Context,
        medicineName: String,
        dosage: String,
        timeLabel: String,
        notificationId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "med_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take scheduled medicines"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("It's time to take your medicine")
            if (dosage.isNotBlank()) append(" ($dosage)")
            if (timeLabel.isNotBlank()) append(" - $timeLabel schedule")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("MedBuddy Reminder: $medicineName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_TIME_LABEL = "extra_time_label"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
