@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.challenges.data

import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeDateBounds
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl
    @Inject
    constructor(
        private val challengeDao: ChallengeDao,
    ) : ChallengeRepository {
        override fun observeActiveChallenges(): Flow<List<Challenge>> {
            return challengeDao.observeActiveChallenges().map { entities -> entities.map { it.toDomain() } }
        }

        override fun observeArchivedChallenges(): Flow<List<Challenge>> {
            return challengeDao.observeArchivedChallenges().map { entities -> entities.map { it.toDomain() } }
        }

        override fun observeChallenge(id: Long): Flow<Challenge?> {
            return challengeDao.observeChallenge(id).map { it?.toDomain() }
        }

        override fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntry>> {
            return challengeDao.observeProgressEntries(challengeId).map { entities -> entities.map { it.toDomain() } }
        }

        override suspend fun getActiveChallenges(): List<Challenge> {
            return challengeDao.getActiveChallenges().map { it.toDomain() }
        }

        override suspend fun getArchivedChallenges(): List<Challenge> {
            return challengeDao.getArchivedChallenges().map { it.toDomain() }
        }

        override suspend fun getChallenge(id: Long): Challenge? {
            return challengeDao.getChallenge(id)?.toDomain()
        }

        override suspend fun getChallengeDateBounds(id: Long): ChallengeDateBounds? {
            return getChallenge(id)?.let { ChallengeDateBounds(it.startDate, it.endDate) }
        }

        override suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntry> {
            return challengeDao.getProgressEntries(challengeId).map { it.toDomain() }
        }

        override suspend fun getAllChallenges(): List<Challenge> {
            return challengeDao.getAllChallenges().map { it.toDomain() }
        }

        override suspend fun getAllProgressEntries(): List<ChallengeProgressEntry> {
            return challengeDao.getAllProgressEntries().map { it.toDomain() }
        }

        override suspend fun insertChallenge(challenge: Challenge): Long {
            return challengeDao.insertChallenge(challenge.toEntity())
        }

        override suspend fun updateChallenge(challenge: Challenge) {
            challengeDao.updateChallenge(challenge.toEntity())
        }

        override suspend fun archiveChallenge(
            id: Long,
            archivedAt: Instant,
        ) {
            challengeDao.archiveChallenge(
                id = id,
                archivedAt = archivedAt.toEpochMilli(),
                updatedAt = archivedAt.toEpochMilli(),
            )
        }

        override suspend fun reactivateChallenge(id: Long) {
            val now = Instant.now().toEpochMilli()
            challengeDao.reactivateChallenge(
                id = id,
                updatedAt = now,
            )
        }

        override suspend fun deleteChallenge(id: Long) {
            challengeDao.deleteChallengeAndProgress(id)
        }

        override suspend fun insertProgressEntry(entry: ChallengeProgressEntry): Long {
            return challengeDao.insertProgressEntry(entry.toEntity())
        }

        override suspend fun restoreProgressEntry(entry: ChallengeProgressEntry): Long {
            return challengeDao.insertProgressEntry(entry.toEntity())
        }

        override suspend fun updateProgressEntry(entry: ChallengeProgressEntry) {
            challengeDao.updateProgressEntry(entry.toEntity())
        }

        override suspend fun deleteProgressEntry(id: Long) {
            challengeDao.deleteProgressEntry(id)
        }

        override suspend fun replaceChallenges(challenges: List<Challenge>) {
            challengeDao.deleteAllChallenges()
            if (challenges.isNotEmpty()) {
                challengeDao.insertChallenges(challenges.map { it.toEntity() })
            }
        }

        override suspend fun replaceProgressEntries(entries: List<ChallengeProgressEntry>) {
            challengeDao.deleteAllProgressEntries()
            if (entries.isNotEmpty()) {
                challengeDao.insertProgressEntries(entries.map { it.toEntity() })
            }
        }

        override suspend fun deleteAllChallenges() {
            challengeDao.deleteAllChallenges()
        }

        override suspend fun deleteAllProgressEntries() {
            challengeDao.deleteAllProgressEntries()
        }
    }

private fun ChallengeEntity.toDomain(): Challenge {
    return Challenge(
        id = id,
        title = title,
        description = description,
        targetQuantity = targetQuantity,
        unit = unit,
        startDate = startDate,
        endDate = endDate,
        lifecycle = lifecycle,
        archivedAt = archivedAt?.let(Instant::ofEpochMilli),
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
    )
}

private fun ChallengeProgressEntryEntity.toDomain(): ChallengeProgressEntry {
    return ChallengeProgressEntry(
        id = id,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = entryDate,
        occurredAt = Instant.ofEpochMilli(occurredAt),
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
    )
}

private fun Challenge.toEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = id,
        title = title,
        description = description,
        targetQuantity = targetQuantity,
        unit = unit,
        startDate = startDate,
        endDate = endDate,
        lifecycle = lifecycle,
        archivedAt = archivedAt?.toEpochMilli(),
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}

private fun ChallengeProgressEntry.toEntity(): ChallengeProgressEntryEntity {
    return ChallengeProgressEntryEntity(
        id = id,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = entryDate,
        occurredAt = occurredAt.toEpochMilli(),
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}
