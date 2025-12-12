package com.example.tavern.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities = [PostEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class TavernDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var Instance: TavernDatabase? = null

        fun getDatabase(context: Context): TavernDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TavernDatabase::class.java, "tavern_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

// The Repository separates the ViewModel from the Database direct access