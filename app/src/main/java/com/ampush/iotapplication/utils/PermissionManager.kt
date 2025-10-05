package com.ampush.iotapplication.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionManager {
    
    // SMS Permissions
    const val SEND_SMS = Manifest.permission.SEND_SMS
    const val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
    const val READ_SMS = Manifest.permission.READ_SMS
    
    // Notification Permission for Android 13+
    const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
    
    // Permission request codes
    const val SMS_PERMISSION_REQUEST = 100
    const val SMS_RECEIVE_REQUEST = 101
    const val NOTIFICATION_PERMISSION_REQUEST = 102
    
    /**
     * Check if SMS sending permission is granted
     */
    fun hasSendSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if SMS receiving permission is granted
     */
    fun hasReceiveSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if SMS reading permission is granted
     */
    fun hasReadSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if all SMS permissions are granted
     */
    fun hasAllSmsPermissions(context: Context): Boolean {
        return hasSendSmsPermission(context) && 
               hasReceiveSmsPermission(context) && 
               hasReadSmsPermission(context)
    }
    
    /**
     * Check if all required permissions are granted (SMS + Notifications)
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasAllSmsPermissions(context) && hasNotificationPermission(context)
    }
    
    /**
     * Get array of missing SMS permissions
     */
    fun getMissingSmsPermissions(context: Context): Array<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!hasSendSmsPermission(context)) {
            missingPermissions.add(SEND_SMS)
        }
        if (!hasReceiveSmsPermission(context)) {
            missingPermissions.add(RECEIVE_SMS)
        }
        if (!hasReadSmsPermission(context)) {
            missingPermissions.add(READ_SMS)
        }
        
        return missingPermissions.toTypedArray()
    }
    
    /**
     * Get array of missing notification permissions
     */
    fun getMissingNotificationPermissions(context: Context): Array<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!hasNotificationPermission(context)) {
            missingPermissions.add(POST_NOTIFICATIONS)
        }
        
        return missingPermissions.toTypedArray()
    }
    
    /**
     * Get array of all missing permissions (SMS + Notifications)
     */
    fun getAllMissingPermissions(context: Context): Array<String> {
        val missingPermissions = mutableListOf<String>()
        missingPermissions.addAll(getMissingSmsPermissions(context))
        missingPermissions.addAll(getMissingNotificationPermissions(context))
        return missingPermissions.toTypedArray()
    }
    
    /**
     * Log permission status for debugging
     */
    fun logPermissionStatus(context: Context) {
        Logger.d("=== Permission Status ===", "PERMISSIONS")
        Logger.d("SEND_SMS: ${hasSendSmsPermission(context)}", "PERMISSIONS")
        Logger.d("RECEIVE_SMS: ${hasReceiveSmsPermission(context)}", "PERMISSIONS")
        Logger.d("READ_SMS: ${hasReadSmsPermission(context)}", "PERMISSIONS")
        Logger.d("POST_NOTIFICATIONS: ${hasNotificationPermission(context)}", "PERMISSIONS")
        Logger.d("All SMS Permissions: ${hasAllSmsPermissions(context)}", "PERMISSIONS")
        Logger.d("All Required Permissions: ${hasAllRequiredPermissions(context)}", "PERMISSIONS")
        
        val missing = getAllMissingPermissions(context)
        if (missing.isNotEmpty()) {
            Logger.w("Missing permissions: ${missing.joinToString(", ")}", "PERMISSIONS")
        }
    }
}
