package com.ampush.iotapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.ampush.iotapplication.data.model.Customer

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LOGIN_TIME = "login_time"
        
        // New API-related keys
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_CUSTOMER_ID = "customer_id"
        private const val KEY_CUSTOMER_EMAIL = "customer_email"
        private const val KEY_CUSTOMER_PHONE = "customer_phone"
        private const val KEY_CUSTOMER_ADDRESS = "customer_address"
        private const val KEY_CUSTOMER_CITY = "customer_city"
        private const val KEY_CUSTOMER_STATE = "customer_state"
        private const val KEY_CUSTOMER_COUNTRY = "customer_country"
        private const val KEY_CUSTOMER_POSTAL_CODE = "customer_postal_code"
        private const val KEY_CUSTOMER_PROFILE_PHOTO = "customer_profile_photo"
    }
    
    /**
     * Save login session (legacy method for dummy auth)
     */
    fun saveLoginSession(phone: String, name: String = "Demo User") {
        Logger.d("Saving login session for phone: $phone", "SESSION")
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_NAME, name)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Save API authentication session
     */
    fun saveApiSession(customer: Customer, token: String, tokenType: String = "Bearer") {
        Logger.d("Saving API session for customer: ${customer.email}", "SESSION")
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .putLong(KEY_CUSTOMER_ID, customer.id)
            .putString(KEY_CUSTOMER_EMAIL, customer.email)
            .putString(KEY_CUSTOMER_PHONE, customer.phoneNumber)
            .putString(KEY_USER_NAME, customer.name)
            .putString(KEY_CUSTOMER_ADDRESS, customer.addressLine1)
            .putString(KEY_CUSTOMER_CITY, customer.city)
            .putString(KEY_CUSTOMER_STATE, customer.state)
            .putString(KEY_CUSTOMER_COUNTRY, customer.country)
            .putString(KEY_CUSTOMER_POSTAL_CODE, customer.postalCode)
            .putString(KEY_CUSTOMER_PROFILE_PHOTO, customer.profilePhotoUrl)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Update customer data in session
     */
    fun updateCustomerData(customer: Customer) {
        Logger.d("Updating customer data in session", "SESSION")
        prefs.edit()
            .putLong(KEY_CUSTOMER_ID, customer.id)
            .putString(KEY_CUSTOMER_EMAIL, customer.email)
            .putString(KEY_CUSTOMER_PHONE, customer.phoneNumber)
            .putString(KEY_USER_NAME, customer.name)
            .putString(KEY_CUSTOMER_ADDRESS, customer.addressLine1)
            .putString(KEY_CUSTOMER_CITY, customer.city)
            .putString(KEY_CUSTOMER_STATE, customer.state)
            .putString(KEY_CUSTOMER_COUNTRY, customer.country)
            .putString(KEY_CUSTOMER_POSTAL_CODE, customer.postalCode)
            .putString(KEY_CUSTOMER_PROFILE_PHOTO, customer.profilePhotoUrl)
            .apply()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Check if user has API token (authenticated via API)
     */
    fun hasApiToken(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Get token type
     */
    fun getTokenType(): String {
        return prefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
    }
    
    /**
     * Get full authorization header
     */
    fun getAuthorizationHeader(): String? {
        val token = getAccessToken()
        val tokenType = getTokenType()
        return if (token != null) "$tokenType $token" else null
    }
    
    /**
     * Get customer ID
     */
    fun getCustomerId(): Long {
        return prefs.getLong(KEY_CUSTOMER_ID, 0)
    }
    
    /**
     * Get customer email
     */
    fun getCustomerEmail(): String? {
        return prefs.getString(KEY_CUSTOMER_EMAIL, null)
    }
    
    /**
     * Get logged in user's phone (legacy + API)
     */
    fun getUserPhone(): String? {
        return prefs.getString(KEY_CUSTOMER_PHONE, null) ?: prefs.getString(KEY_USER_PHONE, null)
    }
    
    /**
     * Get logged in user's name (legacy + API)
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Get customer address
     */
    fun getCustomerAddress(): String? {
        return prefs.getString(KEY_CUSTOMER_ADDRESS, null)
    }
    
    /**
     * Get customer city
     */
    fun getCustomerCity(): String? {
        return prefs.getString(KEY_CUSTOMER_CITY, null)
    }
    
    /**
     * Get customer state
     */
    fun getCustomerState(): String? {
        return prefs.getString(KEY_CUSTOMER_STATE, null)
    }
    
    /**
     * Get customer country
     */
    fun getCustomerCountry(): String? {
        return prefs.getString(KEY_CUSTOMER_COUNTRY, null)
    }
    
    /**
     * Get customer postal code
     */
    fun getCustomerPostalCode(): String? {
        return prefs.getString(KEY_CUSTOMER_POSTAL_CODE, null)
    }
    
    /**
     * Get customer profile photo URL
     */
    fun getCustomerProfilePhoto(): String? {
        return prefs.getString(KEY_CUSTOMER_PROFILE_PHOTO, null)
    }
    
    /**
     * Get login time
     */
    fun getLoginTime(): Long {
        return prefs.getLong(KEY_LOGIN_TIME, 0)
    }
    
    /**
     * Logout user
     */
    fun logout() {
        Logger.d("Logging out user", "SESSION")
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_PHONE)
            .remove(KEY_USER_NAME)
            .remove(KEY_LOGIN_TIME)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_TOKEN_TYPE)
            .remove(KEY_CUSTOMER_ID)
            .remove(KEY_CUSTOMER_EMAIL)
            .remove(KEY_CUSTOMER_PHONE)
            .remove(KEY_CUSTOMER_ADDRESS)
            .remove(KEY_CUSTOMER_CITY)
            .remove(KEY_CUSTOMER_STATE)
            .remove(KEY_CUSTOMER_COUNTRY)
            .remove(KEY_CUSTOMER_POSTAL_CODE)
            .remove(KEY_CUSTOMER_PROFILE_PHOTO)
            .apply()
    }
    
    /**
     * Clear all session data
     */
    fun clearSession() {
        Logger.d("Clearing all session data", "SESSION")
        prefs.edit().clear().apply()
    }
}
