package com.ampush.iotapplication.data.manager

import android.content.Context
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.widget.Toast
import com.ampush.iotapplication.data.model.Device
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DeviceManager(private val context: Context) {
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_DEVICES = "saved_devices"
        private const val KEY_LAST_SELECTED_DEVICE = "last_selected_device"
    }
    
    /**
     * Save devices list to SharedPreferences
     */
    fun saveDevices(devices: List<Device>) {
        val devicesJson = gson.toJson(devices)
        sharedPrefs.edit()
            .putString(KEY_DEVICES, devicesJson)
            .apply()
    }
    
    /**
     * Get saved devices from SharedPreferences
     */
    fun getSavedDevices(): List<Device> {
        val devicesJson = sharedPrefs.getString(KEY_DEVICES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Device>>() {}.type
            gson.fromJson(devicesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Save last selected device for quick access
     */
    fun saveLastSelectedDevice(device: Device) {
        val deviceJson = gson.toJson(device)
        sharedPrefs.edit()
            .putString(KEY_LAST_SELECTED_DEVICE, deviceJson)
            .apply()
    }
    
    /**
     * Get last selected device
     */
    fun getLastSelectedDevice(): Device? {
        val deviceJson = sharedPrefs.getString(KEY_LAST_SELECTED_DEVICE, null) ?: return null
        return try {
            gson.fromJson(deviceJson, Device::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Send SMS command to selected device
     */
    fun sendSmsCommand(device: Device, command: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                device.smsNumber,
                null,
                command,
                null,
                null
            )
            
            // Save as last selected device
            saveLastSelectedDevice(device)
            
            // Show success message
            Toast.makeText(
                context,
                "Command sent to ${device.deviceName}",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to send command: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Check if user has any devices
     */
    fun hasDevices(): Boolean {
        return getSavedDevices().isNotEmpty()
    }
    
    /**
     * Get device by ID
     */
    fun getDeviceById(deviceId: Int): Device? {
        return getSavedDevices().find { it.id == deviceId }
    }
    
    /**
     * Clear all saved devices (for logout)
     */
    fun clearDevices() {
        sharedPrefs.edit()
            .remove(KEY_DEVICES)
            .remove(KEY_LAST_SELECTED_DEVICE)
            .apply()
    }
    
    /**
     * Refresh devices from API
     */
    suspend fun refreshDevices(token: String): Boolean {
        return try {
            val customerRepository = com.ampush.iotapplication.repository.CustomerRepository()
            val result = customerRepository.getMyDevices(token)
            
            result.fold(
                onSuccess = { response ->
                    saveDevices(response.data)
                    com.ampush.iotapplication.utils.Logger.i("Devices refreshed successfully: ${response.data.size} devices", "DEVICE_MANAGER")
                    true
                },
                onFailure = { exception ->
                    com.ampush.iotapplication.utils.Logger.e("Failed to refresh devices", exception, "DEVICE_MANAGER")
                    
                    // Check if it's a JSON parsing error (API returning HTML)
                    if (exception.message?.contains("MalformedJsonException") == true) {
                        com.ampush.iotapplication.utils.Logger.w("API endpoint returning HTML instead of JSON - backend API not implemented", "DEVICE_MANAGER")
                        com.ampush.iotapplication.utils.Logger.w("Devices refresh will be skipped until backend API is ready", "DEVICE_MANAGER")
                    }
                    false
                }
            )
        } catch (e: Exception) {
            com.ampush.iotapplication.utils.Logger.e("Error refreshing devices", e, "DEVICE_MANAGER")
            
            // Check if it's a JSON parsing error (API returning HTML)
            if (e.message?.contains("MalformedJsonException") == true) {
                com.ampush.iotapplication.utils.Logger.w("API endpoint returning HTML instead of JSON - backend API not implemented", "DEVICE_MANAGER")
                com.ampush.iotapplication.utils.Logger.w("Devices refresh will be skipped until backend API is ready", "DEVICE_MANAGER")
            }
            false
        }
    }
}
