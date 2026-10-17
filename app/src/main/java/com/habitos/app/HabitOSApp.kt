package com.habitos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.habitos.app.data.db.HabitDatabase
import com.habitos.app.data.repository.HabitRepository

class HabitOSApp : Application() {
    val database by lazy { HabitDatabase.getInstance(this) }
    val repository by lazy { HabitRepository(database.habitDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel("eod_reminder", "EOD Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Daily end-of-day habit completion reminders"
                    enableVibration(true)
                }
            )
            nm.createNotificationChannel(
                NotificationChannel("birthday_alerts", "Birthday Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Birthday notifications for friends"
                    enableVibration(true)
                }
            )
        }
    }
}
