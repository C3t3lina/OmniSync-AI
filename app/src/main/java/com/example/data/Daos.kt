package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface GeneratedDocDao {
    @Query("SELECT * FROM generated_docs ORDER BY timestamp DESC")
    fun getAllDocs(): Flow<List<GeneratedDoc>>

    @Query("SELECT * FROM generated_docs WHERE id = :id LIMIT 1")
    suspend fun getDocById(id: Int): GeneratedDoc?

    @Query("SELECT * FROM generated_docs WHERE type = :type ORDER BY timestamp DESC")
    fun getDocsByType(type: String): Flow<List<GeneratedDoc>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: GeneratedDoc): Long

    @Delete
    suspend fun deleteDoc(doc: GeneratedDoc)

    @Query("DELETE FROM generated_docs WHERE id = :id")
    suspend fun deleteDocById(id: Int)
}

@Dao
interface ConnectedDeviceDao {
    @Query("SELECT * FROM connected_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<ConnectedDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: ConnectedDevice)

    @Query("UPDATE connected_devices SET status = :status, lastSyncTime = :lastSyncTime WHERE id = :id")
    suspend fun updateDeviceStatus(id: Int, status: String, lastSyncTime: Long)

    @Query("DELETE FROM connected_devices WHERE id = :id")
    suspend fun deleteDeviceById(id: Int)
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallLogEntity): Long

    @Query("DELETE FROM call_logs")
    suspend fun clearCallLogs()
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM spaced_study_plans ORDER BY topicIndex ASC")
    fun getAllStudyPlans(): Flow<List<StudyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlanEntity)

    @Query("DELETE FROM spaced_study_plans")
    suspend fun clearStudyPlans()
}

@Dao
interface UiTapStepDao {
    @Query("SELECT * FROM ui_tapping_steps ORDER BY stepIndex ASC")
    fun getAllUiTapSteps(): Flow<List<UiTapStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUiTapStep(step: UiTapStepEntity)

    @Query("DELETE FROM ui_tapping_steps")
    suspend fun clearUiTapSteps()
}

@Dao
interface LocalServerAuditDao {
    @Query("SELECT * FROM developer_server_audits ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAudit(): Flow<LocalServerAudit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: LocalServerAudit): Long

    @Query("DELETE FROM developer_server_audits")
    suspend fun clearAudits()
}
