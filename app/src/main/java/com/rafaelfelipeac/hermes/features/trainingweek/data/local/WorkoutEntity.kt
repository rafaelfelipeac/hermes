package com.rafaelfelipeac.hermes.features.trainingweek.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStartDate: LocalDate,
    val dayOfWeek: Int?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
