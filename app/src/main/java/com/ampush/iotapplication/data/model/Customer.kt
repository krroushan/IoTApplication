package com.ampush.iotapplication.data.model

import com.google.gson.annotations.SerializedName

data class Customer(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("address_line_1")
    val addressLine1: String,
    
    @SerializedName("address_line_2")
    val addressLine2: String?,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("postal_code")
    val postalCode: String,
    
    @SerializedName("country")
    val country: String,
    
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

data class CustomerResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: CustomerData
)

data class CustomerData(
    @SerializedName("customer")
    val customer: Customer,
    
    @SerializedName("devices")
    val devices: List<Device>,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("token_type")
    val tokenType: String
)


data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: ProfileData
)

data class ProfileData(
    @SerializedName("customer")
    val customer: Customer,
    
    @SerializedName("devices")
    val devices: List<Device>
)

data class ApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String
)

data class TokenResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: TokenData
)

data class TokenData(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("token_type")
    val tokenType: String
)

data class DevicesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<Device>
)

data class FcmTokenRequest(
    @SerializedName("fcm_token")
    val fcm_token: String
)

data class FcmTokenResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: FcmTokenData
)

data class FcmTokenData(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("fcm_token")
    val fcmToken: String
)
