package com.example.tavern.data

import kotlinx.coroutines.flow.Flow

// This class combines both DAOs so the ViewModel has one single place to get data
class TavernRepository(private val postDao: PostDao, private val userDao: UserDao, private val commentDao: CommentDao) {

    // --- POST FEATURES ---
    // Get all posts for the feed
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    // Add a new post
    suspend fun addPost(post: PostEntity) {
        postDao.insertPost(post)
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

    fun getComments(postId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForPost(postId)
    }

    // Add a comment to the database
    suspend fun addComment(comment: CommentEntity) {
        commentDao.insertComment(comment)
    }
}