package com.rafaelfelipeac.hermes.features.progress.presentation

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyFamilyUi
import java.time.ZoneId
import java.util.Locale

private const val RECENT_ACTIVITY_LIMIT = 5

internal fun buildRecentActivities(
    actions: List<UserActionRecord>,
    locale: Locale,
    stringProvider: StringProvider,
): List<ActivityItemUi> {
    val formatter = ActivityUiFormatter(stringProvider)
    val zoneId = ZoneId.systemDefault()

    return actions
        .sortedByDescending { it.timestamp }
        .take(RECENT_ACTIVITY_LIMIT)
        .map { record ->
            val metadata = formatter.parseMetadata(record.metadata)
            ActivityItemUi(
                id = record.id,
                title = formatter.buildTitle(record, metadata),
                subtitle = formatter.buildSubtitle(record, metadata, locale),
                time = formatter.formatTime(record.timestamp, zoneId, locale),
            )
        }
}

internal fun TrophyProgress.toCardUi(): TrophyCardUi {
    return TrophyCardUi(
        stableId = buildTrophyStableId(this),
        trophyId = definition.id,
        family = definition.family.toUi(),
        sortOrder = sortOrder,
        badgeRank = badgeRank,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColorId = categoryColorId,
        currentValue = currentValue,
        target = definition.target,
        isUnlocked = isUnlocked,
        unlockedAt = unlockedAt,
    )
}

private fun buildTrophyStableId(progress: TrophyProgress): String {
    return buildString {
        append(progress.definition.id.name)
        progress.categoryId?.let {
            append('_')
            append(it)
        }
    }
}

private fun TrophyFamily.toUi(): TrophyFamilyUi {
    return when (this) {
        TrophyFamily.CHALLENGES -> TrophyFamilyUi.CHALLENGES
        TrophyFamily.FOLLOW_THROUGH -> TrophyFamilyUi.FOLLOW_THROUGH
        TrophyFamily.CONSISTENCY -> TrophyFamilyUi.CONSISTENCY
        TrophyFamily.ADAPTABILITY -> TrophyFamilyUi.ADAPTABILITY
        TrophyFamily.MOMENTUM -> TrophyFamilyUi.MOMENTUM
        TrophyFamily.BUILDER -> TrophyFamilyUi.BUILDER
        TrophyFamily.RACE_EVENTS -> TrophyFamilyUi.RACE_EVENTS
        TrophyFamily.PERSONAL_RECORDS -> TrophyFamilyUi.PERSONAL_RECORDS
        TrophyFamily.CATEGORIES -> TrophyFamilyUi.CATEGORIES
    }
}
