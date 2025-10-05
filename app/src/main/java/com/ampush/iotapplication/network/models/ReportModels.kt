package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

// Report Response Models
data class ReportResponse(
    @SerializedName("period")
    val period: String, // daily, weekly, monthly, last_N_days
    
    @SerializedName("startDate")
    val startDate: String, // ISO 8601 format
    
    @SerializedName("endDate")
    val endDate: String, // ISO 8601 format
    
    @SerializedName("totalOperations")
    val totalOperations: Int,
    
    @SerializedName("motorOnCount")
    val motorOnCount: Int,
    
    @SerializedName("motorOffCount")
    val motorOffCount: Int,
    
    @SerializedName("statusRequests")
    val statusRequests: Int,
    
    @SerializedName("averageVoltage")
    val averageVoltage: Float?,
    
    @SerializedName("averageCurrent")
    val averageCurrent: Float?,
    
    @SerializedName("averageWaterLevel")
    val averageWaterLevel: Float?,
    
    @SerializedName("uptime")
    val uptime: String, // "8h 30m"
    
    @SerializedName("downtime")
    val downtime: String, // "15h 30m"
    
    @SerializedName("totalMinutes")
    val totalMinutes: Int,
    
    @SerializedName("uptimeMinutes")
    val uptimeMinutes: Int,
    
    @SerializedName("downtimeMinutes")
    val downtimeMinutes: Int
)

// Dashboard Summary Response
data class DashboardSummaryResponse(
    @SerializedName("period")
    val period: String, // "last_7_days", "last_30_days", etc.
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("totalOperations")
    val totalOperations: Int,
    
    @SerializedName("motorOnCount")
    val motorOnCount: Int,
    
    @SerializedName("motorOffCount")
    val motorOffCount: Int,
    
    @SerializedName("statusRequests")
    val statusRequests: Int,
    
    @SerializedName("uniquePhoneNumbers")
    val uniquePhoneNumbers: Int,
    
    @SerializedName("averageVoltage")
    val averageVoltage: Float?,
    
    @SerializedName("averageCurrent")
    val averageCurrent: Float?,
    
    @SerializedName("averageWaterLevel")
    val averageWaterLevel: Float?
)
