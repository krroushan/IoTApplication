package com.ampush.iotapplication.repository

import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.network.ApiService
import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MotorLogRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    
    // Sync Operations
    
    suspend fun syncSingleLog(logEntity: LogEntity): Result<SingleLogResponse> {
        return try {
            Logger.d("Syncing single log: ${logEntity.id}", "MOTOR_LOG_SYNC")
            
            val request = MotorLogRequest(
                timestamp = logEntity.timestamp.time.toString(),
                motorStatus = logEntity.motorStatus,
                voltage = logEntity.voltage,
                current = logEntity.current,
                waterLevel = logEntity.waterLevel,
                mode = logEntity.mode,
                clock = logEntity.clock,
                runTime = logEntity.runTime,
                command = logEntity.command,
                phoneNumber = logEntity.phoneNumber
            )
            
            val response = apiService.syncLog(request)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Successfully synced single log: ${body.id}", "MOTOR_LOG_SYNC")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty response body for single log sync", null, "MOTOR_LOG_SYNC")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to sync single log: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_SYNC")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during single log sync", e, "MOTOR_LOG_SYNC")
            Result.failure(e)
        }
    }
    
    suspend fun syncBatchLogs(logEntities: List<LogEntity>): Result<List<BatchLogResponse>> {
        return try {
            Logger.d("Syncing ${logEntities.size} logs in batch", "MOTOR_LOG_SYNC")
            
            val requests = logEntities.map { log ->
                MotorLogRequest(
                    timestamp = log.timestamp.time.toString(),
                    motorStatus = log.motorStatus,
                    voltage = log.voltage,
                    current = log.current,
                    waterLevel = log.waterLevel,
                    mode = log.mode,
                    clock = log.clock,
                    runTime = log.runTime,
                    command = log.command,
                    phoneNumber = log.phoneNumber
                )
            }
            
            val response = apiService.syncLogsBatch(requests)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Successfully synced ${body.size} logs in batch", "MOTOR_LOG_SYNC")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty response body for batch sync", null, "MOTOR_LOG_SYNC")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to sync batch logs: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_SYNC")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during batch log sync", e, "MOTOR_LOG_SYNC")
            Result.failure(e)
        }
    }
    
    // Data Retrieval Operations
    
    suspend fun getRemoteLogs(
        startDate: Date? = null,
        endDate: Date? = null,
        phoneNumber: String? = null,
        motorStatus: String? = null,
        page: Int = 0,
        size: Int = 100
    ): Result<PaginatedLogsResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            val response = apiService.getLogs(
                startDate = startDate?.time,
                endDate = endDate?.time,
                phoneNumber = queryPhoneNumber,
                motorStatus = motorStatus,
                page = page,
                size = size
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved ${body.logs.size} remote logs (page $page)", "MOTOR_LOG_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty response body for get logs", null, "MOTOR_LOG_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get remote logs: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during get remote logs", e, "MOTOR_LOG_API")
            Result.failure(e)
        }
    }
    
    suspend fun getRemoteLog(logId: Long): Result<MotorLogEntity> {
        return try {
            val response = apiService.getLog(logId)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved remote log: $logId", "MOTOR_LOG_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty response body for get log $logId", null, "MOTOR_LOG_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get remote log $logId: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during get remote log $logId", e, "MOTOR_LOG_API")
            Result.failure(e)
        }
    }
    
    suspend fun getUnsyncedLogsCount(): Result<Int> {
        return try {
            val response = apiService.getUnsyncedLogsCount()
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved unsynced count: ${body.count}", "MOTOR_LOG_API")
                    Result.success(body.count)
                } ?: run {
                    Logger.e("Empty response body for unsynced count", null, "MOTOR_LOG_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get unsynced count: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during get unsynced count", e, "MOTOR_LOG_API")
            Result.failure(e)
        }
    }
    
    // Delete Operations
    
    suspend fun deleteRemoteLog(logId: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteLog(logId)
            
            if (response.isSuccessful) {
                Logger.i("Successfully deleted remote log: $logId", "MOTOR_LOG_API")
                Result.success(true)
            } else {
                Logger.e("Failed to delete remote log $logId: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during delete remote log $logId", e, "MOTOR_LOG_API")
            Result.failure(e)
        }
    }
    
    // Health Check
    
    suspend fun checkApiHealth(): Result<Boolean> {
        return try {
            val response = apiService.getApiHealth()
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val isHealthy = body.success
                    Logger.i("API health check result: $isHealthy", "MOTOR_LOG_API")
                    Result.success(isHealthy)
                } ?: run {
                    Logger.e("Empty response body for health check", null, "MOTOR_LOG_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("API health check failed: ${response.code()} - ${response.message()}", null, "MOTOR_LOG_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during API health check", e, "MOTOR_LOG_API")
            Result.failure(e)
        }
    }
}
