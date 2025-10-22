package com.ampush.iotapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.ampush.iotapplication.data.parser.SmsParser
import com.ampush.iotapplication.notifications.NotificationHelper
import com.ampush.iotapplication.network.WebhookService
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.data.manager.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    private val parser = SmsParser()
    private val webhookService = WebhookService()
    
    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("SMS Receiver triggered", "SMS")
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            Logger.d("Processing ${messages.size} SMS messages", "SMS")
            
            // Get customer's saved devices
            val deviceManager = DeviceManager(context)
            val savedDevices = deviceManager.getSavedDevices()
            val deviceNumbers = savedDevices.map { it.smsNumber }
            
            Logger.d("Customer has ${savedDevices.size} devices: $deviceNumbers", "SMS")
            
            for (message in messages) {
                val phoneNumber = message.originatingAddress
                val messageBody = message.messageBody
                
                Logger.logSmsReceived(phoneNumber ?: "Unknown", messageBody ?: "Empty")
                Log.d("SmsReceiver", "Received SMS from: $phoneNumber, Body: $messageBody")
                
                // Process SMS from any of customer's devices
                if (phoneNumber != null && deviceNumbers.contains(phoneNumber)) {
                    Logger.d("SMS from customer's device ($phoneNumber) - processing", "SMS")
                    processSms(context, messageBody, phoneNumber)
                } else {
                    Logger.d("SMS from non-device number ($phoneNumber) - ignoring", "SMS")
                }
            }
        }
    }
    
    private fun processSms(context: Context, messageBody: String, phoneNumber: String) {
        Logger.d("Processing SMS: $messageBody", "SMS")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val motorData = parser.parseSms(messageBody, phoneNumber)
                
                if (motorData != null) {
                    Logger.d("SMS parsed successfully: ${motorData.motorStatus}", "SMS")
                    
                    // Send to webhook immediately
                    launch {
                        webhookService.sendMotorCommand("SMS_RECEIVED", motorData.motorStatus, phoneNumber)
                    }
                    
                    // Store in database via repository
                    val repository = com.ampush.iotapplication.data.repository.MotorRepository(context)
                    repository.saveMotorData(motorData, "STATUS")
                    
                    Log.d("SmsReceiver", "Parsed and saved motor data: $motorData")
                    
                    // Show notification
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showMotorStatusNotification(motorData)
                } else {
                    Log.w("SmsReceiver", "Failed to parse SMS: $messageBody")
                    
                    // Show basic SMS notification for unparseable messages
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showSmsReceivedNotification(phoneNumber, messageBody)
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS", e)
            }
        }
    }
}
