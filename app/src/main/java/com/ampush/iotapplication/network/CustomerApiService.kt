package com.ampush.iotapplication.network

import com.ampush.iotapplication.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CustomerApiService {
    
    
    @POST("customer/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<CustomerResponse>
    
    @GET("customer/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
    
    @PUT("customer/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
    
    @Multipart
    @PUT("customer/profile")
    suspend fun updateProfileWithPhoto(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("phone_number") phoneNumber: RequestBody?,
        @Part("address_line_1") addressLine1: RequestBody?,
        @Part("address_line_2") addressLine2: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("state") state: RequestBody?,
        @Part("postal_code") postalCode: RequestBody?,
        @Part("country") country: RequestBody?,
        @Part profilePhoto: MultipartBody.Part?
    ): Response<ProfileResponse>
    
    @POST("customer/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse>
    
    
    @POST("customer/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse>
    
    @DELETE("customer/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body request: DeleteAccountRequest
    ): Response<ApiResponse>
    
    @POST("customer/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<TokenResponse>
    
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse>
}

// Request models


data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone_number: String? = null,
    val address_line_1: String? = null,
    val address_line_2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postal_code: String? = null,
    val country: String? = null
)

data class ChangePasswordRequest(
    val current_password: String,
    val password: String,
    val password_confirmation: String
)

data class DeleteAccountRequest(
    val password: String,
    val confirmation: String
)

