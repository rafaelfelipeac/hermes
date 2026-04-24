package com.rafaelfelipeac.hermes.features.weeklytraining.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
@Suppress("TooManyFunctions")
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    suspend fun getAll(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE weekStartDate = :weekStartDate")
    suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE weekStartDate = :weekStartDate")
    fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE weekStartDate IN (:weekStartDates)")
    suspend fun getWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE weekStartDate IN (:weekStartDates)")
    fun observeWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): Flow<List<WorkoutEntity>>

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertAll(workouts: List<WorkoutEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(workouts: List<WorkoutEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("UPDATE workouts SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletion(
        id: Long,
        isCompleted: Boolean,
    )

    @Query("UPDATE workouts SET dayOfWeek = :dayOfWeek, timeSlot = :timeSlot, sort_order = :order WHERE id = :id")
    suspend fun updateDayAndOrder(
        id: Long,
        dayOfWeek: Int?,
        timeSlot: String?,
        order: Int,
    )

    @Query(
        "UPDATE workouts SET weekStartDate = :weekStartDate, dayOfWeek = :dayOfWeek, " +
            "timeSlot = :timeSlot, sort_order = :order WHERE id = :id",
    )
    suspend fun updateSchedule(
        id: Long,
        weekStartDate: LocalDate,
        dayOfWeek: Int?,
        timeSlot: String?,
        order: Int,
    )

    @Suppress("LongParameterList")
    @Query(
        "UPDATE workouts SET type = :type, description = :description, " +
            "isRestDay = :isRestDay, eventType = :eventType, categoryId = :categoryId WHERE id = :id",
    )
    suspend fun updateDetails(
        id: Long,
        type: String,
        description: String,
        isRestDay: Boolean,
        eventType: String,
        categoryId: Long?,
    )

    @Query(
        "UPDATE workouts SET categoryId = :uncategorizedId WHERE categoryId IS NULL AND isRestDay = 0",
    )
    suspend fun assignNullCategoryTo(uncategorizedId: Long)

    @Query(
        "UPDATE workouts SET categoryId = :uncategorizedId WHERE categoryId = :deletedCategoryId",
    )
    suspend fun reassignCategory(
        deletedCategoryId: Long,
        uncategorizedId: Long,
    )

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM workouts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM workouts WHERE weekStartDate = :weekStartDate")
    suspend fun deleteByWeekStartDate(weekStartDate: LocalDate)

    @Transaction
    suspend fun replaceWorkoutsForWeek(
        weekStartDate: LocalDate,
        workouts: List<WorkoutEntity>,
    ) {
        deleteByWeekStartDate(weekStartDate)

        if (workouts.isNotEmpty()) {
            insertAll(workouts)
        }
    }

    @Transaction
    suspend fun replaceWorkoutsForDisplayWeek(
        targetStorageWeekStarts: List<LocalDate>,
        targetDisplayDates: List<LocalDate>,
        targetUnassignedStorageWeekStart: LocalDate,
        replacementWorkouts: List<WorkoutEntity>,
    ): List<WorkoutEntity> {
        val targetDateSet = targetDisplayDates.toSet()
        val existing = getWorkoutsForWeekStarts(targetStorageWeekStarts)
        val replaced =
            existing.filter { workout ->
                if (workout.dayOfWeek == null) {
                    workout.weekStartDate == targetUnassignedStorageWeekStart
                } else {
                    workout.weekStartDate.plusDays((workout.dayOfWeek - 1).toLong()) in targetDateSet
                }
            }

        if (replaced.isNotEmpty()) {
            deleteByIds(replaced.map { it.id })
        }

        if (replacementWorkouts.isNotEmpty()) {
            insertAll(replacementWorkouts)
        }

        return replaced
    }

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}
