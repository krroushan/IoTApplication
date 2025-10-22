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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
     * Check if READ_PHONE_NUMBERS permission is granted (Android 6.0+)
     */
    fun hasPhoneNumbersPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
}
