@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.challenges.domain.repository

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeDateBounds
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ChallengeRepository {
    fun observeActiveChallenges(): Flow<List<Challenge>>

    fun observeArchivedChallenges(): Flow<List<Challenge>>

    fun observeChallenge(id: Long): Flow<Challenge?>

    fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntry>>

    suspend fun getActiveChallenges(): List<Challenge>

    suspend fun getArchivedChallenges(): List<Challenge>

    suspend fun getChallenge(id: Long): Challenge?

    suspend fun getChallengeDateBounds(id: Long): ChallengeDateBounds?

    suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntry>

    suspend fun getAllChallenges(): List<Challenge>

    suspend fun getAllProgressEntries(): List<ChallengeProgressEntry>

    suspend fun insertChallenge(challenge: Challenge): Long

    suspend fun updateChallenge(challenge: Challenge)

    suspend fun archiveChallenge(
        id: Long,
        archivedAt: Instant,
    )

    suspend fun reactivateChallenge(id: Long)

    suspend fun deleteChallenge(id: Long)

    suspend fun insertProgressEntry(entry: ChallengeProgressEntry): Long

    suspend fun restoreProgressEntry(entry: ChallengeProgressEntry): Long

    suspend fun updateProgressEntry(entry: ChallengeProgressEntry)

    suspend fun deleteProgressEntry(id: Long)

    suspend fun replaceChallenges(challenges: List<Challenge>)

    suspend fun replaceProgressEntries(entries: List<ChallengeProgressEntry>)

    suspend fun deleteAllChallenges()

    suspend fun deleteAllProgressEntries()
}
