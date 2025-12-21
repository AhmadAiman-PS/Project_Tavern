package com.example.tavern.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    // Returns a Flow so Compose updates automatically when data changes
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<PostEntity>>
    
    // Search posts by title or content
    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>
    
    // Get posts by specific author (for user profile)
    @Query("SELECT * FROM posts WHERE author = :username ORDER BY id DESC")
    fun getPostsByAuthor(username: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
}
