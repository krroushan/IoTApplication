package com.ampush.iotapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ampush.iotapplication.data.db.AppDatabase
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.repository.MotorLogRepository
import com.ampush.iotapplication.notifications.NotificationHelper
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager

class SyncLogsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val database = AppDatabase.getDatabase(applicationContext)
    private val logDao = database.logDao()
    private val apiService = RetrofitClient.apiService
    private val sessionManager = SessionManager(context)
    private val motorLogRepository = MotorLogRepository(apiService, sessionManager)
    private val notificationHelper = NotificationHelper(context)
    
    override suspend fun doWork(): Result {
        Logger.i("Starting sync worker - attempt ${runAttemptCount}", "SYNC_WORKER")
        
        return try {
            // Check API health first
            val healthResult = motorLogRepository.checkApiHealth()
            if (healthResult.isFailure) {
                Logger.e("API health check failed, skipping sync", healthResult.exceptionOrNull(), "SYNC_WORKER")
                return Result.retry()
            }
            
            // Get unsynced logs
            val unsyncedLogs = logDao.getUnsyncedLogs()
            Logger.d("Found ${unsyncedLogs.size} unsynced logs", "SYNC_WORKER")
            
            if (unsyncedLogs.isEmpty()) {
                Logger.i("No unsynced logs found", "SYNC_WORKER")
                notificationHelper.showSyncNotification("All logs are up to date")
                return Result.success()
            }
            
            // Batch sync for efficiency (max 50 logs per Batch)
            val batchSize = 50
            val batches = unsyncedLogs.chunked(batchSize)
            var totalSynced = 0
            var hasErrors = false
            
            for ((index, batch) in batches.withIndex()) {
                Logger.d("Syncing batch ${index + 1}/${batches.size} (${batch.size} logs)", "SYNC_WORKER")
                
                val result = motorLogRepository.syncBatchLogs(batch)
                
                result.fold(
                    onSuccess = { responses ->
                        val successful = responses.count { it.success }
                        val batchSyncedIds = batch.take(successful).map { it.id }
                        
                        if (batchSyncedIds.isNotEmpty()) {
                            logDao.markAsSynced(batchSyncedIds)
                            totalSynced += batchSyncedIds.size
                            Logger.i("Marked ${batchSyncedIds.size} logs as synced in batch ${index + 1}", "SYNC_WORKER")
                        }
                        
                        val failures = responses.count { !it.success }
                        if (failures > 0) {
                            hasErrors = true
                            Logger.w("$failures logs failed to sync in batch ${index + 1}", "SYNC_WORKER")
                        }
                    },
                    onFailure = { exception ->
                        hasErrors = true
                        Logger.e("Batch ${index + 1} sync failed", exception, "SYNC_WORKER")
                    }
                )
                
                // Small delay between batches to avoid rate limiting
                if (index < batches.size - 1) {
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            // Show notification with results
            val message = when {
                totalSynced == unsyncedLogs.size -> "Successfully synced all ${totalSynced} logs"
                totalSynced > 0 -> "Synced ${totalSynced}/${unsyncedLogs.size} logs (some failed)"
                else -> "Failed to sync any logs"
            }
            
            notificationHelper.showSyncNotification(message)
            
            when {
                totalSynced == unsyncedLogs.size -> {
                    Logger.i("All logs synced successfully: $totalSynced", "SYNC_WORKER")
                    Result.success()
                }
                totalSynced > 0 -> {
                    Logger.w("Partial sync success: $totalSynced/${unsyncedLogs.size}", "SYNC_WORKER")
                    if (runAttemptCount >= 3) {
                        Result.success() // Don't retry forever
                    } else {
                        Result.retry()
                    }
                }
                else -> {
                    Logger.e("No logs synced, retrying", null, "SYNC_WORKER")
                    Result.retry()
                }
            }
            
        } catch (e: Exception) {
            Logger.e("Sync worker exception", e, "SYNC_WORKER")
            notificationHelper.showSyncNotification("Sync failed: ${e.message}")
            
            when (runAttemptCount) {
                in 1..3 -> Result.retry()
                else -> {
                    Logger.e("Max retry attempts reached, marking sync as failed", null, "SYNC_WORKER")
                    Result.failure()
                }
            }
        }
    }
}
