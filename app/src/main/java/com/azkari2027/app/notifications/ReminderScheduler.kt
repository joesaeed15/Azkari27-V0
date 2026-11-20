package com.azkari2027.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azkari2027.app.AzkariApp
import com.azkari2027.app.MainActivity
import com.azkari2027.app.R
import com.azkari2027.app.data.ReminderConfig
import com.azkari2027.app.data.ReminderKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "azkari_2027_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكيرات الأذكار اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات أذكار الصباح والمساء والنوم والاستيقاظ (أوفلاين)"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, config: ReminderConfig, isArabic: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(context, config.key, isArabic)
        if (!config.enabled) {
            alarmManager.cancel(pending)
            return
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
        }
    }

    private fun buildPendingIntent(context: Context, key: ReminderKey, isArabic: Boolean): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_KEY", key.name)
            putExtra("IS_ARABIC", isArabic)
        }
        return PendingIntent.getBroadcast(
            context,
            key.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val keyName = intent.getStringExtra("REMINDER_KEY") ?: return
        val key = runCatching { ReminderKey.valueOf(keyName) }.getOrNull() ?: return
        val isArabic = intent.getBooleanExtra("IS_ARABIC", true)

        ReminderScheduler.ensureChannel(context)
        val catId = if (isArabic) key.arabicCategoryId else key.englishCategoryId
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_CATEGORY_ID", catId)
        }
        val contentPending = PendingIntent.getActivity(
            context, key.requestCode + 50, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (isArabic) key.titleAr else key.titleEn)
            .setContentText(if (isArabic) key.descAr else key.descEn)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPending)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(key.requestCode, notification)

        // Schedule next day automatically
        val app = context.applicationContext as? AzkariApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val settings = app.settingsStore.settingsFlow.first()
            settings.reminders.firstOrNull { it.key == key }?.let { cfg ->
                if (cfg.enabled) ReminderScheduler.schedule(context, cfg, settings.isArabic)
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? AzkariApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val settings = app.settingsStore.settingsFlow.first()
            settings.reminders.filter { it.enabled }.forEach { cfg ->
                ReminderScheduler.schedule(context, cfg, settings.isArabic)
            }
        }
    }
}
