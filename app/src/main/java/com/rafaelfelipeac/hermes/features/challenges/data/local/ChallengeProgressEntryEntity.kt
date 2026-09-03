package com.rafaelfelipeac.hermes.features.challenges.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

private const val CHALLENGE_PROGRESS_ENTRY_TABLE_NAME = "challenge_progress_entries"

@Entity(
    tableName = CHALLENGE_PROGRESS_ENTRY_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["challengeId", "entryDate", "occurredAt", "id"]),
    ],
)
data class ChallengeProgressEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val challengeId: Long,
    val quantity: Long,
    val entryDate: LocalDate,
    val occurredAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
