package com.ampush.iotapplication.utils

import android.content.Context
import com.ampush.iotapplication.utils.Logger

/**
 * Utility class to check if a phone number is set as default for SMS on the device
 */
class SmsDefaultChecker(private val context: Context) {
    
    private val simDetector = SimPhoneDetector(context)
    
    /**
     * Check if the logged-in phone number is the default SIM for SMS sending
     * 
     * @param loggedInPhoneNumber The phone number that user logged in with
     * @return true if this phone number is set as default for SMS, false otherwise
     */
    fun isLoggedInNumberDefaultForSms(loggedInPhoneNumber: String): Boolean {
        return try {
            Logger.d("Checking if $loggedInPhoneNumber is default SMS SIM", "SMS_DEFAULT_CHECK")
            
            val isDefault = simDetector.isPhoneNumberDefaultForSms(loggedInPhoneNumber)
            val defaultSmsNumber = simDetector.getDefaultSmsSimNumber()
            
            Logger.d("Is default: $isDefault, Default SMS number: $defaultSmsNumber", "SMS_DEFAULT_CHECK")
            
            isDefault
        } catch (e: Exception) {
            Logger.e("Error checking SMS default status", e, "SMS_DEFAULT_CHECK")
            false
        }
    }
    
    /**
     * Get information about SMS default status
     * 
     * @param loggedInPhoneNumber The phone number that user logged in with
     * @return SMSDefaultInfo with details about the default status
     */
    fun getSmsDefaultInfo(loggedInPhoneNumber: String): SMSDefaultInfo {
        return try {
            val isDefault = simDetector.isPhoneNumberDefaultForSms(loggedInPhoneNumber)
            val defaultSmsNumber = simDetector.getDefaultSmsSimNumber()
            val allSimNumbers = simDetector.getAllPhoneNumbers()
            
            SMSDefaultInfo(
                isDefault = isDefault,
                loggedInNumber = loggedInPhoneNumber,
                defaultSmsNumber = defaultSmsNumber,
                allSimNumbers = allSimNumbers,
                message = if (isDefault) {
                    "This number is set as default for SMS sending"
                } else {
                    "This number is NOT set as default for SMS sending. Default is: $defaultSmsNumber"
                }
            )
        } catch (e: Exception) {
            Logger.e("Error getting SMS default info", e, "SMS_DEFAULT_CHECK")
            SMSDefaultInfo(
                isDefault = false,
                loggedInNumber = loggedInPhoneNumber,
                defaultSmsNumber = null,
                allSimNumbers = emptyList(),
                message = "Error checking SMS default status: ${e.message}"
            )
        }
    }
}

/**
 * Data class containing SMS default information
 */
data class SMSDefaultInfo(
    val isDefault: Boolean,
    val loggedInNumber: String,
    val defaultSmsNumber: String?,
    val allSimNumbers: List<String>,
    val message: String
)
