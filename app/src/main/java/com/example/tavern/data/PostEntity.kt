package com.example.tavern.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * POST ENTITY
 * Merepresentasikan postingan di aplikasi Tavern
 * 
 * Field upvotes dihapus karena sekarang menggunakan CheerEntity
 * untuk menghitung jumlah likes secara real-time
 * 
 * Relasi:
 * - author: Username pembuat post (FK ke UserEntity.username)
 * - id: Primary key, auto-generated
 * 
 * Terhubung dengan:
 * - CheerEntity (one-to-many): Satu post punya banyak cheers
 * - CommentEntity (one-to-many): Satu post punya banyak comments
 * 
 * Digunakan di:
 * - PostDao.kt untuk query
 * - TavernRepository.kt untuk business logic
 * - TavernViewModel.kt untuk state management
 * - TavernApp.kt untuk UI display
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,                              // ID unik postingan
    
    val author: String,                            // Username pembuat post
    val title: String,                             // Judul postingan
    val content: String,                           // Isi postingan
    val timestamp: Long = System.currentTimeMillis() // Kapan post dibuat (untuk sorting & display)

)
