@file:Suppress("NestedBlockDepth", "MaxLineLength", "MaximumLineLength", "ArgumentListWrapping", "Wrapping")

package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal object BackupSnapshotValidator {
    private val validDayOfWeekRange = DayOfWeek.MONDAY.value..DayOfWeek.SUNDAY.value

    @Suppress("CyclomaticComplexMethod", "LongMethod", "ReturnCount")
    fun validate(snapshot: BackupSnapshot): ImportBackupError? {
        val challengeIds = snapshot.challenges.map { it.id }.toSet()
        val categoryIds = snapshot.categories.map { it.id }.toSet()
        val personalRecordFamilyIds = snapshot.personalRecordFamilies.map { it.id }.toSet()
        val personalRecordEntryIds = snapshot.personalRecordEntries.map { it.id }.toSet()
        val personalRecordEntriesById = snapshot.personalRecordEntries.associateBy { it.id }

        snapshot.challenges.forEach { challenge ->
            if (challenge.title.isBlank()) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { ChallengeTargetType.valueOf(challenge.targetType) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (challenge.targetQuantity <= 0L) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { ChallengeLifecycle.valueOf(challenge.lifecycle) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            try {
                val startDate = LocalDate.parse(challenge.startDate)
                val endDate = LocalDate.parse(challenge.endDate)
                if (startDate.isAfter(endDate)) return ImportBackupError.INVALID_FIELD_VALUE
                if (challenge.targetType == ChallengeTargetType.DAILY.name) {
                    val periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
                    if (runCatching { Math.multiplyExact(challenge.targetQuantity, periodDays) }.isFailure) {
                        return ImportBackupError.INVALID_FIELD_VALUE
                    }
                }
                Instant.parse(challenge.createdAt)
                Instant.parse(challenge.updatedAt)
                challenge.archivedAt?.let(Instant::parse)
            } catch (_: DateTimeParseException) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            if (
                challenge.lifecycle == ChallengeLifecycle.ACTIVE.name &&
                challenge.archivedAt != null
            ) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            if (
                challenge.lifecycle == ChallengeLifecycle.ARCHIVED.name &&
                challenge.archivedAt == null
            ) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
        }

        val challengeProgressTotals = mutableMapOf<Long, Long>()
        snapshot.challengeProgressEntries.forEach { entry ->
            if (entry.challengeId !in challengeIds) return ImportBackupError.INVALID_REFERENCE
            if (entry.quantity <= 0L) return ImportBackupError.INVALID_FIELD_VALUE
            val currentTotal = challengeProgressTotals[entry.challengeId] ?: 0L
            challengeProgressTotals[entry.challengeId] =
                try {
                    Math.addExact(currentTotal, entry.quantity)
                } catch (_: ArithmeticException) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            try {
                val entryDate = LocalDate.parse(entry.entryDate)
                Instant.parse(entry.occurredAt)
                Instant.parse(entry.createdAt)
                Instant.parse(entry.updatedAt)
                val challenge = snapshot.challenges.first { it.id == entry.challengeId }
                val startDate = LocalDate.parse(challenge.startDate)
                val endDate = LocalDate.parse(challenge.endDate)
                if (entryDate.isBefore(startDate) || entryDate.isAfter(endDate)) {
                    return ImportBackupError.INVALID_FIELD_VALUE
                }
            } catch (_: DateTimeParseException) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
        }

        snapshot.workouts.forEach { workout ->
            if (workout.dayOfWeek != null && workout.dayOfWeek !in validDayOfWeekRange) return ImportBackupError.INVALID_FIELD_VALUE
            workout.timeSlot?.let {
                if (runCatching { TimeSlot.valueOf(it) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            }
            if (runCatching { EventType.valueOf(workout.eventType) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            try {
                LocalDate.parse(workout.weekStartDate)
            } catch (_: DateTimeParseException) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            workout.categoryId?.let { categoryId ->
                if (categoryId !in categoryIds) return ImportBackupError.INVALID_REFERENCE
            }
        }

        snapshot.personalRecordFamilies.forEach { family ->
            if (
                runCatching { PersonalRecordMetricType.valueOf(family.metricType) }.isFailure
            ) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            if (
                runCatching { PersonalRecordUnit.valueOf(family.defaultUnit) }.isFailure
            ) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            if (runCatching {
                    PersonalRecordComparisonRule.valueOf(
                        family.comparisonRule,
                    )
                }.isFailure
            ) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            try {
                Instant.parse(family.createdAt)
                Instant.parse(family.updatedAt)
            } catch (_: DateTimeParseException) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
            family.categoryId?.let { categoryId ->
                if (categoryId !in categoryIds) return ImportBackupError.INVALID_REFERENCE
            }
            family.manualCurrentEntryId?.let { entryId ->
                val referencedEntry = personalRecordEntriesById[entryId]
                if (entryId !in personalRecordEntryIds || referencedEntry?.familyId != family.id) return ImportBackupError.INVALID_REFERENCE
            }
        }

        snapshot.personalRecordEntries.forEach { entry ->
            if (entry.familyId !in personalRecordFamilyIds) return ImportBackupError.INVALID_REFERENCE
            if (!entry.value.isFinite()) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { PersonalRecordUnit.valueOf(entry.unit) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            try {
                LocalDate.parse(entry.recordDate)
                Instant.parse(entry.createdAt)
                Instant.parse(entry.updatedAt)
            } catch (_: DateTimeParseException) {
                return ImportBackupError.INVALID_FIELD_VALUE
            }
        }

        snapshot.settings?.let { settings ->
            if (runCatching { ThemeMode.valueOf(settings.themeMode) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { SlotModePolicy.valueOf(settings.slotModePolicy) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { WeekStartDay.valueOf(settings.weekStartDay) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { DistanceUnit.valueOf(settings.distanceUnit) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { PaceUnit.valueOf(settings.paceUnit) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
            if (runCatching { WeightUnit.valueOf(settings.weightUnit) }.isFailure) return ImportBackupError.INVALID_FIELD_VALUE
        }

        return null
    }
}
