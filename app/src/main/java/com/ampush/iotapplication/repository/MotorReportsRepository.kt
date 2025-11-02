package com.ampush.iotapplication.repository

import com.ampush.iotapplication.network.ApiService
import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MotorReportsRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Get daily consumption report
     */
    suspend fun getDailyReport(
        date: Date? = null,
        deviceId: Int? = null,
        userId: Int? = null,
        phone: String? = null
    ): Result<DailyReportResponse> {
        return try {
            val dateStr = date?.let { dateFormat.format(it) }
            val userPhone = phone ?: sessionManager.getUserPhone()
            val customerId = userId ?: sessionManager.getCustomerId().toInt()
            
            Logger.d("Getting daily consumption report: date=$dateStr, device=$deviceId, user=$customerId, phone=$userPhone", "MOTOR_REPORTS")
            
            // Prefer user_id over phone for API call
            val response = apiService.getDailyConsumptionReport(
                date = dateStr,
                deviceId = deviceId,
                userId = if (customerId > 0) customerId else null,
                phone = if (customerId == 0 && userPhone != null) userPhone else null
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Daily report retrieved successfully: ${body.summary.dailyConsumption} kWh", "MOTOR_REPORTS")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty daily report response", null, "MOTOR_REPORTS")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get daily report: ${response.code()} - ${response.message()}", null, "MOTOR_REPORTS")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during daily report", e, "MOTOR_REPORTS")
            Result.failure(e)
        }
    }
    
    /**
     * Get monthly consumption report
     */
    suspend fun getMonthlyReport(
        month: Int? = null,
        year: Int? = null,
        deviceId: Int? = null,
        userId: Int? = null,
        phone: String? = null
    ): Result<MonthlyReportResponse> {
        return try {
            val userPhone = phone ?: sessionManager.getUserPhone()
            val customerId = userId ?: sessionManager.getCustomerId().toInt()
            
            Logger.d("Getting monthly consumption report: month=$month, year=$year, device=$deviceId, user=$customerId", "MOTOR_REPORTS")
            
            val response = apiService.getMonthlyConsumptionReport(
                month = month,
                year = year,
                deviceId = deviceId,
                userId = if (customerId > 0) customerId else null,
                phone = if (customerId == 0 && userPhone != null) userPhone else null
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Monthly report retrieved successfully: ${body.summary.monthlyConsumption} kWh", "MOTOR_REPORTS")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty monthly report response", null, "MOTOR_REPORTS")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get monthly report: ${response.code()} - ${response.message()}", null, "MOTOR_REPORTS")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during monthly report", e, "MOTOR_REPORTS")
            Result.failure(e)
        }
    }
    
    /**
     * Get yearly consumption report
     * Retries once on timeout errors
     */
    suspend fun getYearlyReport(
        year: Int? = null,
        deviceId: Int? = null,
        userId: Int? = null,
        phone: String? = null
    ): Result<YearlyReportResponse> {
        val userPhone = phone ?: sessionManager.getUserPhone()
        val customerId = userId ?: sessionManager.getCustomerId().toInt()
        
        // Retry logic: try twice, especially for timeout errors
        var lastException: Exception? = null
        repeat(2) { attempt ->
            try {
                if (attempt > 0) {
                    Logger.w("Retrying yearly report (attempt ${attempt + 1})", "MOTOR_REPORTS")
                    // Wait a bit before retry
                    delay(1000)
                }
                
                Logger.d("Getting yearly consumption report: year=$year, device=$deviceId, user=$customerId (attempt ${attempt + 1})", "MOTOR_REPORTS")
                
                val response = apiService.getYearlyConsumptionReport(
                    year = year,
                    deviceId = deviceId,
                    userId = if (customerId > 0) customerId else null,
                    phone = if (customerId == 0 && userPhone != null) userPhone else null
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Logger.i("Yearly report retrieved successfully: ${body.summary.annualConsumption} kWh", "MOTOR_REPORTS")
                        return Result.success(body)
                    } ?: run {
                        Logger.e("Empty yearly report response", null, "MOTOR_REPORTS")
                        return Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Logger.e("Failed to get yearly report: ${response.code()} - ${response.message()}", null, "MOTOR_REPORTS")
                    return Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                lastException = e
                val isTimeoutError = e is java.net.SocketTimeoutException || 
                                     e.message?.contains("timeout", ignoreCase = true) == true
                
                if (isTimeoutError && attempt == 0) {
                    // Will retry on next iteration
                    Logger.w("Timeout error on yearly report, will retry: ${e.message}", "MOTOR_REPORTS")
                } else {
                    Logger.e("Exception during yearly report (attempt ${attempt + 1})", e, "MOTOR_REPORTS")
                    if (attempt == 1) {
                        // Last attempt failed
                        return Result.failure(e)
                    }
                }
            }
        }
        
        // If we get here, both attempts failed
        return Result.failure(lastException ?: Exception("Failed to get yearly report after retries"))
    }
    
    /**
     * Get custom date range consumption report
     */
    suspend fun getCustomReport(
        startDate: Date,
        endDate: Date,
        deviceId: Int? = null,
        userId: Int? = null,
        phone: String? = null,
        groupBy: String = "day"
    ): Result<CustomReportResponse> {
        return try {
            val startDateStr = dateFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)
            val userPhone = phone ?: sessionManager.getUserPhone()
            val customerId = userId ?: sessionManager.getCustomerId().toInt()
            
            Logger.d("Getting custom consumption report: $startDateStr to $endDateStr, user=$customerId", "MOTOR_REPORTS")
            
            val response = apiService.getCustomConsumptionReport(
                startDate = startDateStr,
                endDate = endDateStr,
                deviceId = deviceId,
                userId = if (customerId > 0) customerId else null,
                phone = if (customerId == 0 && userPhone != null) userPhone else null,
                groupBy = groupBy
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Custom report retrieved successfully: ${body.summary.totalConsumption} kWh", "MOTOR_REPORTS")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty custom report response", null, "MOTOR_REPORTS")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get custom report: ${response.code()} - ${response.message()}", null, "MOTOR_REPORTS")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during custom report", e, "MOTOR_REPORTS")
            Result.failure(e)
        }
    }
    
    /**
     * Get today's report
     */
    suspend fun getTodayReport(deviceId: Int? = null): Result<DailyReportResponse> {
        return getDailyReport(date = Date(), deviceId = deviceId)
    }
    
    /**
     * Get this month's report
     */
    suspend fun getThisMonthReport(deviceId: Int? = null): Result<MonthlyReportResponse> {
        val calendar = Calendar.getInstance()
        return getMonthlyReport(
            month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR),
            deviceId = deviceId
        )
    }
    
    /**
     * Get this year's report
     */
    suspend fun getThisYearReport(deviceId: Int? = null): Result<YearlyReportResponse> {
        val calendar = Calendar.getInstance()
        return getYearlyReport(
            year = calendar.get(Calendar.YEAR),
            deviceId = deviceId
        )
    }
}

