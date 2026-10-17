package com.habitos.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habitos.app.ui.viewmodel.dataStore
import com.habitos.app.ui.viewmodel.KEY_EOD_HOUR
import com.habitos.app.ui.viewmodel.KEY_EOD_MIN
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

object AlarmScheduler {

    fun scheduleEodReminder(context: Context) {
        val (hour, min) = runBlocking {
            val prefs = context.dataStore.data.first()
            Pair(prefs[KEY_EOD_HOUR] ?: 21, prefs[KEY_EOD_MIN] ?: 0)
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getBroadcast(
            context, 1001,
            Intent(context, AlarmReceiver::class.java).apply { action = "com.habitos.app.EOD_REMINDER" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
            }
        } catch (e: Exception) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
        }
    }

    fun scheduleBirthdayChecks(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = PendingIntent.getBroadcast(
            context, 1002,
            Intent(context, AlarmReceiver::class.java).apply { action = "com.habitos.app.BIRTHDAY_REMINDER" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Fire at midnight every day
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 30)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        // Also schedule 1 day before at 20:00
        val calEve = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val intentEve = PendingIntent.getBroadcast(
            context, 1003,
            Intent(context, AlarmReceiver::class.java).apply { action = "com.habitos.app.BIRTHDAY_REMINDER" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calEve.timeInMillis, intentEve)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calEve.timeInMillis, intentEve)
            }
        } catch (e: Exception) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent)
        }
    }
}
