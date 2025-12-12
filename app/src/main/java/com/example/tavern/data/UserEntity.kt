package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String, // Username is unique ID
    val password: String // Simple storage for this project
)