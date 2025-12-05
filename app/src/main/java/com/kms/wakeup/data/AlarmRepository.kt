package com.kms.wakeup.data

import android.content.Context
import com.kms.wakeup.data.db.AppDatabase
import com.kms.wakeup.data.model.Alarm
import com.kms.wakeup.data.model.AlarmHistory
import com.kms.wakeup.data.model.CustomSentence

object AlarmRepository {

    private fun getDao(context: Context) = AppDatabase.getDatabase(context).alarmDao()

    // === [알람 설정] ===
    fun getAlarms(context: Context): List<Alarm> = getDao(context).getAllAlarms()
    fun addAlarm(context: Context, alarm: Alarm) = getDao(context).insertAlarm(alarm)
    fun removeAlarm(context: Context, alarm: Alarm) = getDao(context).deleteAlarm(alarm)
    fun updateAlarm(context: Context, alarm: Alarm) = getDao(context).updateAlarm(alarm)
    fun clearAllAlarms(context: Context) = getDao(context).clearAllAlarms()

    // === [기상 기록] ===
    fun addHistory(context: Context, history: AlarmHistory) {
        getDao(context).insertHistory(history)
    }

    fun getHistoryList(context: Context): List<AlarmHistory> {
        return getDao(context).getAllHistory()
    }

    fun clearAllHistory(context: Context) = getDao(context).clearAllHistory()

    // === ★ [추가됨] 커스텀 문장 ===
    fun addCustomSentence(context: Context, text: String) {
        getDao(context).insertSentence(CustomSentence(text = text))
    }

    fun getCustomSentences(context: Context): List<CustomSentence> {
        return getDao(context).getAllSentences()
    }

    fun clearCustomSentences(context: Context) {
        getDao(context).clearAllSentences()
    }
}