package com.ampush.iotapplication.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("phone_number")
    val phone_number: String,
    
    @SerializedName("password")
    val password: String
)
