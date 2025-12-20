package com.example.tavern.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tavern.data.CommentEntity
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernRepository
import com.example.tavern.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * TAVERN VIEW MODEL
 * Manages UI state and business logic for the Tavern app
 * Enhanced with loading states and better error handling
 */
class TavernViewModel(private val repository: TavernRepository) : ViewModel() {

    // ===== POSTS STATE =====
    /**
     * Flow of all posts from the database
     * Automatically updates when posts change
     */
    val uiState: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== USER STATE =====
    /**
     * Current logged-in user
     * Null means user is logged out
     */
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    /**
     * Login/Registration error messages
     * Null means no error
     */
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    /**
     * Loading state for async operations
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ===== POST DETAIL STATE =====
    /**
     * Currently selected post for detail view
     * Null means no post is selected (showing feed)
     */
    private val _selectedPost = MutableStateFlow<PostEntity?>(null)
    val selectedPost = _selectedPost.asStateFlow()

    /**
     * Comments for the currently selected post
     * Empty list when no post is selected
     */
    private val _currentComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val currentComments = _currentComments.asStateFlow()

    // ===== AUTHENTICATION FUNCTIONS =====

    /**
     * Login user with username and password
     * 
     * @param user Username to login with
     * @param pass Password to verify
     * 
     * Sets currentUser if successful, or loginError if failed
     */
    fun login(user: String, pass: String) {
        // Clear previous errors
        _loginError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Simulate network delay for better UX with animations
                delay(300)
                
                val validUser = repository.login(user, pass)
                
                if (validUser != null) {
                    _currentUser.value = validUser
                    _loginError.value = null
                } else {
                    _loginError.value = "Invalid Name or Password, traveler."
                }
            } catch (e: Exception) {
                _loginError.value = "An error occurred. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Register new user with username and password
     * 
     * @param user Username for new account
     * @param pass Password for new account
     * 
     * Auto-logins user if registration successful
     */
    fun register(user: String, pass: String) {
        // Clear previous errors
        _loginError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Simulate network delay for better UX
                delay(300)
                
                val success = repository.register(UserEntity(user, pass))
                
                if (success) {
                    // Auto-login after successful registration
                    login(user, pass)
                } else {
                    _loginError.value = "That name is already taken! Choose another legend."
                }
            } catch (e: Exception) {
                _loginError.value = "Registration failed. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Logout current user
     * Clears user state and returns to login screen
     */
    fun logout() {
        _currentUser.value = null
        selectPost(null) // Close any selected post
        _loginError.value = null // Clear any errors
    }

    /**
     * Clear login/registration error
     * Call this when user starts typing to clear error message
     */
    fun clearError() {
        _loginError.value = null
    }

    // ===== POST FUNCTIONS =====

    /**
     * Create new post
     * 
     * @param title Post title
     * @param content Post content/body
     * 
     * Uses current user as author
     */
    fun createPost(title: String, content: String) {
        val authorName = _currentUser.value?.username ?: "Anonymous"
        
        viewModelScope.launch {
            try {
                // Add slight delay for animation smoothness
                delay(100)
                
                repository.addPost(
                    PostEntity(
                        author = authorName,
                        title = title,
                        content = content,
                        upvotes = (0..100).random() // Random initial upvotes for demo
                    )
                )
            } catch (e: Exception) {
                // In production, show error to user
                // For now, silently fail
            }
        }
    }

    /**
     * Select post to view details and comments
     * 
     * @param post Post to select, or null to deselect
     * 
     * When post is selected, fetches its comments from database
     */
    fun selectPost(post: PostEntity?) {
        _selectedPost.value = post

        if (post != null) {
            // Post was selected - load its comments
            viewModelScope.launch {
                try {
                    repository.getComments(post.id).collect { comments ->
                        _currentComments.value = comments
                    }
                } catch (e: Exception) {
                    // If error loading comments, show empty list
                    _currentComments.value = emptyList()
                }
            }
        } else {
            // Post was deselected - clear comments
            _currentComments.value = emptyList()
        }
    }

    // ===== COMMENT FUNCTIONS =====

    /**
     * Add comment to currently selected post
     * 
     * @param content Comment text content
     * 
     * Requires a post to be selected
     * Uses current user as author
     */
    fun addComment(content: String) {
        val post = _selectedPost.value ?: return // Safety: must have post selected
        val authorName = _currentUser.value?.username ?: "Anonymous"

        viewModelScope.launch {
            try {
                // Add slight delay for animation
                delay(100)
                
                val newComment = CommentEntity(
                    postId = post.id,
                    author = authorName,
                    content = content
                )
                
                repository.addComment(newComment)
            } catch (e: Exception) {
                // In production, show error to user
                // For now, silently fail
            }
        }
    }

    // ===== FUTURE ENHANCEMENTS =====
    // You can add these functions later:
    
    // fun upvotePost(postId: Int) - Increase upvote count
    // fun deletePost(postId: Int) - Delete user's own post
    // fun editPost(postId: Int, newTitle: String, newContent: String) - Edit post
    // fun deleteComment(commentId: Int) - Delete user's own comment
    // fun searchPosts(query: String) - Search through posts
    // fun filterPostsByAuthor(author: String) - Show posts by specific author
}

/**
 * Factory for creating TavernViewModel instances
 * Required because ViewModel needs repository parameter
 */
class TavernViewModelFactory(
    private val repository: TavernRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TavernViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TavernViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
