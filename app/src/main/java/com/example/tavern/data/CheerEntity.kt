package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CHEER ENTITY
 * Merepresentasikan "like" atau "cheers" yang diberikan user ke postingan
 * 
 * Relasi:
 * - username: User yang memberikan cheer (FK ke UserEntity.username)
 * - postId: Postingan yang di-cheer (FK ke PostEntity.id)
 * 
 * Digunakan di:
 * - CheerDao.kt untuk query database
 * - TavernRepository.kt untuk business logic
 * - TavernViewModel.kt untuk state management
 * - TavernApp.kt untuk UI display
 */
@Entity(
    tableName = "cheers",
    primaryKeys = ["username", "postId"] // Composite key: satu user hanya bisa cheer sekali per post
)
data class CheerEntity(
    val username: String,    // User yang memberikan cheer
    val postId: Int,         // Post yang di-cheer
    val timestamp: Long = System.currentTimeMillis() // Kapan cheer diberikan
)
