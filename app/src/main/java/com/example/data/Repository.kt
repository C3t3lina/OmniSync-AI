package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val database: AppDatabase) {
    private val chatMessageDao = database.chatMessageDao()
    private val generatedDocDao = database.generatedDocDao()
    private val connectedDeviceDao = database.connectedDeviceDao()
    private val callLogDao = database.callLogDao()
    private val studyPlanDao = database.studyPlanDao()
    private val uiTapStepDao = database.uiTapStepDao()
    private val localServerAuditDao = database.localServerAuditDao()

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    val allDocs: Flow<List<GeneratedDoc>> = generatedDocDao.getAllDocs()
    val allDevices: Flow<List<ConnectedDevice>> = connectedDeviceDao.getAllDevices()
    
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()
    val allStudyPlans: Flow<List<StudyPlanEntity>> = studyPlanDao.getAllStudyPlans()
    val allUiTapSteps: Flow<List<UiTapStepEntity>> = uiTapStepDao.getAllUiTapSteps()
    val latestAudit: Flow<LocalServerAudit?> = localServerAuditDao.getLatestAudit()

    // Call logs helpers
    suspend fun insertCallLog(log: CallLogEntity): Long {
        return callLogDao.insertCallLog(log)
    }

    suspend fun clearCallLogs() {
        callLogDao.clearCallLogs()
    }

    // Study plans helpers
    suspend fun insertStudyPlan(plan: StudyPlanEntity) {
        studyPlanDao.insertStudyPlan(plan)
    }

    suspend fun clearStudyPlans() {
        studyPlanDao.clearStudyPlans()
    }

    // UI automatic tap helper
    suspend fun insertUiTapStep(step: UiTapStepEntity) {
        uiTapStepDao.insertUiTapStep(step)
    }

    suspend fun clearUiTapSteps() {
        uiTapStepDao.clearUiTapSteps()
    }

    // Local server audit helper
    suspend fun insertAudit(audit: LocalServerAudit): Long {
        return localServerAuditDao.insertAudit(audit)
    }

    suspend fun clearAudits() {
        localServerAuditDao.clearAudits()
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearChat() {
        chatMessageDao.clearChat()
    }

    suspend fun insertDoc(doc: GeneratedDoc): Long {
        return generatedDocDao.insertDoc(doc)
    }

    suspend fun getDocById(id: Int): GeneratedDoc? {
        return generatedDocDao.getDocById(id)
    }

    fun getDocsByType(type: String): Flow<List<GeneratedDoc>> {
        return generatedDocDao.getDocsByType(type)
    }

    suspend fun deleteDoc(doc: GeneratedDoc) {
        generatedDocDao.deleteDoc(doc)
    }

    suspend fun deleteDocById(id: Int) {
        generatedDocDao.deleteDocById(id)
    }

    suspend fun insertDevice(device: ConnectedDevice) {
        connectedDeviceDao.insertDevice(device)
    }

    suspend fun updateDeviceStatus(id: Int, status: String, lastSyncTime: Long) {
        connectedDeviceDao.updateDeviceStatus(id, status, lastSyncTime)
    }

    suspend fun deleteDeviceById(id: Int) {
        connectedDeviceDao.deleteDeviceById(id)
    }

    suspend fun prepDevicesIfEmpty() {
        val existing = allDevices.first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                ConnectedDevice(name = "Google Workspace (Gmail, Sheets)", platform = "Google", status = "Sincronizado"),
                ConnectedDevice(name = "MacBook Pro Local Drive", platform = "macOS", status = "Pendiente"),
                ConnectedDevice(name = "iPhone 15 Pro Link", platform = "iPhone", status = "Sincronizado"),
                ConnectedDevice(name = "Android Companion Tablet", platform = "Android", status = "Sincronizado"),
                ConnectedDevice(name = "Canva API Cloud Space", platform = "Canva", status = "Pendiente")
            )
            for (dev in defaults) {
                connectedDeviceDao.insertDevice(dev)
            }
        }
    }
}
