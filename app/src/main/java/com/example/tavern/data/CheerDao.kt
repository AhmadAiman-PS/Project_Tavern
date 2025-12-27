package com.example.tavern.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * CHEER DAO
 * Data Access Object untuk operasi Cheer di database
 * 
 * Fungsi-fungsi:
 * - getCheerCount: Hitung total cheers untuk suatu post
 * - hasUserCheered: Cek apakah user sudah cheer post tertentu
 * - addCheer: Tambah cheer baru
 * - removeCheer: Hapus cheer (un-cheer)
 * 
 * Digunakan oleh:
 * - TavernRepository.kt untuk business logic
 * - TavernViewModel.kt melalui repository
 */
@Dao
interface CheerDao {
    
    /**
     * GET CHEER COUNT
     * Menghitung total cheers untuk suatu postingan
     * 
     * @param postId ID postingan yang ingin dihitung cheersnya
     * @return Flow<Int> yang emit jumlah cheers (reactive, auto-update)
     * 
     * Flow ke:
     * - TavernViewModel.getCheerCount()
     * - UI akan auto-update ketika ada perubahan
     */
    @Query("SELECT COUNT(*) FROM cheers WHERE postId = :postId")
    fun getCheerCount(postId: Int): Flow<Int>
    
    /**
     * HAS USER CHEERED
     * Mengecek apakah user tertentu sudah memberikan cheer ke post
     * 
     * @param username Username yang ingin dicek
     * @param postId ID postingan yang ingin dicek
     * @return Flow<Int> yang emit 1 jika sudah cheer, 0 jika belum
     * 
     * Flow ke:
     * - TavernViewModel.hasUserCheered()
     * - UI menggunakan ini untuk menentukan warna button
     */
    @Query("SELECT COUNT(*) FROM cheers WHERE username = :username AND postId = :postId")
    fun hasUserCheered(username: String, postId: Int): Flow<Int>
    
    /**
     * ADD CHEER
     * Menambahkan cheer baru ke database
     * Menggunakan IGNORE strategy: jika sudah ada, tidak melakukan apa-apa
     * 
     * @param cheer CheerEntity yang akan ditambahkan
     * @return Long - row ID yang di-insert, atau -1 jika gagal
     * 
     * Dipanggil dari:
     * - TavernRepository.toggleCheer()
     * - TavernViewModel.toggleCheer()
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCheer(cheer: CheerEntity): Long
    
    /**
     * REMOVE CHEER
     * Menghapus cheer (un-cheer)
     * 
     * @param username Username yang ingin un-cheer
     * @param postId ID postingan yang ingin di-un-cheer
     * 
     * Dipanggil dari:
     * - TavernRepository.toggleCheer()
     * - TavernViewModel.toggleCheer()
     */
    @Query("DELETE FROM cheers WHERE username = :username AND postId = :postId")
    suspend fun removeCheer(username: String, postId: Int)
}
