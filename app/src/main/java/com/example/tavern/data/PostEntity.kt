package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val title: String,
    val content: String,
    val upvotes: Int = 0,
    val timestamp: Long = System.currentTimeMillis() // Waktu posting dalam milliseconds
)
