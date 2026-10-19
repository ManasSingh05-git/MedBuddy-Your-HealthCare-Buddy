package com.manas.medbuddy.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.manas.medbuddy.data.model.SleepReminderConfig
import com.manas.medbuddy.receiver.SleepReminderReceiver

object SleepReminderScheduler {

    const val CHANNEL_ID = "sleep_reminders_channel"
    private const val REQUEST_CODE = 888

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sleep & Bedtime Reminders"
            val descriptionText = "Notifications reminding you to wind down for bed"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSleepReminder(context: Context, config: SleepReminderConfig) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getReminderPendingIntent(context)

        if (!config.isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val nextTimeMillis = SleepReminderCalculator.calculateNextReminderTimeMillis(config)
        if (nextTimeMillis == null) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTimeMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Fallback for system alarm policy restrictions
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTimeMillis,
                    pendingIntent
                )
            } catch (_: Exception) {
                // Ignore if platform blocks alarm scheduling
            }
        }
    }

    fun cancelSleepReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getReminderPendingIntent(context))
    }

    private fun getReminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, SleepReminderReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }
}
