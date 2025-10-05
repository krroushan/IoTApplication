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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class SmsSender(private val context: Context) {
    
    private val smsManager = SmsManager.getDefault()
    private val targetNumber = "+915754027372041"
    
    private fun hasSmsPermission(): Boolean {
        return PermissionManager.hasSendSmsPermission(context)
    }
    
    
    suspend fun sendMotorOn(): Boolean = withContext(Dispatchers.IO) {
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
