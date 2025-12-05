package com.kms.wakeup.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(data: String?): List<String> {
        return if (data.isNullOrEmpty()) emptyList()
        else data.split(",").map { it.trim() }
    }
}