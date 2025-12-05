package com.kms.wakeup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_history_table")
data class AlarmHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long,      // 정렬용 시간 (예: 1745234567890)
    val date: String,         // 날짜 (예: "2025.10.04")
    val time: String,         // 시간 (예: "09:05")
    val missionType: String   // 미션 종류 (예: "타자 입력")
)