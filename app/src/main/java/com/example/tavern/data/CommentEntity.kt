package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postId: Int,      // Links this comment to a specific post
    val author: String,
    val content: String
)