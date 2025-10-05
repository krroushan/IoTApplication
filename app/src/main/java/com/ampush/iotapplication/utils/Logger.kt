package com.ampush.iotapplication.utils

import android.util.Log

object Logger {
    private const val TAG = "IoTMotorControl"
    
    // Enable logging (always on for debugging)
    private val isDebugMode = true
    
    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }
    
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }
    
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    // Specific logging functions for different components
    fun logNetworkRequest(url: String, method: String = "GET") {
        d("🌐 Network Request: $method $url", "NETWORK")
    }
    
    fun logNetworkResponse(url: String, responseCode: Int, responseBody: String? = null) {
        d("🌐 Network Response: $url -> $responseCode ${responseBody?.let { "Body: $it" } ?: ""}", "NETWORK")
    }
    
    fun logNetworkError(url: String, error: String, throwable: Throwable? = null) {
        e("🌐 Network Error: $url -> $error", throwable, "NETWORK")
    }
    
    fun logDatabaseOperation(operation: String, table: String, details: String = "") {
        d("💾 Database: $operation on $table $details", "DATABASE")
    }
    
    fun logDatabaseError(operation: String, error: String, throwable: Throwable? = null) {
        e("💾 Database Error: $operation -> $error", throwable, "DATABASE")
    }
    
    fun logSmsReceived(sender: String, message: String) {
        i("📱 SMS Received from $sender: $message", "SMS")
    }
    
    fun logSmsError(error: String, throwable: Throwable? = null) {
        e("📱 SMS Error: $error", throwable, "SMS")
    }
    
    fun logMotorCommand(command: String, status: String) {
        i("🔧 Motor Command: $command -> Status: $status", "MOTOR")
    }
    
    fun logMotorError(command: String, error: String, throwable: Throwable? = null) {
        e("🔧 Motor Error: Command '$command' failed -> $error", throwable, "MOTOR")
    }
    
    fun logUIEvent(screen: String, event: String, details: String = "") {
        d("🎨 UI Event: $screen -> $event $details", "UI")
    }
    
    fun logUIError(screen: String, error: String, throwable: Throwable? = null) {
        e("🎨 UI Error: $screen -> $error", throwable, "UI")
    }
    
    fun logWorkerStart(workerName: String) {
        i("⚙️ Worker Started: $workerName", "WORKER")
    }
    
    fun logWorkerComplete(workerName: String, result: String) {
        i("⚙️ Worker Completed: $workerName -> $result", "WORKER")
    }
    
    fun logWorkerError(workerName: String, error: String, throwable: Throwable? = null) {
        e("⚙️ Worker Error: $workerName -> $error", throwable, "WORKER")
    }
}
