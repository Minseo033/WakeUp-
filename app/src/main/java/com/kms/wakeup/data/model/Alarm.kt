package com.kms.wakeup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarm_table") // 테이블 이름 지정
data class Alarm(
    @PrimaryKey(autoGenerate = true) // ID는 1부터 자동으로 증가
    val id: Int = 0,

    val hour: Int,
    val minute: Int,
    val label: String,
    val days: List<String>, // 리스트는 바로 저장 못해서 변환기(Converter)가 필요합니다
    val mission: String?,
    var isOn: Boolean,

    val ringtoneUri: String? = null,
    val useCustomSentence: Boolean = false
) : Serializable