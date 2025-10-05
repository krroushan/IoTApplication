package com.ampush.iotapplication.network

import com.ampush.iotapplication.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class WebhookService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val baseUrl = "https://laravel1.wizzyweb.com/api/"
    // Testing webhook URLs - using webhook.site for testing
    private val motorCommandWebhookUrl = "https://webhook.site/0191f637-5239-4fbb-95c2-c5044e2a8cb5"
    private val smsStatusWebhookUrl = "https://webhook.site/0191f637-5239-4fbb-95c2-c5044e2a8cb5"
    
    /**
     * Send motor command to webhook for real-time monitoring
     */
    suspend fun sendMotorCommand(
        command: String,
        status: String,
        phoneNumber: String,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        
            Logger.logNetworkRequest(motorCommandWebhookUrl, "POST")
        
        try {
            val jsonPayload = JSONObject().apply {
                put("event", "motor_command")
                put("command", command)
                put("status", status)
                put("phone_number", phoneNumber)
                put("timestamp", timestamp)
                put("app", "IoT_Motor_Control")
                put("version", "1.0.0")
            }
            
            val requestBody = jsonPayload.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(motorCommandWebhookUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "IoT-Motor-Control-App")
                .build()
            
            Logger.d("📤 Webhook Request: $requestBody", "WEBHOOK")
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Logger.logNetworkResponse(motorCommandWebhookUrl, response.code, response.body?.string())
                Logger.i("✅ Webhook sent successfully: $command -> $status", "WEBHOOK")
                true
            } else {
                Logger.logNetworkError(motorCommandWebhookUrl, "HTTP ${response.code}: ${response.message}")
                false
            }
            
        } catch (e: IOException) {
            Logger.logNetworkError(motorCommandWebhookUrl, "Network error: ${e.message}", e)
            false
        } catch (e: Exception) {
            Logger.logNetworkError(motorCommandWebhookUrl, "Unexpected error: ${e.message}", e)
            false
        }
    }
    
    /**
     * Send SMS status update to webhook
     */
    suspend fun sendSmsStatus(
        smsType: String,
        phoneNumber: String,
        message: String,
        success: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        
        try {
            val jsonPayload = JSONObject().apply {
                put("event", "sms_status")
                put("sms_type", smsType)
                put("phone_number", phoneNumber)
                put("message", message)
                put("success", success)
                put("timestamp", timestamp)
                put("app", "IoT_Motor_Control")
            }
            
            val requestBody = jsonPayload.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(smsStatusWebhookUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            Logger.d("📤 SMS Webhook: $requestBody", "WEBHOOK")
            
            val response = client.newCall(request).execute()
            response.isSuccessful
            
        } catch (e: Exception) {
            Logger.e("Webhook SMS status failed", e, "WEBHOOK")
            false
        }
    }
    
    /**
     * Send app status/heartbeat to webhook
     */
    suspend fun sendHeartbeat(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("event", "app_heartbeat")
                put("app", "IoT_Motor_Control")
                put("status", "running")
                put("timestamp", System.currentTimeMillis())
            }
            
            val requestBody = jsonPayload.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(motorCommandWebhookUrl)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            Logger.d("💓 Heartbeat sent to webhook", "WEBHOOK")
            response.isSuccessful
            
        } catch (e: Exception) {
            Logger.e("Heartbeat webhook failed", e, "WEBHOOK")
            false
        }
    }
}
