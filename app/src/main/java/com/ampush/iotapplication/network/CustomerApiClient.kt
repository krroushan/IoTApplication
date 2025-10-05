package com.ampush.iotapplication.network

import com.ampush.iotapplication.data.model.*
import com.ampush.iotapplication.utils.Logger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object CustomerApiClient {
    
    private const val BASE_URL = "https://laravel1.wizzyweb.com/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Logger.d("API: $message", "NETWORK")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: CustomerApiService = retrofit.create(CustomerApiService::class.java)
    
    /**
     * Create RequestBody from string
     */
    fun createRequestBody(text: String): okhttp3.RequestBody {
        return text.toRequestBody("text/plain".toMediaType())
    }
    
    /**
     * Create MultipartBody.Part from file
     */
    fun createMultipartBodyPart(
        key: String,
        file: File,
        mediaType: String = "image/*"
    ): MultipartBody.Part {
        val requestBody = file.asRequestBody(mediaType.toMediaType())
        return MultipartBody.Part.createFormData(key, file.name, requestBody)
    }
    
    /**
     * Create MultipartBody.Part from file path
     */
    fun createMultipartBodyPartFromPath(
        key: String,
        filePath: String,
        mediaType: String = "image/*"
    ): MultipartBody.Part? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                createMultipartBodyPart(key, file, mediaType)
            } else {
                Logger.w("File not found: $filePath", "API")
                null
            }
        } catch (e: Exception) {
            Logger.e("Error creating multipart body part", e, "API")
            null
        }
    }
}
