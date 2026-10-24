package com.manas.medbuddy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.manas.medbuddy.data.repository.SleepReminderRepository
import com.manas.medbuddy.util.SleepReminderScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val repository = SleepReminderRepository.getInstance(context)
            val config = repository.getConfig()
            if (config.isEnabled) {
                SleepReminderScheduler.scheduleSleepReminder(context, config)
            }
        }
    }
}
