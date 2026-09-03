package com.rafaelfelipeac.hermes.features.challenges.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.LocalDate

private const val CHALLENGE_TABLE_NAME = "challenges"

@Entity(
    tableName = CHALLENGE_TABLE_NAME,
    indices = [
        Index(value = ["lifecycle"]),
        Index(value = ["updatedAt"]),
        Index(value = ["categoryId"]),
    ],
)
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryId: Long?,
    val title: String,
    val description: String?,
    val targetType: ChallengeTargetType,
    val targetQuantity: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val lifecycle: ChallengeLifecycle,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
