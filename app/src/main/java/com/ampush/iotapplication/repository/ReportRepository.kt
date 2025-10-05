package com.ampush.iotapplication.repository

import com.ampush.iotapplication.network.ApiService
import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import java.util.Date

class ReportRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    
    /**
     * Get daily report for a specific date
     */
    suspend fun getDailyReport(
        date: Date,
        phoneNumber: String? = null
    ): Result<ReportResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            Logger.d("Getting daily report for ${date.time}, phone: $queryPhoneNumber", "REPORT_API")
            
            val response = apiService.getDailyReport(date.time, queryPhoneNumber)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved daily report successfully", "REPORT_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty daily report response", null, "REPORT_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get daily report: ${response.code()} - ${response.message()}", null, "REPORT_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during daily report", e, "REPORT_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get weekly report for a week starting date
     */
    suspend fun getWeeklyReport(
        weekStart: Date,
        phoneNumber: String? = null
    ): Result<ReportResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            Logger.d("Getting weekly report for ${weekStart.time}, phone: $queryPhoneNumber", "REPORT_API")
            
            val response = apiService.getWeeklyReport(weekStart.time, queryPhoneNumber)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved weekly report successfully", "REPORT_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty weekly report response", null, "REPORT_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get weekly report: ${response.code()} - ${response.message()}", null, "REPORT_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during weekly report", e, "REPORT_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get monthly report for a month starting date
     */
    suspend fun getMonthlyReport(
        monthStart: Date,
        phoneNumber: String? = null
    ): Result<ReportResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            Logger.d("Getting monthly report for ${monthStart.time}, phone: $queryPhoneNumber", "REPORT_API")
            
            val response = apiService.getMonthlyReport(monthStart.time, queryPhoneNumber)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved monthly report successfully", "REPORT_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty monthly report response", null, "REPORT_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get monthly report: ${response.code()} - ${response.message()}", null, "REPORT_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during monthly report", e, "REPORT_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get custom date range report
     */
    suspend fun getCustomReport(
        startDate: Date,
        endDate: Date,
        phoneNumber: String? = null
    ): Result<ReportResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            Logger.d("Getting custom report from ${startDate.time} to ${endDate.time}, phone: $queryPhoneNumber", "REPORT_API")
            
            val response = apiService.getCustomReport(startDate.time, endDate.time, queryPhoneNumber)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved custom report successfully", "REPORT_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty custom report response", null, "REPORT_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get custom report: ${response.code()} - ${response.message()}", null, "REPORT_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
              }
        } catch (e: Exception) {
            Logger.e("Exception during custom report", e, "REPORT_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get dashboard summary report (last N days)
     */
    suspend fun getSummaryReport(
        days: Int = 7,
        phoneNumber: String? = null
    ): Result<DashboardSummaryResponse> {
        return try {
            val userId = sessionManager.getUserPhone()
            val queryPhoneNumber = phoneNumber ?: userId
            
            Logger.d("Getting dashboard summary for $days days, phone: $queryPhoneNumber", "REPORT_API")
            
            val response = apiService.getSummaryReport(queryPhoneNumber, days)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Logger.i("Retrieved dashboard summary successfully", "REPORT_API")
                    Result.success(body)
                } ?: run {
                    Logger.e("Empty dashboard summary response", null, "REPORT_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Logger.e("Failed to get dashboard summary: ${response.code()} - ${response.message()}", null, "REPORT_API")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Exception during dashboard summary", e, "REPORT_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get report for today
     */
    suspend fun getTodayReport(phoneNumber: String? = null): Result<ReportResponse> {
        val today = Date()
        return getDailyReport(today, phoneNumber)
    }
    
    /**
     * Get report for yesterday
     */
    suspend fun getYesterdayReport(phoneNumber: String? = null): Result<ReportResponse> {
        val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
        return getDailyReport(yesterday, phoneNumber)
    }
    
    /**
     * Get report for this week (starting Monday)
     */
    suspend fun getThisWeekReport(phoneNumber: String? = null): Result<ReportResponse> {
        val calendar = java.util.Calendar.getInstance()
        calendar.firstDayOfWeek = java.util.Calendar.MONDAY
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        
        val weekStart = calendar.time
        return getWeeklyReport(weekStart, phoneNumber)
    }
    
    /**
     * Get report for this month
     */
    suspend fun getThisMonthReport(phoneNumber: String? = null): Result<ReportResponse> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        
        val monthStart = calendar.time
        return getMonthlyReport(monthStart, phoneNumber)
    }
}
