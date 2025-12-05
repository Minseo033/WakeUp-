package com.kms.wakeup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_sentence_table")
data class CustomSentence(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String
)