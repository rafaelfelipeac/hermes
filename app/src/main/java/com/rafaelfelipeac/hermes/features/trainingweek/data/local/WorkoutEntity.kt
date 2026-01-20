package com.rafaelfelipeac.hermes.features.trainingweek.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

private const val WORKOUT_TABLE_NAME = "workouts"
private const val WORKOUT_SORT_ORDER_COLUMN = "sort_order"
private const val DEFAULT_WORKOUT_ID = 0L

@Entity(tableName = WORKOUT_TABLE_NAME)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = DEFAULT_WORKOUT_ID,
    val weekStartDate: LocalDate,
    val dayOfWeek: Int?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    @ColumnInfo(name = WORKOUT_SORT_ORDER_COLUMN)
    val sortOrder: Int,
)
