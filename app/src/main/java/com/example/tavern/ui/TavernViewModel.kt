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
 * Manages UI state and business logic untuk aplikasi Tavern
 * 
 * Responsibilities:
 * - State management (posts, users, comments, cheers)
 * - Business logic (auth, CRUD operations)
 * - Loading states untuk Coroutines
 * - Error handling
 * 
 * Data mengalir:
 * Repository -> ViewModel (StateFlow) -> UI (Compose)
 * UI events -> ViewModel (functions) -> Repository -> Database
 * 
 * Digunakan di:
 * - TavernApp.kt (main navigation & screens)
 * - ProfileScreen.kt (user profile)
 * - RegisterScreen.kt (registration)
 */
class TavernViewModel(private val repository: TavernRepository) : ViewModel() {

    // ==========================================
    // STATE MANAGEMENT
    // ==========================================
    // Semua state menggunakan StateFlow untuk:
    // - Reactive updates (UI auto-update saat data berubah)
    // - Lifecycle-aware (tidak leak memory)
    // - Type-safe
    
    // ===== POSTS STATE =====
    
    /**
     * UI STATE - ALL POSTS
     * Flow dari repository.allPosts -> auto-update saat ada post baru/dihapus
     * 
     * Data dari: PostDao.getAllPosts()
     * Muncul di: TavernApp.kt Feed screen (list posts)
     * 
     * stateIn: Convert Flow biasa jadi StateFlow untuk Compose
     * WhileSubscribed(5000): Keep active 5 detik after last subscriber
     */
    val uiState: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== USER STATE =====
    
    /**
     * CURRENT USER
     * User yang sedang login
     * null = belum login/sudah logout
     * 
     * Set di: login(), register()
     * Clear di: logout()
     * Digunakan di: Semua screen untuk authorization check
     */
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    /**
     * LOGIN ERROR
     * Error message untuk login/register
     * null = no error
     * 
     * Set di: login(), register()
     * Clear di: clearError()
     * Ditampilkan di: LoginScreen, RegisterScreen
     */
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    /**
     * IS LOADING
     * Global loading state untuk operasi async
     * true = sedang proses (show loading indicator)
     * false = selesai (hide loading indicator)
     * 
     * Set di: Semua suspend functions (login, register, post, comment, dll)
     * Digunakan di: Semua screens untuk disable buttons & show loading
     * 
     * COROUTINES IMPLEMENTATION:
     * - Set true sebelum delay
     * - Set false di finally block (always executed)
     * - Mencegah double-click/spam
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ===== POST DETAIL STATE =====
    
    /**
     * SELECTED POST
     * Post yang sedang dibuka di detail screen
     * null = sedang di feed (tidak ada post yang dipilih)
     * 
     * Set di: selectPost()
     * Clear di: selectPost(null), logout()
     * Digunakan di: PostDetailScreen untuk navigasi
     */
    private val _selectedPost = MutableStateFlow<PostEntity?>(null)
    val selectedPost = _selectedPost.asStateFlow()

    /**
     * CURRENT COMMENTS
     * Comments untuk selected post
     * Empty list = tidak ada comments atau tidak ada post yang dipilih
     * 
     * Update otomatis dari: CommentDao.getCommentsForPost()
     * Muncul di: PostDetailScreen comment list
     */
    private val _currentComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val currentComments = _currentComments.asStateFlow()

    // ===== SEARCH STATE =====
    
    /**
     * SEARCH QUERY
     * Kata kunci pencarian
     * Empty string = tidak sedang search
     * 
     * Set di: updateSearchQuery()
     * Clear di: clearSearch()
     * Digunakan di: Search bar UI
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    /**
     * SEARCH RESULTS
     * Hasil pencarian posts
     * 
     * Update dari: PostDao.searchPosts()
     * Muncul di: Feed screen (replace allPosts saat searching)
     */
    private val _searchResults = MutableStateFlow<List<PostEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    
    /**
     * IS SEARCHING
     * Flag apakah sedang dalam mode search
     * 
     * true = showing search results
     * false = showing all posts
     * 
     * Digunakan untuk: Toggle antara allPosts dan searchResults
     */
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // ===== PROFILE STATE =====
    
    /**
     * PROFILE USER
     * User yang sedang dilihat profilenya
     * null = tidak sedang lihat profile
     * 
     * Set di: viewProfile()
     * Clear di: exitProfile(), logout()
     * Muncul di: ProfileScreen
     */
    private val _profileUser = MutableStateFlow<UserEntity?>(null)
    val profileUser = _profileUser.asStateFlow()
    
    /**
     * PROFILE POSTS
     * Posts dari user yang sedang dilihat profilenya
     * 
     * Update dari: PostDao.getPostsByAuthor()
     * Muncul di: ProfileScreen "My Posts" section
     */
    private val _profilePosts = MutableStateFlow<List<PostEntity>>(emptyList())
    val profilePosts = _profilePosts.asStateFlow()

    // ==========================================
    // AUTHENTICATION FUNCTIONS
    // ==========================================
    
    /**
     * LOGIN
     * Memverifikasi username & password
     * 
     * COROUTINES IMPLEMENTATION:
     * - viewModelScope.launch: Run di background thread
     * - delay(1500): Simulasi network latency 1.5 detik
     * - try-catch-finally: Error handling & cleanup
     * - isLoading: Prevent double-click
     * 
     * Flow:
     * 1. Set isLoading = true (disable UI)
     * 2. Delay 1.5s (simulate network)
     * 3. Call repository.login() (check database)
     * 4. Update currentUser atau loginError
     * 5. Set isLoading = false (enable UI)
     * 
     * @param user Username
     * @param pass Password
     * 
     * Success: currentUser terisi -> Navigate ke Feed
     * Fail: loginError terisi -> Show error message
     */
    fun login(user: String, pass: String) {
        // Clear previous errors
        _loginError.value = null
        
        // Set loading state (disable buttons, show loading)
        _isLoading.value = true

        // COROUTINE: Launch background task
        viewModelScope.launch {
            try {
                // COROUTINE: Simulate network delay (non-blocking)
                // UI tetap responsive, hanya button yang disabled
                delay(1500)  // 1.5 seconds
                
                // Call repository (suspend function)
                val validUser = repository.login(user, pass)
                
                // Update state based on result
                if (validUser != null) {
                    _currentUser.value = validUser  // Login success
                    _loginError.value = null
                } else {
                    _loginError.value = "Invalid Name or Password, traveler."
                }
            } catch (e: Exception) {
                // Handle unexpected errors
                _loginError.value = "An error occurred. Please try again."
            } finally {
                // Always executed, even if exception
                // Ensure loading is stopped
                _isLoading.value = false
            }
        }
    }

    /**
     * REGISTER
     * Mendaftarkan user baru
     * 
     * COROUTINES IMPLEMENTATION: Same as login()
     * 
     * Flow:
     * 1. Validate (done in UI)
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.register()
     * 5. If success: Auto-login
     * 6. Set isLoading = false
     * 
     * @param user Username
     * @param pass Password
     * 
     * Success: Auto-call login()
     * Fail: loginError = username already taken
     */
    fun register(user: String, pass: String) {
        _loginError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // COROUTINE: Network delay simulation
                delay(1500)
                
                val success = repository.register(UserEntity(user, pass))
                
                if (success) {
                    // Auto-login after successful registration
                    login(user, pass)
                } else {
                    _loginError.value = "That name is already taken! Choose another legend."
                    _isLoading.value = false  // Stop loading since we're not continuing
                }
            } catch (e: Exception) {
                _loginError.value = "Registration failed. Please try again."
                _isLoading.value = false
            }
            // Note: No finally needed here because login() handles loading state
        }
    }

    /**
     * LOGOUT
     * Clear all user state dan kembali ke login screen
     * 
     * Efek:
     * - currentUser = null
     * - selectedPost = null
     * - profileUser = null
     * - searchQuery = ""
     * - Navigate ke LoginScreen (handled by TavernApp)
     */
    fun logout() {
        _currentUser.value = null
        selectPost(null)
        exitProfile()
        clearSearch()
        _loginError.value = null
    }

    /**
     * CLEAR ERROR
     * Clear login/register error message
     * Called saat user mulai mengetik (clear error on input change)
     */
    fun clearError() {
        _loginError.value = null
    }

    // ==========================================
    // POST FUNCTIONS
    // ==========================================

    /**
     * CREATE POST
     * Membuat postingan baru
     * 
     * COROUTINES IMPLEMENTATION:
     * - delay(1500): Simulate posting to server
     * - isLoading: Disable form & show loading in button
     * 
     * Flow:
     * 1. Get current user (author)
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.addPost()
     * 5. Post muncul di allPosts Flow (auto-update UI)
     * 6. Set isLoading = false
     * 7. Dialog auto-close (handled in UI)
     * 
     * @param title Post title
     * @param content Post content
     * 
     * Post muncul di: Feed screen (via allPosts Flow)
     */
    fun createPost(title: String, content: String) {
        val authorName = _currentUser.value?.username ?: "Anonymous"
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // COROUTINE: Network delay
                delay(1500)
                
                repository.addPost(
                    PostEntity(
                        author = authorName,
                        title = title,
                        content = content
                        // timestamp: auto-set in entity
                        // id: auto-generated
                    )
                )
            } catch (e: Exception) {
                // Handle error (could show toast/snackbar)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * SELECT POST
     * Membuka post untuk melihat detail & comments
     * 
     * Flow:
     * 1. Set selectedPost = post
     * 2. Fetch comments dari database (auto-update via Flow)
     * 3. Navigate ke PostDetailScreen (handled by TavernApp)
     * 
     * @param post Post yang ingin dibuka (null untuk close)
     * 
     * Comments muncul di: currentComments StateFlow
     * Ditampilkan di: PostDetailScreen comment list
     */
    fun selectPost(post: PostEntity?) {
        _selectedPost.value = post

        if (post != null) {
            // Post selected: Load comments
            viewModelScope.launch {
                try {
                    // Collect Flow (auto-update saat ada comment baru)
                    repository.getComments(post.id).collect { comments ->
                        _currentComments.value = comments
                    }
                } catch (e: Exception) {
                    _currentComments.value = emptyList()
                }
            }
        } else {
            // Post deselected: Clear comments
            _currentComments.value = emptyList()
        }
    }
    
    /**
     * DELETE POST
     * Menghapus postingan (HANYA OWNER YANG BISA)
     * 
     * AUTHORIZATION CHECK: Dilakukan di sini!
     * User hanya bisa delete postnya sendiri
     * 
     * COROUTINES IMPLEMENTATION:
     * - delay(1500): Simulate server deletion
     * - isLoading: Show loading, prevent double-delete
     * 
     * Flow:
     * 1. Check authorization (currentUser == post.author)
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.deletePost()
     * 5. Post hilang dari allPosts Flow (auto-update UI)
     * 6. Set isLoading = false
     * 7. Navigate back (handled in UI)
     * 
     * @param post Post yang akan dihapus
     * 
     * Post hilang dari: Feed, Profile, Search results (via Flow)
     */
    fun deletePost(post: PostEntity) {
        // AUTHORIZATION CHECK
        val currentUsername = _currentUser.value?.username
        if (currentUsername == null || currentUsername != post.author) {
            // Not authorized: current user bukan owner
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // COROUTINE: Network delay
                delay(1500)
                
                // Delete from database
                repository.deletePost(post.id)
                
                // If we're viewing this post, clear selection
                if (_selectedPost.value?.id == post.id) {
                    selectPost(null)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // COMMENT FUNCTIONS
    // ==========================================

    /**
     * ADD COMMENT
     * Menambahkan comment ke post yang sedang dibuka
     * 
     * COROUTINES IMPLEMENTATION:
     * - delay(1500): Simulate posting comment
     * - isLoading: Disable input & show loading in send button
     * 
     * Flow:
     * 1. Get selected post & current user
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.addComment()
     * 5. Comment muncul di currentComments Flow
     * 6. Set isLoading = false
     * 7. Input field auto-clear (handled in UI)
     * 
     * @param content Comment text
     * 
     * Comment muncul di: PostDetailScreen comment list
     */
    fun addComment(content: String) {
        val post = _selectedPost.value ?: return  // Safety: must have post
        val authorName = _currentUser.value?.username ?: "Anonymous"
        
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // COROUTINE: Network delay
                delay(1500)
                
                val newComment = CommentEntity(
                    postId = post.id,
                    author = authorName,
                    content = content
                    // id: auto-generated
                )
                
                repository.addComment(newComment)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // SEARCH FUNCTIONS
    // ==========================================
    
    /**
     * UPDATE SEARCH QUERY
     * Update keyword pencarian & fetch results
     * 
     * Flow:
     * 1. Update searchQuery
     * 2. If empty: Clear search, show all posts
     * 3. If not empty: Set isSearching = true, fetch results
     * 4. Results auto-update via Flow
     * 
     * @param query Keyword pencarian
     * 
     * Results muncul di: searchResults StateFlow
     * Ditampilkan di: Feed screen (replace allPosts)
     * 
     * Note: Instant search (no delay) untuk better UX
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            // Clear search
            _isSearching.value = false
            _searchResults.value = emptyList()
        } else {
            // Perform search
            _isSearching.value = true
            viewModelScope.launch {
                try {
                    // Collect search results (auto-update)
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
     * CLEAR SEARCH
     * Reset search state & show all posts
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
        _searchResults.value = emptyList()
    }

    // ==========================================
    // PROFILE FUNCTIONS
    // ==========================================
    
    /**
     * VIEW PROFILE
     * Membuka profile page user tertentu
     * 
     * Flow:
     * 1. Fetch user data
     * 2. Set profileUser
     * 3. Fetch user's posts
     * 4. Set profilePosts
     * 5. Navigate ke ProfileScreen (handled by TavernApp)
     * 
     * @param username Username yang profilenya ingin dilihat
     * 
     * Data muncul di: ProfileScreen
     */
    fun viewProfile(username: String) {
        viewModelScope.launch {
            try {
                // Fetch user data
                val user = repository.getUserByUsername(username)
                _profileUser.value = user
                
                // Fetch user's posts
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
     * UPDATE PROFILE
     * Update bio & avatar user
     * 
     * COROUTINES IMPLEMENTATION:
     * - delay(1500): Simulate updating server
     * - isLoading: Disable form & show loading in save button
     * 
     * Flow:
     * 1. Get current user
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.updateUser()
     * 5. Update currentUser & profileUser
     * 6. Set isLoading = false
     * 7. Exit edit mode (handled in UI)
     * 
     * @param bio New bio text
     * @param avatarUrl New avatar identifier
     * 
     * Changes muncul di: ProfileScreen & currentUser
     */
    fun updateProfile(bio: String, avatarUrl: String) {
        val user = _currentUser.value ?: return
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // COROUTINE: Network delay
                delay(1500)
                
                // Create updated user
                val updatedUser = user.copy(bio = bio, avatarUrl = avatarUrl)
                
                // Update in database
                repository.updateUser(updatedUser)
                
                // Update local state
                _currentUser.value = updatedUser
                
                // If viewing own profile, update profileUser too
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
     * EXIT PROFILE
     * Close profile page & return to feed
     */
    fun exitProfile() {
        _profileUser.value = null
        _profilePosts.value = emptyList()
    }
    
    // ==========================================
    // CHEER FUNCTIONS (NEW!)
    // ==========================================
    
    /**
     * TOGGLE CHEER
     * Like atau unlike postingan
     * 
     * COROUTINES IMPLEMENTATION:
     * - delay(1500): Simulate updating server
     * - isLoading: Show loading animation di button
     * 
     * Flow:
     * 1. Get current user
     * 2. Set isLoading = true
     * 3. Delay 1.5s
     * 4. Call repository.toggleCheer()
     * 5. Cheer count auto-update via Flow
     * 6. Button color auto-update via hasUserCheered Flow
     * 7. Set isLoading = false
     * 
     * @param post Post yang di-cheer/un-cheer
     * 
     * UI updates:
     * - Cheer count number (via getCheerCount Flow)
     * - Button color (via hasUserCheered Flow)
     * - Button animation (shake/color change)
     */
    fun toggleCheer(post: PostEntity) {
        val username = _currentUser.value?.username ?: return
        
        // Note: Kita tidak set global isLoading untuk cheer
        // karena user harus bisa cheer multiple posts sekaligus
        // Loading state per-post dihandle di UI
        
        viewModelScope.launch {
            try {
                // COROUTINE: Network delay
                delay(500)  // Shorter delay for better UX
                
                // Toggle cheer in database
                repository.toggleCheer(username, post.id)
                
                // UI will auto-update via Flows:
                // - getCheerCount() akan emit angka baru
                // - hasUserCheered() akan emit status baru
            } catch (e: Exception) {
                // Handle error (could show toast)
            }
        }
    }
}

/**
 * TAVERN VIEW MODEL FACTORY
 * Factory pattern untuk create ViewModel dengan dependencies
 * Required karena ViewModel butuh repository parameter
 * 
 * Digunakan di:
 * - TavernApp.kt: viewModel(factory = TavernViewModelFactory(repository))
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
