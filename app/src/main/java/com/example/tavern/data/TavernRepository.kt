package com.example.tavern.data

import kotlinx.coroutines.flow.Flow

// This class combines all DAOs so the ViewModel has one single place to get data
class TavernRepository(private val postDao: PostDao, private val userDao: UserDao, private val commentDao: CommentDao) {

    // --- POST FEATURES ---
    // Get all posts for the feed
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    // Add a new post
    suspend fun addPost(post: PostEntity) {
        postDao.insertPost(post)
    }
    
    // Search posts by query
    fun searchPosts(query: String): Flow<List<PostEntity>> {
        return postDao.searchPosts(query)
    }
    
    // Get posts by specific author
    fun getPostsByAuthor(username: String): Flow<List<PostEntity>> {
        return postDao.getPostsByAuthor(username)
    }

    // --- USER / LOGIN FEATURES ---
    // Check if username/password matches
    suspend fun login(user: String, pass: String): UserEntity? {
        return userDao.login(user, pass)
    }

    // Register a new user (returns true if successful)
    suspend fun register(user: UserEntity): Boolean {
        val result = userDao.register(user)
        return result != -1L // If result is -1, the user already exists
    }
    
    // Get user by username
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }
    
    // Get user by username as Flow
    fun getUserByUsernameFlow(username: String): Flow<UserEntity?> {
        return userDao.getUserByUsernameFlow(username)
    }
    
    // Update user profile
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    // --- COMMENT FEATURES ---
    fun getComments(postId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForPost(postId)
    }

    // Add a comment to the database
    suspend fun addComment(comment: CommentEntity) {
        commentDao.insertComment(comment)
    }
}
