package com.manas.medbuddy.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.manas.medbuddy.MainActivity
import com.manas.medbuddy.data.repository.SleepReminderRepository
import com.manas.medbuddy.util.SleepReminderScheduler

class SleepReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val repository = SleepReminderRepository.getInstance(context)
        val config = repository.getConfig()

        if (config.isEnabled) {
            showNotification(context, config.getFormattedBedtime())
            // Reschedule next upcoming sleep reminder
            SleepReminderScheduler.scheduleSleepReminder(context, config)
        }
    }

    private fun showNotification(context: Context, formattedBedtime: String) {
        SleepReminderScheduler.createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 999, mainIntent, flags)

        val builder = NotificationCompat.Builder(context, SleepReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🌙 Bedtime Reminder")
            .setContentText("Your bedtime is set for $formattedBedtime. Time to wind down for bed!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(777, builder.build())
    }
}
