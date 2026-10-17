package com.habitos.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                AlarmScheduler.scheduleEodReminder(context)
                AlarmScheduler.scheduleBirthdayChecks(context)
            }
            "com.habitos.app.EOD_REMINDER" -> {
                scope.launch {
                    try {
                        NotificationService.sendEodNotification(context)
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "EOD error", e)
                    } finally {
                        AlarmScheduler.scheduleEodReminder(context)
                    }
                }
            }
            "com.habitos.app.BIRTHDAY_REMINDER" -> {
                scope.launch {
                    try {
                        NotificationService.checkAndSendBirthdayNotifications(context)
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "Birthday error", e)
                    } finally {
                        AlarmScheduler.scheduleBirthdayChecks(context)
                    }
                }
            }
        }
    }
}
