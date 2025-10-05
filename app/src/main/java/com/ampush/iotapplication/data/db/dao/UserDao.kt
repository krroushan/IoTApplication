package com.ampush.iotapplication.data.db.dao

import androidx.room.*
import com.ampush.iotapplication.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password AND isActive = 1 LIMIT 1")
    suspend fun loginUser(phone: String, password: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?
    
    @Insert
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET lastLoginAt = :loginTime WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, loginTime: Date)
    
    @Query("UPDATE users SET password = :newPassword WHERE phone = :phone")
    suspend fun updatePassword(phone: String, newPassword: String)
    
    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    suspend fun isPhoneRegistered(phone: String): Int
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    
    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: Long)
}
