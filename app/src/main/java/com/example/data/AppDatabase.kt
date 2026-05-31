package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessage::class, GeneratedDoc::class, ConnectedDevice::class, CallLogEntity::class, StudyPlanEntity::class, UiTapStepEntity::class, LocalServerAudit::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun generatedDocDao(): GeneratedDocDao
    abstract fun connectedDeviceDao(): ConnectedDeviceDao
    abstract fun callLogDao(): CallLogDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun uiTapStepDao(): UiTapStepDao
    abstract fun localServerAuditDao(): LocalServerAuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_personal_agent_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
