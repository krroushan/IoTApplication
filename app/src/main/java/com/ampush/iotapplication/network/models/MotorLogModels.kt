package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

// Motor Log Request Models
data class MotorLogRequest(
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("motorStatus")
    val motorStatus: String, // ON|OFF|STATUS
    
    @SerializedName("voltage")
    val voltage: Float?,
    
    @SerializedName("current")
    val current: Float?,
    
    @SerializedName("waterLevel")
    val waterLevel: Float?,
    
    @SerializedName("mode")
   val mode: String?,
    
    @SerializedName("clock")
    val clock: String?,
    
    @SerializedName("runTime")
    val runTime: Int?,
    
    @SerializedName("command")
    val command: String, // MOTORON|MOTOROFF|STATUS
    
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

// Single Log Response
data class SingleLogResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("syncedAt")
    val syncedAt: String
)

// Batch Log Response
data class BatchLogResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("syncedAt")
    val syncedAt: String
)

// Motor Log Entity (from server)
data class MotorLogEntity(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("deviceId")
    val deviceId: Int?,
    
    @SerializedName("userId")
    val userId: Int?,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("motorStatus")
    val motorStatus: String,
    
    @SerializedName("voltage")
    val voltage: Float?,
    
    @SerializedName("current")
    val current: Float?,
    
    @SerializedName("waterLevel")
    val waterLevel: Int?,
    
    @SerializedName("runTime")
    val runTime: Int?,
    
    @SerializedName("mode")
    val mode: String?,
    
    @SerializedName("clock")
    val clock: String?,
    
    @SerializedName("command")
    val command: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("deviceName")
    val deviceName: String?,
    
    @SerializedName("isSynced")
    val isSynced: Boolean,
    
    @SerializedName("device")
    val device: DeviceInfo?,
    
    @SerializedName("user")
    val user: UserInfo?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

// Device Info (nested in MotorLogEntity)
data class DeviceInfo(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("smsNumber")
    val smsNumber: String,
    
    @SerializedName("isActive")
    val isActive: Boolean
)

// User Info (nested in MotorLogEntity)
data class UserInfo(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

// Paginated Logs Response
data class PaginatedLogsResponse(
    @SerializedName("logs")
    val logs: List<MotorLogEntity>,
    
    @SerializedName("totalCount")
    val totalCount: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("hasNext")
    val hasNext: Boolean
)

// Unsynced Count Response
data class UnsyncedCountResponse(
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("message")
    val message: String
)

// Generic Success Response
data class SuccessResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String
)
