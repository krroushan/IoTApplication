package com.ampush.iotapplication.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.ampush.iotapplication.data.db.dao.LogDao
import com.ampush.iotapplication.data.db.dao.UserDao
import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.data.db.entities.UserEntity

@Database(
    entities = [LogEntity::class, UserEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun logDao(): LogDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motor_control_database"
                ).fallbackToDestructiveMigration() // For development - recreates DB
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
