package com.example.tavern.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * POST DAO
 * Data Access Object untuk operasi Post di database
 * 
 * Fungsi-fungsi:
 * - getAllPosts: Ambil semua post (untuk feed)
 * - searchPosts: Cari post berdasarkan keyword
 * - getPostsByAuthor: Ambil post dari user tertentu (untuk profile)
 * - insertPost: Tambah post baru
 * - deletePost: Hapus post (hanya owner)
 * 
 * Digunakan oleh:
 * - TavernRepository.kt untuk business logic
 * - TavernViewModel.kt melalui repository
 */
@Dao
interface PostDao {
    
    /**
     * GET ALL POSTS
     * Mengambil semua postingan, diurutkan dari yang terbaru
     * Returns Flow untuk auto-update UI saat ada perubahan
     * 
     * Flow ke:
     * - TavernRepository.allPosts
     * - TavernViewModel.uiState
     * - TavernApp.kt Feed screen (menampilkan list posts)
     */
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>
    
    /**
     * SEARCH POSTS
     * Mencari postingan berdasarkan keyword di title atau content
     * Menggunakan LIKE untuk partial matching (case-insensitive)
     * 
     * @param query Kata kunci pencarian
     * @return Flow<List<PostEntity>> yang auto-update
     * 
     * Flow ke:
     * - TavernRepository.searchPosts()
     * - TavernViewModel.updateSearchQuery()
     * - TavernApp.kt Search results display
     */
    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>
    
    /**
     * GET POSTS BY AUTHOR
     * Mengambil semua post dari user tertentu (untuk profile page)
     * 
     * @param username Username yang postnya ingin diambil
     * @return Flow<List<PostEntity>> yang auto-update
     * 
     * Flow ke:
     * - TavernRepository.getPostsByAuthor()
     * - TavernViewModel.viewProfile()
     * - ProfileScreen.kt "My Posts" section
     */
    @Query("SELECT * FROM posts WHERE author = :username ORDER BY timestamp DESC")
    fun getPostsByAuthor(username: String): Flow<List<PostEntity>>
    
    /**
     * INSERT POST
     * Menambahkan postingan baru ke database
     * Menggunakan REPLACE strategy: update jika ID sudah ada
     * 
     * @param post PostEntity yang akan ditambahkan
     * 
     * Dipanggil dari:
     * - TavernRepository.addPost()
     * - TavernViewModel.createPost() dengan Coroutines delay 1.5s
     * - TavernApp.kt AddPostDialog
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
    /**
     * DELETE POST
     * Menghapus postingan dari database
     * PENTING: Hanya owner yang boleh menghapus (dicek di ViewModel)
     * 
     * @param postId ID postingan yang akan dihapus
     * 
     * Dipanggil dari:
     * - TavernRepository.deletePost()
     * - TavernViewModel.deletePost() dengan Coroutines delay 1.5s
     * - TavernApp.kt Delete confirmation dialog
     * 
     * CASCADE EFFECT:
     * - Comments dengan postId ini akan ikut terhapus (jika ada foreign key)
     * - Cheers dengan postId ini akan ikut terhapus (jika ada foreign key)
     */
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)
}
