package com.ampush.iotapplication.data.sms

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.PermissionManager
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.DefaultDeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class SmsSender(private val context: Context) {
    
    private val smsManager = SmsManager.getDefault()
    private val deviceManager = DeviceManager(context)
    private val defaultDeviceManager = DefaultDeviceManager(context)
    
    /**
     * Get target phone number - uses default device or first saved device
     */
    private fun getTargetNumber(): String? {
        // Try default device first
        val defaultDevice = defaultDeviceManager.getDefaultDevice()
        if (defaultDevice != null) {
            Logger.d("Using default device: ${defaultDevice.smsNumber}", "SMS")
            return defaultDevice.smsNumber
        }
        
        // Fallback to first saved device
        val devices = deviceManager.getSavedDevices()
        if (devices.isNotEmpty()) {
            Logger.d("Using first saved device: ${devices[0].smsNumber}", "SMS")
            return devices[0].smsNumber
        }
        
        Logger.w("No devices configured for customer", "SMS")
        return null
    }
    
    private fun hasSmsPermission(): Boolean {
        return PermissionManager.hasSendSmsPermission(context)
    }
    
    
    suspend fun sendMotorOn(): Boolean = withContext(Dispatchers.IO) {
        val targetNumber = getTargetNumber()
        
        if (targetNumber == null) {
            Logger.e("No device configured for SMS", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Please configure a device first", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        Logger.d("Attempting to send MOTORON SMS to $targetNumber", "SMS")

        if (!hasSmsPermission()) {
            Logger.e("SMS permission not granted", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "SMS permission required", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        try {
            smsManager.sendTextMessage(targetNumber, null, "MOTORON", null, null)
            Logger.i("MOTORON SMS sent successfully to $targetNumber", "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Motor ON command sent", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Logger.e("Failed to send MOTORON SMS", e, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to send Motor ON command: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
    
    suspend fun sendMotorOff(): Boolean = withContext(Dispatchers.IO) {
        val targetNumber = getTargetNumber()
        
        if (targetNumber == null) {
            Logger.e("No device configured for SMS", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Please configure a device first", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        Logger.d("Attempting to send MOTOROFF SMS to $targetNumber", "SMS")
        
        if (!hasSmsPermission()) {
            Logger.e("SMS permission not granted", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "SMS permission required", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        try {
            smsManager.sendTextMessage(targetNumber, null, "MOTOROFF", null, null)
            Logger.i("MOTOROFF SMS sent successfully to $targetNumber", "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Motor OFF command sent", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Logger.e("Failed to send MOTOROFF SMS", e, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to send Motor OFF command: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
    
    suspend fun sendStatusRequest(): Boolean = withContext(Dispatchers.IO) {
        val targetNumber = getTargetNumber()
        
        if (targetNumber == null) {
            Logger.e("No device configured for SMS", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Please configure a device first", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        Logger.d("Attempting to send STATUS SMS to $targetNumber", "SMS")
        
        if (!hasSmsPermission()) {
            Logger.e("SMS permission not granted", null, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "SMS permission required", Toast.LENGTH_LONG).show()
            }
            return@withContext false
        }
        
        try {
            smsManager.sendTextMessage(targetNumber, null, "STATUS", null, null)
            Logger.i("STATUS SMS sent successfully to $targetNumber", "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Status request sent", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Logger.e("Failed to send STATUS SMS", e, "SMS")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to send status request: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
}
