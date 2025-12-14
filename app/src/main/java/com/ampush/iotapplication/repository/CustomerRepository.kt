package com.ampush.iotapplication.repository

import com.ampush.iotapplication.data.model.*
import com.ampush.iotapplication.network.CustomerApiClient
import com.ampush.iotapplication.network.LoginRequest
import com.ampush.iotapplication.network.UpdateProfileRequest
import com.ampush.iotapplication.network.ChangePasswordRequest
import com.ampush.iotapplication.network.DeleteAccountRequest
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CustomerRepository {
    
    private val apiService = CustomerApiClient.apiService
    
    
    /**
     * Login customer with phone number
     */
    suspend fun login(phoneNumber: String, password: String): Result<CustomerResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Logging in customer with phone: $phoneNumber", "CUSTOMER_API")
            
            val request = LoginRequest(phone_number = phoneNumber, password = password)
            val response = apiService.login(request)
            
            if (response.isSuccessful) {
                val customerResponse = response.body()
                if (customerResponse != null) {
                    Logger.i("Customer logged in successfully: ${customerResponse.data.customer.phoneNumber}", "CUSTOMER_API")
                    Result.success(customerResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Login failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Login error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get customer profile
     */
    suspend fun getProfile(token: String): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting customer profile", "CUSTOMER_API")
            
            val response = apiService.getProfile("Bearer $token")
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse != null) {
                    Logger.i("Profile retrieved successfully", "CUSTOMER_API")
                    Result.success(profileResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Get profile failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Get profile error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Update customer profile
     */
    suspend fun updateProfile(
        token: String,
        name: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        addressLine1: String? = null,
        addressLine2: String? = null,
        city: String? = null,
        state: String? = null,
        postalCode: String? = null,
        country: String? = null,
        profilePhotoPath: String? = null
    ): Result<ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating customer profile", "CUSTOMER_API")
            
            val response = if (profilePhotoPath != null) {
                // Update with profile photo
                val profilePhoto = CustomerApiClient.createMultipartBodyPartFromPath(
                    "profile_photo", profilePhotoPath
                )
                
                apiService.updateProfileWithPhoto(
                    token = "Bearer $token",
                    name = name?.let { CustomerApiClient.createRequestBody(it) },
                    email = email?.let { CustomerApiClient.createRequestBody(it) },
                    phoneNumber = phoneNumber?.let { CustomerApiClient.createRequestBody(it) },
                    addressLine1 = addressLine1?.let { CustomerApiClient.createRequestBody(it) },
                    addressLine2 = addressLine2?.let { CustomerApiClient.createRequestBody(it) },
                    city = city?.let { CustomerApiClient.createRequestBody(it) },
                    state = state?.let { CustomerApiClient.createRequestBody(it) },
                    postalCode = postalCode?.let { CustomerApiClient.createRequestBody(it) },
                    country = country?.let { CustomerApiClient.createRequestBody(it) },
                    profilePhoto = profilePhoto
                )
            } else {
                // Update without profile photo
                val request = UpdateProfileRequest(
                    name = name,
                    email = email,
                    phone_number = phoneNumber,
                    address_line_1 = addressLine1,
                    address_line_2 = addressLine2,
                    city = city,
                    state = state,
                    postal_code = postalCode,
                    country = country
                )
                apiService.updateProfile("Bearer $token", request)
            }
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse != null) {
                    Logger.i("Profile updated successfully", "CUSTOMER_API")
                    Result.success(profileResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Update profile failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Update profile error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Change password
     */
    suspend fun changePassword(
        token: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Changing password", "CUSTOMER_API")
            
            val request = ChangePasswordRequest(
                current_password = currentPassword,
                password = newPassword,
                password_confirmation = confirmPassword
            )
            val response = apiService.changePassword("Bearer $token", request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Logger.i("Password changed successfully", "CUSTOMER_API")
                    Result.success(apiResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Change password failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Change password error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    
    /**
     * Logout
     */
    suspend fun logout(token: String): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Logging out customer", "CUSTOMER_API")
            
            val response = apiService.logout("Bearer $token")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Logger.i("Customer logged out successfully", "CUSTOMER_API")
                    Result.success(apiResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Logout failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Logout error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Delete account
     */
    suspend fun deleteAccount(token: String, password: String): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Deleting customer account", "CUSTOMER_API")
            
            val request = DeleteAccountRequest(
                password = password,
                confirmation = "DELETE MY ACCOUNT"
            )
            val response = apiService.deleteAccount("Bearer $token", request)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Logger.i("Account deleted successfully", "CUSTOMER_API")
                    Result.success(apiResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Delete account failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                
                // Try to parse error message from response
                val errorBody = response.errorBody()?.string()
                Logger.e("Error body: $errorBody", null, "CUSTOMER_API")
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Delete account error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Refresh token
     */
    suspend fun refreshToken(token: String): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Refreshing token", "CUSTOMER_API")
            
            val response = apiService.refreshToken("Bearer $token")
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    Logger.i("Token refreshed successfully", "CUSTOMER_API")
                    Result.success(tokenResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Refresh token failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Refresh token error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Get my devices
     */
    suspend fun getMyDevices(token: String): Result<DevicesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting my devices", "CUSTOMER_API")
            
            val response = RetrofitClient.apiService.getMyDevices("Bearer $token")
            
            if (response.isSuccessful) {
                val devicesResponse = response.body()
                if (devicesResponse != null) {
                    Logger.i("Devices retrieved successfully: ${devicesResponse.data.size} devices", "CUSTOMER_API")
                    Result.success(devicesResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Get devices failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Get devices error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Update FCM token
     */
    suspend fun updateFcmToken(token: String, fcmToken: String): Result<FcmTokenResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating FCM token", "CUSTOMER_API")
            
            val request = FcmTokenRequest(fcm_token = fcmToken)
            val response = RetrofitClient.apiService.updateFcmToken("Bearer $token", request)
            
            if (response.isSuccessful) {
                val fcmResponse = response.body()
                if (fcmResponse != null) {
                    Logger.i("FCM token updated successfully", "CUSTOMER_API")
                    Result.success(fcmResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Update FCM token failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Update FCM token error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
    
    /**
     * Health check
     */
    suspend fun healthCheck(): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Checking API health", "CUSTOMER_API")
            
            val response = apiService.healthCheck()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Logger.i("API health check successful", "CUSTOMER_API")
                    Result.success(apiResponse)
                } else {
                    Logger.e("Empty response body", null, "CUSTOMER_API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "Health check failed: ${response.code()} - ${response.message()}"
                Logger.e(errorMessage, null, "CUSTOMER_API")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.e("Health check error", e, "CUSTOMER_API")
            Result.failure(e)
        }
    }
}
