package com.example.tavern.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun login(user: String, pass: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun register(user: UserEntity): Long // Returns -1 if user exists
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsernameFlow(username: String): Flow<UserEntity?>
    
    @Update
    suspend fun updateUser(user: UserEntity)
}
