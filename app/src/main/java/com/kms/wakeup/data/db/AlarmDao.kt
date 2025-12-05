package com.kms.wakeup.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kms.wakeup.data.model.Alarm
import com.kms.wakeup.data.model.AlarmHistory
import com.kms.wakeup.data.model.CustomSentence

@Dao
interface AlarmDao {
    // === [알람 관련] ===
    @Query("SELECT * FROM alarm_table ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): List<Alarm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: Alarm)

    @Delete
    fun deleteAlarm(alarm: Alarm)

    @Update
    fun updateAlarm(alarm: Alarm)

    @Query("DELETE FROM alarm_table")
    fun clearAllAlarms()

    // === [기상 기록 관련] ===
    @Insert
    fun insertHistory(history: AlarmHistory)

    @Query("SELECT * FROM alarm_history_table ORDER BY timestamp DESC")
    fun getAllHistory(): List<AlarmHistory>

    @Query("DELETE FROM alarm_history_table")
    fun clearAllHistory()

    // === ★ [추가됨] 커스텀 문장 관련 ===
    @Insert
    fun insertSentence(sentence: CustomSentence)

    @Query("SELECT * FROM custom_sentence_table")
    fun getAllSentences(): List<CustomSentence>

    @Query("DELETE FROM custom_sentence_table")
    fun clearAllSentences()
}