package com.kms.wakeup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kms.wakeup.data.model.Alarm
import com.kms.wakeup.data.model.AlarmHistory
import com.kms.wakeup.data.model.CustomSentence

// ★ version을 4로 변경, entities에 CustomSentence 추가
@Database(entities = [Alarm::class, AlarmHistory::class, CustomSentence::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wakeup_database"
                )
                    .fallbackToDestructiveMigration() // 버전 변경 시 기존 데이터 삭제 후 재생성
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}