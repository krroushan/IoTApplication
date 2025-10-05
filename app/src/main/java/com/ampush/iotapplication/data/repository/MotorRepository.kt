package com.ampush.iotapplication.data.repository

import android.content.Context
import androidx.work.*
import com.ampush.iotapplication.data.db.AppDatabase
import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.data.model.MotorData
import com.ampush.iotapplication.data.sms.SmsSender
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.WebhookService
import com.ampush.iotapplication.worker.SyncLogsWorker
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.TimeUnit
import com.ampush.iotapplication.utils.Logger

class MotorRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val logDao = database.logDao()
    private val smsSender = SmsSender(context)
    private val apiService = RetrofitClient.apiService
    private val webhookService = WebhookService()
    
    // SMS Commands
    suspend fun sendMotorOn(): Boolean {
        Logger.d("Sending Motor ON command via SMS", "REPOSITORY")
        try {
            val success = smsSender.sendMotorOn()
            if (success) {
                Logger.d("Motor ON SMS sent successfully", "REPOSITORY")
                
                // Send to webhook for real-time monitoring
                webhookService.sendMotorCommand("MOTOR_ON", "SMS_SENT", "8227070208")
                webhookService.sendSmsStatus("MOTOR_ON", "8227070208", "MOTORON", true)
                
                scheduleImmediateSync()
            } else {
                Logger.w("Motor ON SMS failed to send", "REPOSITORY")
                
                // Send failure to webhook
                webhookService.sendMotorCommand("MOTOR_ON", "SMS_FAILED", "8227070208")
                webhookService.sendSmsStatus("MOTOR_ON", "8227070208", "MOTORON", false)
            }
            return success
        } catch (e: Exception) {
            Logger.e("Error sending Motor ON SMS", e, "REPOSITORY")
            throw e
        }
    }
    
    suspend fun sendMotorOff(): Boolean {
        Logger.d("Sending Motor OFF command via SMS", "REPOSITORY")
        try {
            val success = smsSender.sendMotorOff()
            if (success) {
                Logger.d("Motor OFF SMS sent successfully", "REPOSITORY")
                
                // Send to webhook for real-time monitoring
                webhookService.sendMotorCommand("MOTOR_OFF", "SMS_SENT", "8227070208")
                webhookService.sendSmsStatus("MOTOR_OFF", "8227070208", "MOTOROFF", true)
                
                scheduleImmediateSync()
            } else {
                Logger.w("Motor OFF SMS failed to send", "REPOSITORY")
                
                // Send failure to webhook
                webhookService.sendMotorCommand("MOTOR_OFF", "SMS_FAILED", "8227070208")
                webhookService.sendSmsStatus("MOTOR_OFF", "8227070208", "MOTOROFF", false)
            }
            return success
        } catch (e: Exception) {
            Logger.e("Error sending Motor OFF SMS", e, "REPOSITORY")
            throw e
        }
    }
    
    suspend fun sendStatusRequest(): Boolean {
        Logger.d("Sending STATUS request via SMS", "REPOSITORY")
        try {
            val success = smsSender.sendStatusRequest()
            if (success) {
                Logger.d("STATUS SMS sent successfully", "REPOSITORY")
                
                // Send to webhook for real-time monitoring
                webhookService.sendMotorCommand("STATUS_REQUEST", "SMS_SENT", "8227070208")
                webhookService.sendSmsStatus("STATUS_REQUEST", "8227070208", "STATUS", true)
                
                scheduleImmediateSync()
            } else {
                Logger.w("STATUS SMS failed to send", "REPOSITORY")
                
                // Send failure to webhook
                webhookService.sendMotorCommand("STATUS_REQUEST", "SMS_FAILED", "8227070208")
                webhookService.sendSmsStatus("STATUS_REQUEST", "8227070208", "STATUS", false)
            }
            return success
        } catch (e: Exception) {
            Logger.e("Error sending STATUS SMS", e, "REPOSITORY")
            throw e
        }
    }
    
    // Database Operations
    fun getAllLogs(): Flow<List<LogEntity>> = logDao.getAllLogs()
    
    fun getLogsByDateRange(startDate: Date, endDate: Date): Flow<List<LogEntity>> = 
        logDao.getLogsByDateRange(startDate, endDate)
    
    suspend fun getLatestLog(): LogEntity? = logDao.getLatestLog()
    
    suspend fun saveMotorData(motorData: MotorData, command: String) {
        val logEntity = LogEntity(
            timestamp = motorData.timestamp,
            motorStatus = motorData.motorStatus,
            voltage = motorData.voltage,
            current = motorData.current,
            waterLevel = motorData.waterLevel,
            mode = motorData.mode,
            clock = motorData.clock,
            runTime = motorData.runTime,
            command = command,
            phoneNumber = motorData.phoneNumber,
            isSynced = false
        )
        
        logDao.insertLog(logEntity)
        scheduleSync()
    }
    
    suspend fun getUnsyncedLogs(): List<LogEntity> = logDao.getUnsyncedLogs()
    
    suspend fun markLogsAsSynced(ids: List<Long>) = logDao.markAsSynced(ids)
    
    // Fix existing logs with incorrect values
    suspend fun fixInvalidLogs() {
        Logger.d("Fixing logs with invalid STATUS_RESPONSE command", "REPOSITORY")
        val fixedCommands = logDao.fixStatusResponseCommand()
        Logger.i("Fixed $fixedCommands logs with STATUS_RESPONSE command", "REPOSITORY")
        
        Logger.d("Fixing logs with UNKNOWN motorStatus", "REPOSITORY")
        val fixedStatuses = logDao.fixUnknownMotorStatus()
        Logger.i("Fixed $fixedStatuses logs with UNKNOWN motorStatus", "REPOSITORY")
    }
    
    // Background Sync
    private fun scheduleSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncLogsWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_logs",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    // Immediate Sync for real-time commands
    private fun scheduleImmediateSync() {
        Logger.d("Scheduling immediate sync for real-time response", "REPOSITORY")
        val syncRequest = OneTimeWorkRequestBuilder<SyncLogsWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS) // NO DELAY!
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "immediate_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    fun schedulePeriodicSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncLogsWorker>(
            15, TimeUnit.MINUTES
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodic_sync_logs",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
    
    // Cleanup old logs (keep only last 30 days)
    suspend fun cleanupOldLogs() {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
        }.time
        
        logDao.deleteOldLogs(cutoffDate)
    }
}
