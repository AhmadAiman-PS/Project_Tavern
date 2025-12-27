package com.example.tavern.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * TAVERN DATABASE
 * Main database class untuk aplikasi Tavern
 * 
 * Entities (Tables):
 * - PostEntity: Tabel posts (id, author, title, content, timestamp)
 * - UserEntity: Tabel users (username, password, bio, avatarUrl, joinedDate)
 * - CommentEntity: Tabel comments (id, postId, author, content)
 * - CheerEntity: Tabel cheers (username, postId, timestamp) - NEW!
 * 
 * Version History:
 * - v1: Initial (Posts, Users)
 * - v2: Added Comments
 * - v3: Enhanced Comments
 * - v4: Enhanced Users (bio, avatar, joinedDate)
 * - v5: Added timestamp to Posts
 * - v6: Added Cheers system, removed upvotes from Posts
 * 
 * DAOs (Data Access Objects):
 * - PostDao: CRUD operations untuk posts
 * - UserDao: CRUD operations untuk users
 * - CommentDao: CRUD operations untuk comments
 * - CheerDao: CRUD operations untuk cheers
 * 
 * Migration Strategy:
 * - fallbackToDestructiveMigration(): Database akan di-recreate saat version berubah
 * - WARNING: Data lama akan hilang! Untuk production, gunakan proper migration
 * 
 * Digunakan di:
 * - TavernRepository.kt melalui DAOs
 * - TavernViewModel.kt melalui Repository
 * - MainActivity.kt untuk inisialisasi
 */
@Database(
    entities = [
        PostEntity::class,      // Tabel posts
        UserEntity::class,      // Tabel users
        CommentEntity::class,   // Tabel comments
        CheerEntity::class      // Tabel cheers (NEW!)
    ], 
    version = 6,               // Update version: 5 -> 6
    exportSchema = false       // Tidak export schema file
)
abstract class TavernDatabase : RoomDatabase() {
    
    // Abstract DAOs - Room will generate implementation
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao
    abstract fun cheerDao(): CheerDao  // NEW DAO!

    companion object {
        @Volatile
        private var Instance: TavernDatabase? = null

        /**
         * GET DATABASE
         * Singleton pattern: hanya satu instance database
         * Thread-safe dengan synchronized block
         * 
         * @param context Application context
         * @return TavernDatabase instance
         * 
         * Dipanggil dari:
         * - TavernApp.kt saat inisialisasi
         * - MainActivity.kt
         */
        fun getDatabase(context: Context): TavernDatabase {
            // Return existing instance if available
            return Instance ?: synchronized(this) {
                // Create new instance if null
                Room.databaseBuilder(
                    context,
                    TavernDatabase::class.java,
                    "tavern_db"  // Database file name
                )
                    .fallbackToDestructiveMigration()  // Recreate DB on version change
                    .build()
                    .also { Instance = it }  // Save instance
            }
        }
    }
}
