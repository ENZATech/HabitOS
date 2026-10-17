package com.habitos.app.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.habitos.app.HabitOSApp
import com.habitos.app.R
import com.habitos.app.data.models.Birthday
import com.habitos.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

object NotificationService {

    suspend fun sendEodNotification(context: Context) {
        val app = context.applicationContext as HabitOSApp
        val repo = app.repository
        val today = LocalDate.now()

        val pendingTasks = repo.myDayTasks().first()
        val totalHabits = repo.totalActiveHabits()
        val checkedToday = repo.checkinCountForDate(today)
        val pendingHabits = totalHabits > 0 && checkedToday < totalHabits
        val todayBirthdays = repo.allBirthdays().first().filter {
            it.birthDate.month == today.month && it.birthDate.dayOfMonth == today.dayOfMonth
        }

        if (pendingTasks.isEmpty() && !pendingHabits && todayBirthdays.isEmpty()) return

        val lines = buildList {
            if (pendingTasks.isNotEmpty()) add("📋 ${pendingTasks.size} My Day task(s) pending")
            if (pendingHabits) add("🎯 ${totalHabits - checkedToday} habit(s) not checked in")
            todayBirthdays.forEach { add("🎂 ${it.name}'s birthday today!") }
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val style = NotificationCompat.InboxStyle()
        lines.forEach { style.addLine(it) }
        style.setBigContentTitle("habitOS – EOD Check-in ⏰")

        val notification = NotificationCompat.Builder(context, "eod_reminder")
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle("habitOS – EOD Check-in ⏰")
            .setContentText("You have ${lines.size} item(s) to complete today")
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(2001, notification)
    }

    suspend fun checkAndSendBirthdayNotifications(context: Context) {
        val app = context.applicationContext as HabitOSApp
        val birthdays = app.repository.allBirthdays().first()
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        birthdays.forEachIndexed { idx, b ->
            val bDay = b.birthDate
            val isToday    = bDay.month == today.month    && bDay.dayOfMonth == today.dayOfMonth
            val isTomorrow = bDay.month == tomorrow.month && bDay.dayOfMonth == tomorrow.dayOfMonth

            when {
                isToday -> {
                    val n = NotificationCompat.Builder(context, "birthday_alerts")
                        .setSmallIcon(R.drawable.ic_splash_logo)
                        .setContentTitle("🎂 It's ${b.name}'s Birthday Today!")
                        .setContentText("Don't forget to wish them! Tap to open habitOS.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    nm.notify(3000 + idx, n)
                }
                isTomorrow -> {
                    val n = NotificationCompat.Builder(context, "birthday_alerts")
                        .setSmallIcon(R.drawable.ic_splash_logo)
                        .setContentTitle("🎉 ${b.name}'s Birthday is Tomorrow!")
                        .setContentText("Prepare a wish for ${b.name} in advance!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build()
                    nm.notify(4000 + idx, n)
                }
            }
        }
    }
}
