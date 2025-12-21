package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String, // Username is unique ID
    val password: String, // Simple storage for this project
    val bio: String = "A wandering traveler...", // User biography
    val avatarUrl: String = "", // Profile picture URL or identifier
    val joinedDate: Long = System.currentTimeMillis() // Timestamp when user joined (cake day)
)
