package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

// Motor Command Webhook Request
data class MotorCommandWebhookRequest(
    @SerializedName("event")
    val event: String = "motor_command",
    
    @SerializedName("command")
    val command: String, // MOTOR_ON, MOTOR_OFF, STATUS
    
    @SerializedName("status")
    val status: String, // SMS_SENT, SMS_DELIVERED, SMS_FAILED, MOTOR_ON, MOTOR_OFF, ERROR
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("app")
    val app: String = "IoT_Motor_Control",
    
    @SerializedName("version")
    val version: String = "1.0.0"
)

// SMS Status Webhook Request
data class SmsStatusWebhookRequest(
    @SerializedName("event")
    val event: String = "sms_status",
    
    @SerializedName("sms_type")
    val smsType: String, // MOTOR_ON, MOTOR_OFF, STATUS
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("message")
    val message: String, // MOTORON, MOTOROFF, STATUS SMS content
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("app")
    val app: String = "IoT_Motor_Control"
)

// Webhook Response
data class WebhookResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("received_at")
    val receivedAt: String,
    
    @SerializedName("event_id")
    val eventId: String
)

// Webhook Health Response
data class WebhookHealthResponse(
    @SerializedName("status")
    val status: String, // healthy
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("webhooks")
    val webhooks: Map<String, String>,
    
    @SerializedName("version")
    val version: String
)

// API Health Response
data class ApiHealthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("version")
    val version: String
)
