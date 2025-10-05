package com.ampush.iotapplication.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val password: String, // In production, this should be hashed
    val email: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val lastLoginAt: Date? = null,
    val profileImageUrl: String? = null
)
