package com.example.tavern.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class TavernDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var Instance: TavernDatabase? = null

        fun getDatabase(context: Context): TavernDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TavernDatabase::class.java, "tavern_db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

// The Repository separates the ViewModel from the Database direct access
class TavernRepository(private val postDao: PostDao) {
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()

    suspend fun addPost(post: PostEntity) {
        postDao.insertPost(post)
    }
}