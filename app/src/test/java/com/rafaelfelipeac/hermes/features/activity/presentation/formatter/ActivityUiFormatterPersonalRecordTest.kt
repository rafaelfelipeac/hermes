package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ActivityUiFormatterPersonalRecordTest : ActivityUiFormatterTestFixture() {
    @Test
    fun personalRecordDistanceResult_hasSingleUnitAndSpacedSeparator() {
        val record = personalRecordEntryRecord()
        val metadata =
            mapOf(
                UserActionMetadataKeys.PERSONAL_RECORD_UNIT to "KILOMETER",
                UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE to "42.2",
                UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE to "2026-08-24",
            )

        assertEquals(
            "42.2 km\nAug 24, 2026",
            formatter.buildSubtitle(record, metadata, Locale.US),
        )
    }

    @Test
    fun personalRecordResultTitle_usesSeriesNameWithMetricFallback() {
        val record = personalRecordEntryRecord()

        assertEquals(
            "You added a 5 km PR result.",
            formatter.buildTitle(
                record,
                mapOf(
                    UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE to "5 km",
                    UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "DISTANCE",
                ),
            ),
        )
        assertEquals(
            "You added a Distance PR result.",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "DISTANCE"),
            ),
        )
    }

    @Test
    fun personalRecordMetricParsing_isCaseInsensitiveAndKeepsUnknownValues() {
        val record = personalRecordEntryRecord()

        assertEquals(
            "You added a Distance PR result.",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "distance"),
            ),
        )
        assertEquals(
            "You added a future_metric PR result.",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "future_metric"),
            ),
        )
    }

    @Test
    fun personalRecordTimeResult_hasSpacedSeparator() {
        val record = personalRecordEntryRecord()
        val metadata =
            mapOf(
                UserActionMetadataKeys.PERSONAL_RECORD_UNIT to "SECOND",
                UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE to "2360",
                UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE to "2026-08-24",
            )

        assertEquals(
            "39:20\nAug 24, 2026",
            formatter.buildSubtitle(record, metadata, Locale.US),
        )
    }

    @Test
    fun personalRecordUpdateSubtitle_keepsRawValueAndDateFallbacks() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.UPDATE_PERSONAL_RECORD_ENTRY.name,
                entityType = UserActionEntityType.PERSONAL_RECORD.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            "tempo rápido${NEW_LINE}2026-13-40",
            formatter.buildSubtitle(
                record,
                mapOf(
                    UserActionMetadataKeys.PERSONAL_RECORD_UNIT to "SECOND",
                    UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE to "tempo rápido",
                    UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE to "2026-13-40",
                ),
                Locale.US,
            ),
        )
    }
}
