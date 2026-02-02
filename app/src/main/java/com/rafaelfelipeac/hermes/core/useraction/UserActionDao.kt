package com.rafaelfelipeac.hermes.core.useraction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: UserActionEntity): Long

    @Query("SELECT * FROM user_actions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<UserActionEntity>>
}
