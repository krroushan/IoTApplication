package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

data class PhoneValidationRequest(
    @SerializedName("phone")
    val phone: String
)

data class PhoneValidationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("isRegistered")
    val isRegistered: Boolean
)
