package com.ampush.iotapplication.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    /**
     * Check if all required permissions for SIM detection are granted
     */
    fun hasSimDetectionPermissions(context: Context): Boolean {
        val permissions = getSimDetectionPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get required permissions for SIM detection
     */
    fun getSimDetectionPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS
            )
        } else {
            arrayOf(Manifest.permission.READ_PHONE_STATE)
        }
    }
    
    /**
     * Get missing permissions for SIM detection
     */
    fun getMissingSimDetectionPermissions(context: Context): Array<String> {
        val requiredPermissions = getSimDetectionPermissions()
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
    
    /**
     * Check if READ_PHONE_STATE permission is granted
     */
    fun hasPhoneStatePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if READ_PHONE_NUMBERS permission is granted (Android 13+)
     */
    fun hasPhoneNumbersPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notification permission not required for older versions
        }
    }
    
    /**
     * Check if SMS permissions are granted
     */
    fun hasSmsPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get all SMS permissions
     */
    fun getSmsPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
    }
    
    /**
     * Get notification permission (Android 13+)
     */
    fun getNotificationPermission(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
    
    /**
     * Get all essential permissions for the app to function properly
     */
    fun getAllEssentialPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // SIM detection permissions
        val simPermissions = getSimDetectionPermissions()
        permissions.addAll(simPermissions)
        
        // SMS permissions
        val smsPermissions = getSmsPermissions()
        permissions.addAll(smsPermissions)
        
        // Notification permission (Android 13+)
        val notificationPermissions = getNotificationPermission()
        permissions.addAll(notificationPermissions)
        
        return permissions.toTypedArray()
    }
    
    /**
     * Check if all essential permissions are granted
     */
    fun hasAllEssentialPermissions(context: Context): Boolean {
        return getAllEssentialPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
