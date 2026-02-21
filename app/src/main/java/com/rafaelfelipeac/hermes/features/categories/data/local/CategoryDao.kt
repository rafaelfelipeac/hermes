package com.rafaelfelipeac.hermes.features.categories.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET name = :name WHERE id = :id")
    suspend fun updateName(
        id: Long,
        name: String,
    )

    @Query("UPDATE categories SET colorId = :colorId WHERE id = :id")
    suspend fun updateColor(
        id: Long,
        colorId: String,
    )

    @Query("UPDATE categories SET isHidden = :isHidden WHERE id = :id")
    suspend fun updateVisibility(
        id: Long,
        isHidden: Boolean,
    )

    @Query("UPDATE categories SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(
        id: Long,
        sortOrder: Int,
    )

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
