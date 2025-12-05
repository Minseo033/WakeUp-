package com.kms.wakeup.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kms.wakeup.data.model.Alarm
import java.util.Calendar

object AlarmScheduler {

    fun register(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = Calendar.getInstance()

        if (alarm.days.isEmpty()) {
            val triggerTimeMillis = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }.timeInMillis

            scheduleAlarm(context, alarmManager, triggerTimeMillis, alarm, null)
            return
        }

        alarm.days.forEach { dayLabel ->
            val dayOfWeek = when (dayLabel) {
                "일" -> Calendar.SUNDAY
                "월" -> Calendar.MONDAY
                "화" -> Calendar.TUESDAY
                "수" -> Calendar.WEDNESDAY
                "목" -> Calendar.THURSDAY
                "금" -> Calendar.FRIDAY
                "토" -> Calendar.SATURDAY
                else -> null
            } ?: return@forEach

            val triggerTime = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            scheduleAlarm(context, alarmManager, triggerTime.timeInMillis, alarm, dayOfWeek)
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerMillis: Long,
        alarm: Alarm,
        dayOfWeek: Int?
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("hour", alarm.hour)
            putExtra("minute", alarm.minute)
            putExtra("label", alarm.label)
            putExtra("mission", alarm.mission)
            putExtra("ringtoneUri", alarm.ringtoneUri)
            putExtra("useCustomSentence", alarm.useCustomSentence) // ★ [추가]
            if (dayOfWeek != null) putExtra("dayOfWeek", dayOfWeek)
        }

        val requestCode = buildRequestCode(alarm.id, dayOfWeek)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        if (alarm.days.isEmpty()) {
            cancelPendingIntent(context, alarmManager, alarm.id, null)
        } else {
            alarm.days.forEach { dayLabel ->
                val dayOfWeek = when (dayLabel) {
                    "일" -> Calendar.SUNDAY
                    "월" -> Calendar.MONDAY
                    "화" -> Calendar.TUESDAY
                    "수" -> Calendar.WEDNESDAY
                    "목" -> Calendar.THURSDAY
                    "금" -> Calendar.FRIDAY
                    "토" -> Calendar.SATURDAY
                    else -> null
                }
                cancelPendingIntent(context, alarmManager, alarm.id, dayOfWeek)
            }
        }
    }

    private fun cancelPendingIntent(
        context: Context,
        alarmManager: AlarmManager,
        alarmId: Int,
        dayOfWeek: Int?
    ) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = buildRequestCode(alarmId, dayOfWeek)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildRequestCode(alarmId: Int, dayOfWeek: Int?): Int {
        return if (dayOfWeek == null) {
            alarmId * 10
        } else {
            alarmId * 10 + dayOfWeek
        }
    }
}