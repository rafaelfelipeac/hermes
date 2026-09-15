package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class ActivityUiFormatterTest : ActivityUiFormatterTestFixture() {
    @Test
    fun buildTitle_raceEventCreate_usesEventNameWithoutCrashing() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.CREATE_RACE_EVENT.name,
                entityType = UserActionEntityType.RACE_EVENT.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        val title = formatter.buildTitle(record, emptyMap())
        val titleWithMetadata =
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.NEW_DESCRIPTION to "City 10K"),
            )

        assertEquals("You created the event \"untitled\".", title)
        assertEquals("You created the event \"City 10K\".", titleWithMetadata)
    }

    @Test
    fun distanceUnitChange_hasLocalizedTitleAndSubtitle() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.CHANGE_DISTANCE_UNIT.name,
                entityType = UserActionEntityType.SETTINGS.name,
                entityId = null,
                metadata = null,
                timestamp = 0L,
            )
        val metadata =
            mapOf(
                UserActionMetadataKeys.OLD_VALUE to "KILOMETERS",
                UserActionMetadataKeys.NEW_VALUE to "MILES",
            )

        assertEquals("You changed the distance unit.", formatter.buildTitle(record, metadata))
        assertEquals("From \"km\" to \"mi\".", formatter.buildSubtitle(record, metadata, Locale.US))
    }

    @Test
    fun paceCalculatorUse_hasLocalizedActivityTitle() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.USE_PACE_CALCULATOR.name,
                entityType = UserActionEntityType.APP.name,
                entityId = null,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals("You used the pace calculator.", formatter.buildTitle(record, emptyMap()))
    }

    @Test
    fun buildTitle_usesEntitySpecificFallbackBeforeGlobalFallback() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.CREATE_WORKOUT.name,
                entityType = UserActionEntityType.CATEGORY.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            R.string.activity_action_fallback.toString(),
            formatter.buildTitle(record, emptyMap()),
        )
    }

    @Test
    fun buildTitle_usesGlobalFallbackForUnknownActionAndEntity() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = "NOT_A_REAL_ACTION",
                entityType = "NOT_A_REAL_ENTITY",
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            R.string.activity_action_fallback.toString(),
            formatter.buildTitle(record, emptyMap()),
        )
    }

    @Test
    fun shareTrophyTitle_quotesTheTrophyNameAndFallsBackToUnknown() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.SHARE_TROPHY.name,
                entityType = UserActionEntityType.TROPHY.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            "You started sharing the trophy \"Full Time\".",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.TROPHY_NAME to "Full Time"),
            ),
        )
        assertEquals(
            "You started sharing the trophy \"Unknown\".",
            formatter.buildTitle(record, emptyMap()),
        )
    }

    @Test
    fun formatTime_usesRequestedLocaleAndTimeZone() {
        assertEquals(
            "09:30",
            formatter.formatTime(
                timestamp = Instant.parse("2026-09-05T12:30:00Z").toEpochMilli(),
                zoneId = ZoneId.of("America/Sao_Paulo"),
                locale = Locale.forLanguageTag("pt-BR"),
            ),
        )
    }
}
