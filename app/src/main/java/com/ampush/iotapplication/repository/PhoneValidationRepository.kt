package com.ampush.iotapplication.repository

import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.models.PhoneValidationResponse
import com.ampush.iotapplication.utils.Logger

class PhoneValidationRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun validatePhoneNumber(phoneNumber: String): Result<PhoneValidationResponse> {
        return try {
            Logger.d("Validating phone number: $phoneNumber", "PHONE_VALIDATION")
            
            val response = apiService.validatePhoneNumber(phoneNumber)
            
            when {
                // Success case: Phone is registered (200 OK)
                response.isSuccessful && response.body() != null -> {
                    val validationResult = response.body()!!
                    Logger.i("Phone validation result: isRegistered=${validationResult.isRegistered}, message=${validationResult.message}", "PHONE_VALIDATION")
                    Result.success(validationResult)
                }
                
                // Phone not registered (404) - This is a valid response, not an error
                response.code() == 404 -> {
                    val errorBody = response.errorBody()?.string()
                    Logger.d("Phone not registered (404): $errorBody", "PHONE_VALIDATION")
                    
                    // Parse the error response to get the message
                    val notRegisteredResponse = PhoneValidationResponse(
                        success = false,
                        message = "This number is not registered with our motor control system. Please choose a registered number.",
                        isRegistered = false
                    )
                    Result.success(notRegisteredResponse)
                }
                
                // Missing phone parameter (400)
                response.code() == 400 -> {
                    val errorBody = response.errorBody()?.string()
                    Logger.e("Missing phone parameter (400): $errorBody", null, "PHONE_VALIDATION")
                    Result.failure(Exception("Phone number is required"))
                }
                
                // Other errors
                else -> {
                    val errorBody = response.errorBody()?.string()
                    Logger.e("Phone validation failed: ${response.code()} - $errorBody", null, "PHONE_VALIDATION")
                    Result.failure(Exception("Phone validation failed: ${response.message()} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Logger.e("Exception during phone validation", e, "PHONE_VALIDATION")
            Result.failure(e)
        }
    }
}
