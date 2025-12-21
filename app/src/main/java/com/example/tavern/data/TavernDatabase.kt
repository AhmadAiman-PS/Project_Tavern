package com.example.tavern.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [PostEntity::class, UserEntity::class, CommentEntity::class], version = 5, exportSchema = false)
abstract class TavernDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao


    companion object {
        @Volatile
        private var Instance: TavernDatabase? = null

        fun getDatabase(context: Context): TavernDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TavernDatabase::class.java, "tavern_db")
                    .fallbackToDestructiveMigration() // This will recreate database on version change
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

// The Repository separates the ViewModel from the Database direct access
