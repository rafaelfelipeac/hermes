package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import java.time.Instant
import java.time.LocalDate

internal fun WorkoutEntity.toBackupRecord(): BackupWorkoutRecord {
    return BackupWorkoutRecord(
        id = id,
        weekStartDate = weekStartDate.toString(),
        dayOfWeek = dayOfWeek,
        timeSlot = timeSlot,
        sortOrder = sortOrder,
        eventType = eventType,
        type = type,
        description = description,
        isCompleted = isCompleted,
        categoryId = categoryId,
    )
}

internal fun CategoryEntity.toBackupRecord(): BackupCategoryRecord {
    return BackupCategoryRecord(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}

internal fun PersonalRecordFamilyEntity.toBackupRecord(): BackupPersonalRecordFamilyRecord {
    return BackupPersonalRecordFamilyRecord(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = metricType.name,
        defaultUnit = defaultUnit.name,
        comparisonRule = comparisonRule.name,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )
}

internal fun PersonalRecordEntryEntity.toBackupRecord(): BackupPersonalRecordEntryRecord {
    return BackupPersonalRecordEntryRecord(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit.name,
        customUnitLabel = customUnitLabel,
        recordDate = recordDate.toString(),
        note = note,
        createdAt = Instant.ofEpochMilli(createdAt).toString(),
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )
}

internal fun UserActionEntity.toBackupRecord(): BackupUserActionRecord {
    return BackupUserActionRecord(
        id = id,
        actionType = actionType,
        entityType = entityType,
        entityId = entityId,
        metadata = metadata,
        timestamp = timestamp,
    )
}

internal fun BackupChallengeRecord.toEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = id,
        title = title,
        description = description,
        targetType = ChallengeTargetType.valueOf(targetType),
        targetQuantity = targetQuantity,
        categoryId = categoryId,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        lifecycle = ChallengeLifecycle.valueOf(lifecycle),
        archivedAt = archivedAt?.let { Instant.parse(it).toEpochMilli() },
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

internal fun BackupChallengeProgressEntryRecord.toEntity(): ChallengeProgressEntryEntity {
    return ChallengeProgressEntryEntity(
        id = id,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = LocalDate.parse(entryDate),
        occurredAt = Instant.parse(occurredAt).toEpochMilli(),
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

internal fun BackupCategoryRecord.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = isHidden,
        isSystem = isSystem,
    )
}

internal fun BackupPersonalRecordFamilyRecord.toEntity(): PersonalRecordFamilyEntity {
    return PersonalRecordFamilyEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = PersonalRecordMetricType.valueOf(metricType),
        defaultUnit = PersonalRecordUnit.valueOf(defaultUnit),
        comparisonRule = PersonalRecordComparisonRule.valueOf(comparisonRule),
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

internal fun BackupPersonalRecordEntryRecord.toEntity(): PersonalRecordEntryEntity {
    return PersonalRecordEntryEntity(
        id = id,
        familyId = familyId,
        value = value,
        unit = PersonalRecordUnit.valueOf(unit),
        customUnitLabel = customUnitLabel,
        recordDate = LocalDate.parse(recordDate),
        note = note,
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}

internal fun BackupWorkoutRecord.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        weekStartDate = LocalDate.parse(weekStartDate),
        dayOfWeek = dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = eventType == EventType.REST.name,
        eventType = eventType,
        timeSlot = timeSlot,
        categoryId = categoryId,
        sortOrder = sortOrder,
    )
}

internal fun BackupUserActionRecord.toEntity(): UserActionEntity {
    return UserActionEntity(
        id = id,
        actionType = actionType,
        entityType = entityType,
        entityId = entityId,
        metadata = metadata,
        timestamp = timestamp,
    )
}
