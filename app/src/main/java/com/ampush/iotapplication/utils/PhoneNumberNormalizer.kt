package com.ampush.iotapplication.utils

/**
 * Utility class for phone number normalization
 * Handles common issues like duplicate country codes
 */
object PhoneNumberNormalizer {
    
    /**
     * Normalize phone number by removing duplicate country codes
     * Examples:
     * - "919942695978" -> "+919942695978" (removes duplicate 91)
     * - "9102318033" -> "+919102318033" (keeps single 91)
     * - "+919942695978" -> "+919942695978" (already normalized)
     */
    fun normalizePhoneNumber(phoneNumber: String): String {
        if (phoneNumber.isBlank()) return phoneNumber
        
        var normalized = phoneNumber.trim()
        
        // Remove leading + if present
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1)
        }
        
        // Handle duplicate country codes (e.g., 919942695978 -> 9942695978)
        if (normalized.startsWith("91") && normalized.length > 10) {
            // Check if it's a duplicate 91 (Indian country code)
            val withoutFirst91 = normalized.substring(2)
            if (withoutFirst91.length == 10 && withoutFirst91.matches(Regex("^[6-9]\\d{9}$"))) {
                // This looks like a valid 10-digit Indian mobile number after removing 91
                normalized = withoutFirst91
                Logger.d("Removed duplicate country code: $phoneNumber -> $normalized", "PHONE_NORMALIZER")
            }
        }
        
        // Add +91 prefix for consistency
        return if (normalized.startsWith("91")) {
            "+$normalized"
        } else {
            "+91$normalized"
        }
    }
    
    /**
     * Test the normalization with common examples
     */
    fun testNormalization() {
        val testCases = listOf(
            "919942695978",  // Duplicate 91
            "9102318033",    // Single 91
            "+919942695978", // Already with +
            "9942695978",    // No country code
            "919876543210"   // Another duplicate case
        )
        
        Logger.i("Testing phone number normalization:", "PHONE_NORMALIZER")
        testCases.forEach { input ->
            val normalized = normalizePhoneNumber(input)
            Logger.i("$input -> $normalized", "PHONE_NORMALIZER")
        }
    }
}
