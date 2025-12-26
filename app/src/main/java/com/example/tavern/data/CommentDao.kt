package com.example.tavern.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    // Get comments only for the specific post we are looking at
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY id ASC")
    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>>

    @Insert
    suspend fun insertComment(comment: CommentEntity)
}