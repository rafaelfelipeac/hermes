package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoChallengeSeeder
    @Inject
    constructor(
        private val challengeRepository: ChallengeRepository,
        private val stringProvider: StringProvider,
    ) {
        suspend fun seed(
            today: LocalDate,
            clearExisting: Boolean = true,
        ) {
            if (clearExisting) {
                challengeRepository.deleteAllProgressEntries()
                challengeRepository.deleteAllChallenges()
            }

            val zoneId = ZoneId.systemDefault()
            val now = Instant.now()

            buildChallengeScenarios(
                stringProvider = stringProvider,
                today = today,
                now = now,
            ).forEach { scenario ->
                val challengeId = challengeRepository.insertChallenge(scenario.challenge)
                val baseInstant = scenario.challenge.createdAt

                scenario.entries.forEachIndexed { index, (quantity, entryDate) ->
                    val occurredAt =
                        entryDate
                            .atStartOfDay(zoneId)
                            .plusHours(8 + index.toLong())
                            .toInstant()
                    challengeRepository.insertProgressEntry(
                        buildChallengeProgressEntry(
                            challengeId = challengeId,
                            quantity = quantity,
                            entryDate = entryDate,
                            occurredAt = occurredAt,
                            baseInstant = baseInstant,
                        ),
                    )
                }
            }
        }
    }
