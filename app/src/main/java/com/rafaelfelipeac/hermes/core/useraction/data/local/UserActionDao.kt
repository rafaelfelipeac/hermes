package com.rafaelfelipeac.hermes.core.useraction.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActionDao {
    @Query("SELECT * FROM user_actions ORDER BY timestamp DESC")
    suspend fun getAll(): List<UserActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: UserActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<UserActionEntity>): List<Long>

    @Query("SELECT * FROM user_actions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<UserActionEntity>>

    @Query("DELETE FROM user_actions")
    suspend fun deleteAll()
}
