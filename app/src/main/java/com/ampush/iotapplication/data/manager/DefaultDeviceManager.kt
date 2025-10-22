package com.ampush.iotapplication.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.ampush.iotapplication.data.model.Device
import com.ampush.iotapplication.utils.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DefaultDeviceManager(private val context: Context) {
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("default_device_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DEFAULT_DEVICE = "default_device"
    }
    
    /**
     * Set default device
     */
    fun setDefaultDevice(device: Device?) {
        if (device != null) {
            val deviceJson = Gson().toJson(device)
            sharedPrefs.edit()
                .putString(KEY_DEFAULT_DEVICE, deviceJson)
                .apply()
            Logger.d("Default device set: ${device.deviceName}", "DEFAULT_DEVICE")
        } else {
            clearDefaultDevice()
            Logger.d("Default device cleared", "DEFAULT_DEVICE")
        }
    }
    
    /**
     * Get default device
     */
    fun getDefaultDevice(): Device? {
        val deviceJson = sharedPrefs.getString(KEY_DEFAULT_DEVICE, null)
        return if (deviceJson != null) {
            try {
                Gson().fromJson(deviceJson, Device::class.java)
            } catch (e: Exception) {
                Logger.e("Error parsing default device", e, "DEFAULT_DEVICE")
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Check if default device is set
     */
    fun hasDefaultDevice(): Boolean {
        return getDefaultDevice() != null
    }
    
    /**
     * Clear default device
     */
    fun clearDefaultDevice() {
        sharedPrefs.edit()
            .remove(KEY_DEFAULT_DEVICE)
            .apply()
        Logger.d("Default device cleared", "DEFAULT_DEVICE")
    }
    
    /**
     * Check if device is the default device
     */
    fun isDefaultDevice(device: Device): Boolean {
        val defaultDevice = getDefaultDevice()
        return defaultDevice?.deviceName == device.deviceName && 
               defaultDevice?.smsNumber == device.smsNumber
    }
}
