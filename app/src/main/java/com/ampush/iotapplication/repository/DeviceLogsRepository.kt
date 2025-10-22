package com.ampush.iotapplication.repository

import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.models.MotorLogEntity
import com.ampush.iotapplication.network.models.PaginatedLogsResponse
import com.ampush.iotapplication.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceLogsRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get motor logs for a specific device from API
     */
    suspend fun getDeviceLogs(
        deviceId: Int,
        startDate: String? = null,
        endDate: String? = null,
        phoneNumber: String? = null,
        motorStatus: String? = null,
        page: Int = 0,
        size: Int = 100
    ): Result<List<MotorLogEntity>> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Fetching logs for device ID: $deviceId", "API")
            
            val response = apiService.getLogsByDeviceId(
                deviceId = deviceId,
                startDate = startDate,
                endDate = endDate,
                phoneNumber = phoneNumber,
                motorStatus = motorStatus,
                page = page,
                size = size
            )
            
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    Logger.d("Successfully fetched ${data.logs.size} logs for device $deviceId", "API")
                    Result.success(data.logs)
                } else {
                    Logger.w("Empty response body for device $deviceId", "API")
                    Result.success(emptyList())
                }
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Logger.e("Failed to fetch logs for device $deviceId", null, "API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Exception fetching logs for device $deviceId", e, "API")
            Result.failure(e)
        }
    }
    
    /**
     * Get all motor logs from API
     */
    suspend fun getAllLogs(
        startDate: Long? = null,
        endDate: Long? = null,
        phoneNumber: String? = null,
        motorStatus: String? = null,
        page: Int = 0,
        size: Int = 100
    ): Result<List<MotorLogEntity>> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Fetching all logs from API", "API")
            
            val response = apiService.getLogs(
                startDate = startDate,
                endDate = endDate,
                phoneNumber = phoneNumber,
                motorStatus = motorStatus,
                page = page,
                size = size
            )
            
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    Logger.d("Successfully fetched ${data.logs.size} logs from API", "API")
                    Result.success(data.logs)
                } else {
                    Logger.w("Empty response body from API", "API")
                    Result.success(emptyList())
                }
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Logger.e("Failed to fetch logs from API", null, "API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Exception fetching logs from API", e, "API")
            Result.failure(e)
        }
    }
}
