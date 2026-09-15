@file:Suppress("LongMethod")

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoPersonalRecordSeeder
    @Inject
    constructor(
        private val personalRecordDao: PersonalRecordDao,
        private val userActionDao: UserActionDao,
        private val stringProvider: StringProvider,
    ) {
        suspend fun seed(today: LocalDate) {
            val zoneId = ZoneId.systemDefault()

            personalRecordSeeds(stringProvider).forEachIndexed { sortOrder, seed ->
                val familyCreatedAt =
                    today
                        .minusDays(seed.entries.maxOf { it.daysAgo } + 1)
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli()
                val family =
                    PersonalRecordFamilyEntity(
                        categoryId = seed.categoryId,
                        title = seed.title,
                        metricType = seed.metricType,
                        defaultUnit = seed.unit,
                        comparisonRule = seed.comparisonRule,
                        manualCurrentEntryId = null,
                        sortOrder = sortOrder,
                        createdAt = familyCreatedAt,
                        updatedAt = System.currentTimeMillis(),
                    )
                val familyId = personalRecordDao.insertFamily(family)

                userActionDao.insert(
                    createPersonalRecordFamilyAction(
                        stringProvider = stringProvider,
                        familyId = familyId,
                        seed = seed,
                        timestamp = familyCreatedAt,
                    ),
                )

                val entryIds =
                    seed.entries.map { entrySeed ->
                        val recordDate = today.minusDays(entrySeed.daysAgo)
                        val timestamp =
                            recordDate
                                .atStartOfDay(zoneId)
                                .plusHours(DEMO_PERSONAL_RECORD_ENTRY_HOUR)
                                .toInstant()
                                .toEpochMilli()
                        val entryId =
                            personalRecordDao.insertEntry(
                                PersonalRecordEntryEntity(
                                    familyId = familyId,
                                    value = entrySeed.value,
                                    unit = seed.unit,
                                    customUnitLabel = seed.customUnitLabel,
                                    recordDate = recordDate,
                                    note = null,
                                    createdAt = timestamp,
                                    updatedAt = timestamp,
                                ),
                            )

                        userActionDao.insert(
                            createPersonalRecordEntryAction(
                                stringProvider = stringProvider,
                                familyId = familyId,
                                entryId = entryId,
                                seed = seed,
                                entrySeed = entrySeed,
                                recordDate = recordDate,
                                timestamp = timestamp,
                            ),
                        )
                        entryId
                    }

                seed.manualCurrentEntryIndex?.let { selectedIndex ->
                    personalRecordDao.updateFamily(
                        family.copy(
                            id = familyId,
                            manualCurrentEntryId = entryIds[selectedIndex],
                        ),
                    )
                }
            }
        }
    }
