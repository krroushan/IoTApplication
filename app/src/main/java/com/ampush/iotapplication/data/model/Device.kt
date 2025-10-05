package com.ampush.iotapplication.data.model

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("device_name")
    val deviceName: String,
    
    @SerializedName("sms_number")
    val smsNumber: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("last_activity_at")
    val lastActivityAt: String?
)
