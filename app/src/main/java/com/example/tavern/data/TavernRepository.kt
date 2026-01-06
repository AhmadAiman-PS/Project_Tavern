package com.example.tavern.data

import kotlinx.coroutines.flow.Flow

/**
 * TAVERN REPOSITORY
 * Single source of truth untuk data operations
 * Menggabungkan semua DAOs dan menyediakan API yang clean untuk ViewModel
 * 
 * DAOs yang digunakan:
 * - postDao: Operasi posts (CRUD)
 * - userDao: Operasi users (auth, profile)
 * - commentDao: Operasi comments (CRUD)
 * - cheerDao: Operasi cheers/likes (toggle, count)
 * 
 * Dipanggil oleh:
 * - TavernViewModel.kt untuk semua business logic
 * - ViewModel akan expose data ini ke UI via StateFlow
 */
class TavernRepository(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val commentDao: CommentDao,
    private val cheerDao: CheerDao  // NEW DAO untuk cheers
) {

    // ===== POST OPERATIONS =====
    
    /**
     * ALL POSTS FLOW
     * Flow yang emit list semua posts, auto-update saat ada perubahan
     * 
     * Flow dari: PostDao.getAllPosts()
     * Flow ke: TavernViewModel.uiState
     * Ditampilkan di: TavernApp.kt Feed screen
     */
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    /**
     * ADD POST
     * Menambahkan postingan baru
     * Suspend function: harus dipanggil dari coroutine
     * 
     * @param post PostEntity yang akan ditambahkan
     * 
     * Dipanggil dari: TavernViewModel.createPost() dengan delay 1.5s
     * Data muncul di: allPosts Flow -> Feed screen
     */
    suspend fun addPost(post: PostEntity) {
        postDao.insertPost(post)
    }
    
    /**
     * SEARCH POSTS
     * Mencari posts berdasarkan keyword
     * 
     * @param query Kata kunci pencarian
     * @return Flow<List<PostEntity>> hasil pencarian
     * 
     * Dipanggil dari: TavernViewModel.updateSearchQuery()
     * Data muncul di: searchResults StateFlow -> Search results UI
     */
    fun searchPosts(query: String): Flow<List<PostEntity>> {
        return postDao.searchPosts(query)
    }
    
    /**
     * GET POSTS BY AUTHOR
     * Mengambil semua posts dari user tertentu
     * 
     * @param username Username yang postsnya ingin diambil
     * @return Flow<List<PostEntity>> posts dari user
     * 
     * Dipanggil dari: TavernViewModel.viewProfile()
     * Data muncul di: profilePosts StateFlow -> ProfileScreen.kt
     */
    fun getPostsByAuthor(username: String): Flow<List<PostEntity>> {
        return postDao.getPostsByAuthor(username)
    }
    
    /**
     * DELETE POST
     * Menghapus postingan
     * PENTING: Authorization check dilakukan di ViewModel
     * 
     * @param postId ID postingan yang akan dihapus
     * 
     * Dipanggil dari: TavernViewModel.deletePost() dengan delay 1.5s
     * Efek: Post hilang dari allPosts Flow -> UI auto-update
     */
    suspend fun deletePost(postId: Int) {
        postDao.deletePost(postId)
    }

    // ===== USER OPERATIONS =====
    
    /**
     * LOGIN
     * Memverifikasi username dan password
     * 
     * @param user Username
     * @param pass Password
     * @return UserEntity jika valid, null jika invalid
     * 
     * Dipanggil dari: TavernViewModel.login() dengan delay 1.5s
     * Data muncul di: currentUser StateFlow -> Auth state
     */
    suspend fun login(user: String, pass: String): UserEntity? {
        return userDao.login(user, pass)
    }

    /**
     * REGISTER
     * Mendaftarkan user baru
     * 
     * @param user UserEntity baru
     * @return Boolean - true jika berhasil, false jika username sudah ada
     * 
     * Dipanggil dari: TavernViewModel.register() dengan delay 1.5s
     * Success: Auto-login -> currentUser StateFlow
     */
    suspend fun register(user: UserEntity): Boolean {
        val result = userDao.register(user)
        return result != -1L  // -1 means username already exists
    }
    
    /**
     * GET USER BY USERNAME
     * Mengambil data user berdasarkan username
     * Suspend function: one-time fetch
     * 
     * @param username Username yang dicari
     * @return UserEntity jika ditemukan, null jika tidak
     * 
     * Dipanggil dari: TavernViewModel.viewProfile()
     * Data muncul di: profileUser StateFlow -> ProfileScreen.kt
     */
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }
    
    /**
     * GET USER BY USERNAME FLOW
     * Mengambil data user dengan Flow (auto-update)
     * 
     * @param username Username yang dicari
     * @return Flow<UserEntity?> yang emit user data
     * 
     * Bisa digunakan untuk: Real-time profile updates
     */
    fun getUserByUsernameFlow(username: String): Flow<UserEntity?> {
        return userDao.getUserByUsernameFlow(username)
    }
    
    /**
     * UPDATE USER
     * Mengupdate data user (bio, avatar, dll)
     * 
     * @param user UserEntity dengan data baru
     * 
     * Dipanggil dari: TavernViewModel.updateProfile() dengan delay 1.5s
     * Data muncul di: currentUser & profileUser StateFlow
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    // ===== COMMENT OPERATIONS =====
    
    /**
     * GET COMMENTS
     * Mengambil semua comments untuk suatu post
     * 
     * @param postId ID post yang commentsnya ingin diambil
     * @return Flow<List<CommentEntity>> yang auto-update
     * 
     * Dipanggil dari: TavernViewModel.selectPost()
     * Data muncul di: currentComments StateFlow -> PostDetailScreen.kt
     */
    fun getComments(postId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForPost(postId)
    }

    /**
     * ADD COMMENT
     * Menambahkan comment baru
     * 
     * @param comment CommentEntity yang akan ditambahkan
     * 
     * Dipanggil dari: TavernViewModel.addComment() dengan delay 1.5s
     * Data muncul di: currentComments Flow -> Comment list UI
     */
    suspend fun addComment(comment: CommentEntity) {
        commentDao.insertComment(comment)
    }
    
    // ===== CHEER OPERATIONS (NEW!) =====
    
    /**
     * GET CHEER COUNT
     * Menghitung total cheers untuk suatu post
     * 
     * @param postId ID post yang ingin dihitung cheersnya
     * @return Flow<Int> jumlah cheers (auto-update)
     * 
     * Dipanggil dari: TavernViewModel untuk setiap post
     * Data muncul di: PostCard UI (angka di samping icon gelas)
     */
    fun getCheerCount(postId: Int): Flow<Int> {
        return cheerDao.getCheerCount(postId)
    }
    
    /**
     * HAS USER CHEERED
     * Cek apakah user sudah cheer post tertentu
     * 
     * @param username Username yang dicek
     * @param postId ID post yang dicek
     * @return Flow<Boolean> true jika sudah cheer, false jika belum
     * 
     * Dipanggil dari: TavernViewModel untuk setiap post
     * Data digunakan untuk: Menentukan warna button cheer (primary vs outline)
     */
    fun hasUserCheered(username: String, postId: Int): Flow<Int> {
        return cheerDao.hasUserCheered(username, postId)
    }
    
    /**
     * TOGGLE CHEER
     * Menambah atau menghapus cheer (like/unlike)
     * Logic: Jika belum cheer -> add, Jika sudah cheer -> remove
     * 
     * @param username Username yang melakukan action
     * @param postId ID post yang di-cheer/un-cheer
     * @return Boolean - true jika berhasil
     * 
     * Dipanggil dari: TavernViewModel.toggleCheer() dengan delay 1.5s
     * Efek: 
     * - cheerCount Flow akan auto-update
     * - hasUserCheered Flow akan auto-update
     * - UI akan auto-update (angka & warna button)
     */
    suspend fun toggleCheer(username: String, postId: Int): Boolean {
        return try {
            // Cek apakah user sudah cheer
            val alreadyCheered = cheerDao.hasUserCheered(username, postId)
            
            // PERHATIAN: hasUserCheered return Flow, tapi kita butuh nilai sekarang
            // Untuk simplicity, kita coba add dulu
            val result = cheerDao.addCheer(CheerEntity(username, postId))
            
            if (result == -1L) {
                // Gagal add (already exists), berarti remove
                cheerDao.removeCheer(username, postId)
            }
            
            true  // Success
        } catch (e: Exception) {
            false  // Failed
        }
    }
}
