package com.ampush.iotapplication.data.parser

import com.ampush.iotapplication.data.model.MotorData
import java.util.regex.Pattern

class SmsParser {
    
    companion object {
        private val MOTOR_PATTERN = Pattern.compile("Motor[\\s]*:?[\\s]*(ON|OFF)", Pattern.CASE_INSENSITIVE)
        private val VOLTAGE_PATTERN = Pattern.compile("Voltage[\\s]*:?[\\s]*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
        private val CURRENT_PATTERN = Pattern.compile("Current[\\s]*:?[\\s]*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
        private val WATER_LEVEL_PATTERN = Pattern.compile("Water[\\s]*Level[\\s]*:?[\\s]*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
        private val MODE_PATTERN = Pattern.compile("Mode[\\s]*:?[\\s]*(\\w+)", Pattern.CASE_INSENSITIVE)
        private val CLOCK_PATTERN = Pattern.compile("Clock[\\s]*:?[\\s]*(ON|OFF|\\d{1,2}:\\d{2}(?::\\d{2})?(?:\\s*[AP]M)?)", Pattern.CASE_INSENSITIVE)
        private val RUN_TIME_PATTERN = Pattern.compile("Run[\\s]*Time[\\s]*:?[\\s]*(\\d+)[\\s]*(sec|seconds?|s)", Pattern.CASE_INSENSITIVE)
    }
    
    fun parseSms(smsBody: String, phoneNumber: String): MotorData? {
        return try {
            val motorStatus = extractMotorStatus(smsBody)
            val voltage = extractVoltage(smsBody)
            val current = extractCurrent(smsBody)
            val waterLevel = extractWaterLevel(smsBody)
            val mode = extractMode(smsBody)
            val clock = extractClock(smsBody)
            val runTime = extractRunTime(smsBody)
            
            MotorData(
                motorStatus = motorStatus ?: "STATUS",
                voltage = voltage,
                current = current,
                waterLevel = waterLevel,
                mode = mode,
                clock = clock,
                runTime = runTime,
                rawMessage = smsBody,
                phoneNumber = phoneNumber
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractMotorStatus(text: String): String? {
        val matcher = MOTOR_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.uppercase()
        } else null
    }
    
    private fun extractVoltage(text: String): Float? {
        val matcher = VOLTAGE_PATTERN.matcher(text)
        return if (matcher.find()) {
            try {
                matcher.group(1)?.toFloat()
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }
    
    private fun extractCurrent(text: String): Float? {
        val matcher = CURRENT_PATTERN.matcher(text)
        return if (matcher.find()) {
            try {
                matcher.group(1)?.toFloat()
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }
    
    private fun extractWaterLevel(text: String): Float? {
        val matcher = WATER_LEVEL_PATTERN.matcher(text)
        return if (matcher.find()) {
            try {
                matcher.group(1)?.toFloat()
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }
    
    private fun extractMode(text: String): String? {
        val matcher = MODE_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
    
    private fun extractClock(text: String): String? {
        val matcher = CLOCK_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
    
    private fun extractRunTime(text: String): Int? {
        val matcher = RUN_TIME_PATTERN.matcher(text)
        return if (matcher.find()) {
            try {
                matcher.group(1)?.toInt()
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }
}
