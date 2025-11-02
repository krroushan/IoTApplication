package com.ampush.iotapplication.network

import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication API Endpoints
    @POST("customer/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<CustomerResponse>
    
    @GET("customer/devices")
    suspend fun getMyDevices(@Header("Authorization") token: String): Response<DevicesResponse>
    
    @POST("customer/fcm-token")
    suspend fun updateFcmToken(@Header("Authorization") token: String, @Body request: FcmTokenRequest): Response<FcmTokenResponse>
    
    // Motor Logs API Endpoints
    @POST("logs")
    suspend fun syncLog(@Body logRequest: MotorLogRequest): Response<SingleLogResponse>
    
    @POST("logs/batch")
    suspend fun syncLogsBatch(@Body logRequests: List<MotorLogRequest>): Response<List<BatchLogResponse>>
    
    @GET("logs")
    suspend fun getLogs(
        @Query("startDate") startDate: Long? = null,
        @Query("endDate") endDate: Long? = null,
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("motorStatus") motorStatus: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PaginatedLogsResponse>
    
    @GET("logs")
    suspend fun getLogsByDeviceId(
        @Query("deviceId") deviceId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("motorStatus") motorStatus: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PaginatedLogsResponse>
    
    @GET("validate-phone")
    suspend fun validatePhoneNumber(
        @Query("phone") phone: String
    ): Response<PhoneValidationResponse>
    
    @GET("logs/{id}")
    suspend fun getLog(@Path("id") id: Long): Response<MotorLogEntity>
    
    @DELETE("logs/{id}")
    suspend fun deleteLog(@Path("id") id: Long): Response<SuccessResponse>
    
    @GET("logs/unsynced/count")
    suspend fun getUnsyncedLogsCount(): Response<UnsyncedCountResponse>
    
    // Motor Consumption Reports API Endpoints
    @GET("reports/daily")
    suspend fun getDailyConsumptionReport(
        @Query("date") date: String? = null,
        @Query("device_id") deviceId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("phone") phone: String? = null
    ): Response<DailyReportResponse>
    
    @GET("reports/monthly")
    suspend fun getMonthlyConsumptionReport(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Query("device_id") deviceId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("phone") phone: String? = null
    ): Response<MonthlyReportResponse>
    
    @GET("reports/yearly")
    suspend fun getYearlyConsumptionReport(
        @Query("year") year: Int? = null,
        @Query("device_id") deviceId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("phone") phone: String? = null
    ): Response<YearlyReportResponse>
    
    @GET("reports/custom")
    suspend fun getCustomConsumptionReport(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("device_id") deviceId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("phone") phone: String? = null,
        @Query("group_by") groupBy: String? = "day"
    ): Response<CustomReportResponse>
    
    // Legacy Reports API Endpoints (kept for backward compatibility)
    @GET("reports/daily")
    suspend fun getDailyReport(
        @Query("date") date: Long,
        @Query("phoneNumber") phoneNumber: String? = null
    ): Response<ReportResponse>
    
    @GET("reports/weekly")
    suspend fun getWeeklyReport(
        @Query("weekStart") weekStart: Long,
        @Query("phoneNumber") phoneNumber: String? = null
    ): Response<ReportResponse>
    
    @GET("reports/monthly")
    suspend fun getMonthlyReport(
        @Query("monthStart") monthStart: Long,
        @Query("phoneNumber") phoneNumber: String? = null
    ): Response<ReportResponse>
    
    @GET("reports/custom")
    suspend fun getCustomReport(
        @Query("startDate") startDate: Long,
        @Query("endDate") endDate: Long,
        @Query("phoneNumber") phoneNumber: String? = null
    ): Response<ReportResponse>
    
    @GET("reports/summary")
    suspend fun getSummaryReport(
        @Query("phoneNumber") phoneNumber: String? = null,
        @Query("days") days: Int = 7
    ): Response<DashboardSummaryResponse>
    
    // Webhook Endpoints
    @POST("webhooks/motor-command")
    suspend fun sendMotorCommandWebhook(@Body webhookRequest: MotorCommandWebhookRequest): Response<WebhookResponse>
    
    @POST("webhooks/sms-status")
    suspend fun sendSmsStatusWebhook(@Body webhookRequest: SmsStatusWebhookRequest): Response<WebhookResponse>
    
    @GET("webhooks/health")
    suspend fun getWebhookHealth(): Response<WebhookHealthResponse>
    
    // API Health Check
    @GET("health")
    suspend fun getApiHealth(): Response<ApiHealthResponse>
}
