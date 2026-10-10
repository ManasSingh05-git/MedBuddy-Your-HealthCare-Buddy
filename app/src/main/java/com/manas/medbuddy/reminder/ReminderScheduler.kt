package com.manas.medbuddy.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.manas.medbuddy.model.Medicine
import com.manas.medbuddy.model.ReminderTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMedicineReminders(medicine: Medicine) {
        cancelMedicineReminders(medicine)

        medicine.reminderTimes.forEachIndexed { index, reminder ->
            scheduleSingleReminder(medicine, reminder, index)
        }
    }

    private fun scheduleSingleReminder(medicine: Medicine, reminder: ReminderTime, index: Int) {
        val calendar = parseTimeToCalendar(reminder.time) ?: return
        val now = Calendar.getInstance()

        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val notificationId = (medicine.id.hashCode() + index * 1000).let { if (it < 0) -it else it }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(ReminderReceiver.EXTRA_DOSAGE, medicine.dosage)
            putExtra(ReminderReceiver.EXTRA_TIME_LABEL, reminder.label)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelMedicineReminders(medicine: Medicine) {
        medicine.reminderTimes.forEachIndexed { index, _ ->
            val notificationId = (medicine.id.hashCode() + index * 1000).let { if (it < 0) -it else it }
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun parseTimeToCalendar(timeStr: String): Calendar? {
        val formats = listOf(
            SimpleDateFormat("hh:mm a", Locale.getDefault()),
            SimpleDateFormat("h:mm a", Locale.getDefault()),
            SimpleDateFormat("HH:mm", Locale.getDefault()),
            SimpleDateFormat("H:mm", Locale.getDefault())
        )

        for (format in formats) {
            try {
                val date = format.parse(timeStr.trim())
                if (date != null) {
                    val timeCal = Calendar.getInstance().apply { time = date }
                    val result = Calendar.getInstance()
                    result.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    result.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    result.set(Calendar.SECOND, 0)
                    result.set(Calendar.MILLISECOND, 0)
                    return result
                }
            } catch (_: Exception) {
            }
        }
        return null
    }
}
