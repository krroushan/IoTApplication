package com.ampush.iotapplication.utils

import android.content.Context
import android.telephony.TelephonyManager
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.PermissionHelper
import com.ampush.iotapplication.utils.PhoneNumberNormalizer
import android.telephony.SubscriptionManager
import android.telephony.SubscriptionInfo

data class SimInfo(
    val phoneNumber: String?,
    val carrierName: String?,
    val countryIso: String?,
    val simSlotIndex: Int,
    val subscriptionId: Int = -1,
    val displayName: String? = null
)

class SimPhoneDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "SIM_DETECTOR"
    }
    
    /**
     * Get phone number from SIM card(s)
     * Returns list of SIM info for devices with multiple SIMs
     */
    fun getSimPhoneNumbers(): List<SimInfo> {
        val simInfoList = mutableListOf<SimInfo>()
        
        try {
            // Check if we have all required permissions
            if (!PermissionHelper.hasSimDetectionPermissions(context)) {
                Logger.w("SIM detection permissions not granted", TAG)
                return emptyList()
            }
            
            // Method 1: Try using SubscriptionManager for multiple SIMs (Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    
                    val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                    if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                        Logger.d("Found ${activeSubscriptions.size} active subscriptions", TAG)
                        
                        for (subscriptionInfo in activeSubscriptions) {
                            try {
                                val rawPhoneNumber = subscriptionInfo.number?.takeIf { it.isNotBlank() }
                                val carrierName = subscriptionInfo.carrierName?.toString()?.takeIf { it.isNotBlank() }
                                val countryIso = subscriptionInfo.countryIso?.takeIf { it.isNotBlank() }
                                val displayName = subscriptionInfo.displayName?.toString()
                                
                                if (rawPhoneNumber != null) {
                                    // Extract only the 10-digit number (remove country codes)
                                    val cleanPhoneNumber = when {
                                        rawPhoneNumber.startsWith("+91") && rawPhoneNumber.length == 13 -> {
                                            rawPhoneNumber.substring(3) // Remove +91
                                        }
                                        rawPhoneNumber.startsWith("91") && rawPhoneNumber.length == 12 -> {
                                            rawPhoneNumber.substring(2) // Remove 91
                                        }
                                        rawPhoneNumber.startsWith("91") && rawPhoneNumber.length == 10 -> {
                                            rawPhoneNumber // Already 10 digits
                                        }
                                        rawPhoneNumber.length == 10 -> {
                                            rawPhoneNumber // Already 10 digits
                                        }
                                        else -> {
                                            rawPhoneNumber // Keep as-is if can't determine
                                        }
                                    }
                                    
                                    simInfoList.add(
                                        SimInfo(
                                            phoneNumber = cleanPhoneNumber,
                                            carrierName = carrierName,
                                            countryIso = countryIso,
                                            simSlotIndex = subscriptionInfo.simSlotIndex,
                                            subscriptionId = subscriptionInfo.subscriptionId,
                                            displayName = displayName
                                        )
                                    )
                                    Logger.i("Detected SIM ${subscriptionInfo.simSlotIndex}: $rawPhoneNumber -> $cleanPhoneNumber (${carrierName ?: "Unknown Carrier"})", TAG)
                                    Logger.d("Raw SIM ${subscriptionInfo.simSlotIndex} data: length=${rawPhoneNumber.length}, startsWith91=${rawPhoneNumber.startsWith("91")}, is12Digits=${rawPhoneNumber.length == 12}", TAG)
                                } else {
                                    Logger.d("SIM ${subscriptionInfo.simSlotIndex} has no phone number", TAG)
                                }
                            } catch (e: Exception) {
                                Logger.e("Error getting info for subscription ${subscriptionInfo.subscriptionId}", e, TAG)
                            }
                        }
                    } else {
                        Logger.d("No active subscriptions found", TAG)
                    }
                } catch (e: Exception) {
                    Logger.e("Error using SubscriptionManager", e, TAG)
                }
            }
            
            // Method 2: Fallback to TelephonyManager if no SIMs detected via SubscriptionManager
            if (simInfoList.isEmpty()) {
                try {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    
                    val rawPhoneNumber = try {
                        telephonyManager.line1Number?.takeIf { it.isNotBlank() }
                    } catch (e: SecurityException) {
                        Logger.e("Security exception getting phone number", e, TAG)
                        null
                    }
                    
                    val carrierName = try {
                        telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        Logger.e("Error getting carrier name", e, TAG)
                        null
                    }
                    
                    val countryIso = try {
                        telephonyManager.networkCountryIso?.takeIf { it.isNotBlank() }
                    } catch (e: Exception) {
                        Logger.e("Error getting country ISO", e, TAG)
                        null
                    }
                    
                    if (rawPhoneNumber != null) {
                        // Extract only the 10-digit number (remove country codes)
                        val cleanPhoneNumber = when {
                            rawPhoneNumber.startsWith("+91") && rawPhoneNumber.length == 13 -> {
                                rawPhoneNumber.substring(3) // Remove +91
                            }
                            rawPhoneNumber.startsWith("91") && rawPhoneNumber.length == 12 -> {
                                rawPhoneNumber.substring(2) // Remove 91
                            }
                            rawPhoneNumber.startsWith("91") && rawPhoneNumber.length == 10 -> {
                                rawPhoneNumber // Already 10 digits
                            }
                            rawPhoneNumber.length == 10 -> {
                                rawPhoneNumber // Already 10 digits
                            }
                            else -> {
                                rawPhoneNumber // Keep as-is if can't determine
                            }
                        }
                        
                        simInfoList.add(
                            SimInfo(
                                phoneNumber = cleanPhoneNumber,
                                carrierName = carrierName,
                                countryIso = countryIso,
                                simSlotIndex = 0
                            )
                        )
                        Logger.i("Fallback: Detected phone number: $rawPhoneNumber -> $cleanPhoneNumber", TAG)
                    }
                } catch (e: Exception) {
                    Logger.e("Error in fallback TelephonyManager method", e, TAG)
                }
            }
            
        } catch (e: Exception) {
            Logger.e("Error detecting SIM phone numbers", e, TAG)
        }
        
        Logger.i("Total SIMs detected: ${simInfoList.size}", TAG)
        return simInfoList
    }
    
    /**
     * Get the primary phone number (first SIM)
     */
    fun getPrimaryPhoneNumber(): String? {
        val simInfoList = getSimPhoneNumbers()
        return simInfoList.firstOrNull()?.phoneNumber
    }
    
    /**
     * Get all detected phone numbers with their details
     */
    fun getAllSimInfo(): List<SimInfo> {
        return getSimPhoneNumbers()
    }
    
    /**
     * Get phone numbers as a simple list
     */
    fun getAllPhoneNumbers(): List<String> {
        return getSimPhoneNumbers().mapNotNull { it.phoneNumber }
    }
    
    /**
     * Check if device has SIM card
     */
    fun hasSimCard(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.simState == TelephonyManager.SIM_STATE_READY
        } catch (e: Exception) {
            Logger.e("Error checking SIM state", e, TAG)
            false
        }
    }
    
    /**
     * Format phone number with country code if missing
     */
    fun formatPhoneNumber(phoneNumber: String, countryCode: String = "+91"): String {
        return when {
            phoneNumber.startsWith("+") -> phoneNumber
            phoneNumber.startsWith("91") -> "+$phoneNumber"
            phoneNumber.startsWith("0") -> countryCode + phoneNumber.substring(1)
            else -> "$countryCode$phoneNumber"
        }
    }
    
    
    /**
     * Get required permissions for SIM detection
     */
    fun getRequiredPermissions(): Array<String> {
        return PermissionHelper.getSimDetectionPermissions()
    }
    
    /**
     * Check if a phone number is the default SIM for SMS sending on the device
     */
    fun isPhoneNumberDefaultForSms(phoneNumber: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                
                if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                    // Get the default SMS subscription ID (using reflection for API compatibility)
                    val defaultSmsSubscriptionId = try {
                        val method = SubscriptionManager::class.java.getMethod("getDefaultSmsSubscriptionId")
                        method.invoke(subscriptionManager) as Int
                    } catch (e: Exception) {
                        -1
                    }
                    
                    // Find the subscription with matching phone number
                    val matchingSubscription = activeSubscriptions.find { subscriptionInfo ->
                        val simPhoneNumber = PhoneNumberNormalizer.normalizePhoneNumber(subscriptionInfo.number ?: "")
                        simPhoneNumber == PhoneNumberNormalizer.normalizePhoneNumber(phoneNumber)
                    }
                    
                    // Check if this subscription is the default for SMS
                    matchingSubscription?.subscriptionId == defaultSmsSubscriptionId
                } else {
                    // Fallback: if only one SIM, it's considered default
                    val allSims = getSimPhoneNumbers()
                    allSims.size == 1 && allSims.firstOrNull()?.phoneNumber == phoneNumber
                }
            } else {
                // For older Android versions, assume first SIM is default
                val allSims = getSimPhoneNumbers()
                allSims.firstOrNull()?.phoneNumber == phoneNumber
            }
        } catch (e: Exception) {
            Logger.e("Error checking default SMS SIM", e, TAG)
            false
        }
    }
    
    /**
     * Get the default SMS SIM phone number
     */
    fun getDefaultSmsSimNumber(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                
                if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                    val defaultSmsSubscriptionId = try {
                        val method = SubscriptionManager::class.java.getMethod("getDefaultSmsSubscriptionId")
                        method.invoke(subscriptionManager) as Int
                    } catch (e: Exception) {
                        -1
                    }
                    val defaultSubscription = activeSubscriptions.find { it.subscriptionId == defaultSmsSubscriptionId }
                    defaultSubscription?.number?.let { PhoneNumberNormalizer.normalizePhoneNumber(it) }
                } else {
                    // Fallback to first SIM
                    getSimPhoneNumbers().firstOrNull()?.phoneNumber
                }
            } else {
                // For older Android versions
                getSimPhoneNumbers().firstOrNull()?.phoneNumber
            }
        } catch (e: Exception) {
            Logger.e("Error getting default SMS SIM", e, TAG)
            getSimPhoneNumbers().firstOrNull()?.phoneNumber
        }
    }
}
