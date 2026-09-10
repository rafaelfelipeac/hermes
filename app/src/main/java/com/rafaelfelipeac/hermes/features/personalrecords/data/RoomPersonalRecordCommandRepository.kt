package com.rafaelfelipeac.hermes.features.personalrecords.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.PERSONAL_RECORD
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordValueNormalizer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.command.PersonalRecordCommandRepository
import com.rafaelfelipeac.hermes.features.personalrecords.domain.command.PersonalRecordCommandResult
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPersonalRecordCommandRepository
    @Inject
    constructor(
        private val database: HermesDatabase,
        private val userActionLogger: UserActionLogger,
        private val clock: Clock,
    ) : PersonalRecordCommandRepository {
        private val categoryDao = database.categoryDao()
        private val personalRecordDao = database.personalRecordDao()

        override suspend fun deleteEntry(entryId: Long): PersonalRecordCommandResult {
            return database.withTransaction {
                val entry =
                    personalRecordDao.getEntry(entryId)
                        ?: return@withTransaction PersonalRecordCommandResult.NoChange
                val family =
                    personalRecordDao.getFamily(entry.familyId)
                        ?: return@withTransaction PersonalRecordCommandResult.NoChange
                val categoryName = family.categoryId?.let { categoryDao.getCategory(it)?.name }.orEmpty()

                personalRecordDao.deleteEntry(entryId)

                if (family.manualCurrentEntryId == entryId) {
                    personalRecordDao.clearManualCurrentEntry(
                        familyId = family.id,
                        updatedAt = Instant.now(clock).toEpochMilli(),
                    )
                }

                userActionLogger.log(
                    actionType = DELETE_PERSONAL_RECORD_ENTRY,
                    entityType = PERSONAL_RECORD,
                    entityId = entryId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                            PERSONAL_RECORD_FAMILY_ID to entry.familyId.toString(),
                            PERSONAL_RECORD_FAMILY_TITLE to family.title,
                            PERSONAL_RECORD_CATEGORY_ID to family.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to categoryName,
                            PERSONAL_RECORD_METRIC_TYPE to family.metricType.name,
                            PERSONAL_RECORD_UNIT to entry.unit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to family.comparisonRule.name,
                            PERSONAL_RECORD_RECORD_DATE to entry.recordDate.toString(),
                            PERSONAL_RECORD_NEW_VALUE to entry.value.toString(),
                            PERSONAL_RECORD_NORMALIZED_VALUE to
                                PersonalRecordValueNormalizer
                                    .normalize(entry.value, entry.unit)
                                    .toString(),
                        ),
                )

                PersonalRecordCommandResult.Changed
            }
        }
    }
