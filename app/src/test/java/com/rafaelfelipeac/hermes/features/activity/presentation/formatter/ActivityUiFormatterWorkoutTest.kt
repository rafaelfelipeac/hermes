package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ActivityUiFormatterWorkoutTest : ActivityUiFormatterTestFixture() {
    @Test
    fun moveWorkoutSubtitle_usesSplitLinesAndNormalizesHistoricDayTokens() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.MOVE_WORKOUT_BETWEEN_DAYS.name,
                entityType = UserActionEntityType.WORKOUT.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )
        val metadata =
            mapOf(
                UserActionMetadataKeys.WEEK_START_DATE to "2026-09-01",
                UserActionMetadataKeys.OLD_DAY_OF_WEEK to "mon-day",
                UserActionMetadataKeys.NEW_DAY_OF_WEEK to "3",
                UserActionMetadataKeys.OLD_TIME_SLOT to "MORNING",
                UserActionMetadataKeys.NEW_TIME_SLOT to UserActionMetadataValues.UNPLANNED,
            )

        assertEquals(
            "Week of Sep 1, 2026.${NEW_LINE}From \"Monday | Morning\" to \"Wednesday\".",
            formatter.buildSubtitle(record, metadata, Locale.US),
        )
    }

    @Test
    fun reorderWorkoutSubtitle_omitsSameDayTransition() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.REORDER_WORKOUT.name,
                entityType = UserActionEntityType.WORKOUT.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            null,
            formatter.buildSubtitle(
                record,
                mapOf(
                    UserActionMetadataKeys.OLD_DAY_OF_WEEK to "2",
                    UserActionMetadataKeys.NEW_DAY_OF_WEEK to "Tue",
                ),
                Locale.US,
            ),
        )
    }
}
