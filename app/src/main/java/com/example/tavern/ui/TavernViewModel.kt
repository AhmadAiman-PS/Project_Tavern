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
 * Enhanced with loading states and Coroutines simulation
 */
class TavernViewModel(private val repository: TavernRepository) : ViewModel() {

    // ===== POSTS STATE =====
    val uiState: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== USER STATE =====
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    /**
     * Loading state for async operations
     * TRUE = Operation in progress (show loading indicator)
     * FALSE = Operation complete (hide loading indicator)
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ===== POST DETAIL STATE =====
    private val _selectedPost = MutableStateFlow<PostEntity?>(null)
    val selectedPost = _selectedPost.asStateFlow()

    private val _currentComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val currentComments = _currentComments.asStateFlow()

    // ===== SEARCH STATE =====
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<PostEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // ===== PROFILE STATE =====
    private val _profileUser = MutableStateFlow<UserEntity?>(null)
    val profileUser = _profileUser.asStateFlow()
    
    private val _profilePosts = MutableStateFlow<List<PostEntity>>(emptyList())
    val profilePosts = _profilePosts.asStateFlow()

    // ===== AUTHENTICATION FUNCTIONS =====

    /**
     * Login user with network delay simulation
     * 
     * Coroutines Implementation:
     * - Uses suspend function with delay() to simulate network latency
     * - Sets isLoading = true during operation
     * - Runs on viewModelScope (background thread)
     * - UI remains responsive during delay
     */
    fun login(user: String, pass: String) {
        // Clear previous errors
        _loginError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (1.5 seconds)
                // This runs on background thread, UI doesn't freeze
                delay(1500)
                
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
                // Always set loading to false when done
                _isLoading.value = false
            }
        }
    }

    /**
     * Register new user with network delay simulation
     */
    fun register(user: String, pass: String) {
        _loginError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (1.5 seconds)
                delay(1500)
                
                val success = repository.register(UserEntity(user, pass))
                
                if (success) {
                    // Auto-login after successful registration
                    login(user, pass)
                } else {
                    _loginError.value = "That name is already taken! Choose another legend."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _loginError.value = "Registration failed. Please try again."
                _isLoading.value = false
            }
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        _currentUser.value = null
        selectPost(null)
        exitProfile()
        clearSearch()
        _loginError.value = null
    }

    /**
     * Clear login/registration error
     */
    fun clearError() {
        _loginError.value = null
    }

    // ===== POST FUNCTIONS =====

    /**
     * Create new post with network delay simulation
     * 
     * Coroutines Implementation:
     * - Simulates sending data to server with 1.5s delay
     * - Shows loading indicator during operation
     * - Prevents double-posting by disabling button
     */
    fun createPost(title: String, content: String) {
        val authorName = _currentUser.value?.username ?: "Anonymous"
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (1.5 seconds)
                delay(1500)
                
                repository.addPost(
                    PostEntity(
                        author = authorName,
                        title = title,
                        content = content,
                        upvotes = (0..100).random()
                    )
                )
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select post to view details and comments
     */
    fun selectPost(post: PostEntity?) {
        _selectedPost.value = post

        if (post != null) {
            viewModelScope.launch {
                try {
                    repository.getComments(post.id).collect { comments ->
                        _currentComments.value = comments
                    }
                } catch (e: Exception) {
                    _currentComments.value = emptyList()
                }
            }
        } else {
            _currentComments.value = emptyList()
        }
    }

    // ===== COMMENT FUNCTIONS =====

    /**
     * Add comment with network delay simulation
     * 
     * Coroutines Implementation:
     * - Simulates posting comment to server with 1.5s delay
     * - Shows loading indicator in send button
     * - Automatically clears input field after success
     */
    fun addComment(content: String) {
        val post = _selectedPost.value ?: return
        val authorName = _currentUser.value?.username ?: "Anonymous"
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (1.5 seconds)
                delay(1500)
                
                val newComment = CommentEntity(
                    postId = post.id,
                    author = authorName,
                    content = content
                )
                
                repository.addComment(newComment)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ===== SEARCH FUNCTIONS =====
    
    /**
     * Update search query and perform search
     * Search is instant (no delay) for better UX
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _isSearching.value = false
            _searchResults.value = emptyList()
        } else {
            _isSearching.value = true
            viewModelScope.launch {
                try {
                    repository.searchPosts(query).collect { results ->
                        _searchResults.value = results
                    }
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                }
            }
        }
    }
    
    /**
     * Clear search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
        _searchResults.value = emptyList()
    }

    // ===== PROFILE FUNCTIONS =====
    
    /**
     * View user profile
     */
    fun viewProfile(username: String) {
        viewModelScope.launch {
            try {
                // Load user data
                val user = repository.getUserByUsername(username)
                _profileUser.value = user
                
                // Load user's posts
                if (user != null) {
                    repository.getPostsByAuthor(username).collect { posts ->
                        _profilePosts.value = posts
                    }
                }
            } catch (e: Exception) {
                _profileUser.value = null
                _profilePosts.value = emptyList()
            }
        }
    }
    
    /**
     * Update current user profile with network delay simulation
     * 
     * Coroutines Implementation:
     * - Simulates updating profile on server with 1.5s delay
     * - Shows loading indicator in Save button
     * - Disables form during update
     */
    fun updateProfile(bio: String, avatarUrl: String) {
        val user = _currentUser.value ?: return
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (1.5 seconds)
                delay(1500)
                
                val updatedUser = user.copy(bio = bio, avatarUrl = avatarUrl)
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
                
                // If viewing own profile, update it too
                if (_profileUser.value?.username == user.username) {
                    _profileUser.value = updatedUser
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Exit profile view
     */
    fun exitProfile() {
        _profileUser.value = null
        _profilePosts.value = emptyList()
    }
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
