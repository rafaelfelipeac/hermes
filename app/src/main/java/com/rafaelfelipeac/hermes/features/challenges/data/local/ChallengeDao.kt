@file:Suppress("ArgumentListWrapping", "MaxLineLength")

package com.rafaelfelipeac.hermes.features.challenges.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE lifecycle = 'ACTIVE' ORDER BY updatedAt DESC, id DESC")
    fun observeActiveChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE lifecycle = 'ARCHIVED' ORDER BY updatedAt DESC, id DESC")
    fun observeArchivedChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    fun observeChallenge(id: Long): Flow<ChallengeEntity?>

    @Query(
        "SELECT * FROM challenge_progress_entries WHERE challengeId = :challengeId " +
            "ORDER BY entryDate DESC, occurredAt DESC, id DESC",
    )
    fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntryEntity>>

    @Query("SELECT * FROM challenges WHERE lifecycle = 'ACTIVE' ORDER BY updatedAt DESC, id DESC")
    suspend fun getActiveChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE lifecycle = 'ARCHIVED' ORDER BY updatedAt DESC, id DESC")
    suspend fun getArchivedChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallenge(id: Long): ChallengeEntity?

    @Query(
        "SELECT * FROM challenge_progress_entries WHERE challengeId = :challengeId " +
            "ORDER BY entryDate DESC, occurredAt DESC, id DESC",
    )
    suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntryEntity>

    @Query("SELECT * FROM challenges ORDER BY id ASC")
    suspend fun getAllChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenge_progress_entries ORDER BY challengeId ASC, entryDate ASC, occurredAt ASC, id ASC")
    suspend fun getAllProgressEntries(): List<ChallengeProgressEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ChallengeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>): List<Long>

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET lifecycle = 'ARCHIVED', archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveChallenge(
        id: Long,
        archivedAt: Long,
        updatedAt: Long,
    )

    @Query("UPDATE challenges SET lifecycle = 'ACTIVE', archivedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun reactivateChallenge(
        id: Long,
        updatedAt: Long,
    )

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallenge(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressEntry(entry: ChallengeProgressEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressEntries(entries: List<ChallengeProgressEntryEntity>): List<Long>

    @Update
    suspend fun updateProgressEntry(entry: ChallengeProgressEntryEntity)

    @Query("DELETE FROM challenge_progress_entries WHERE id = :id")
    suspend fun deleteProgressEntry(id: Long)

    @Query("DELETE FROM challenge_progress_entries WHERE challengeId = :challengeId")
    suspend fun deleteProgressEntriesForChallenge(challengeId: Long)

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()

    @Query("DELETE FROM challenge_progress_entries")
    suspend fun deleteAllProgressEntries()

    @Transaction
    suspend fun deleteChallengeAndProgress(challengeId: Long) {
        deleteProgressEntriesForChallenge(challengeId)
        deleteChallenge(challengeId)
    }
}
