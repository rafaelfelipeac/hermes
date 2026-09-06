package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal const val TROPHIES_SHARE_INTENT_TYPE = "text/plain"
internal const val TROPHIES_DEBUG_PACKAGE_SUFFIX = ".dev"
internal const val TROPHIES_PREVIEW_COUNT = 6
internal const val TROPHIES_GRID_COLUMNS = 2
internal const val TROPHIES_SAMPLE_FULL_TIME_TARGET = 4
internal const val TROPHIES_SAMPLE_COMEBACK_TARGET = 4
internal const val TROPHIES_SAMPLE_PODIUM_TARGET = 10
internal const val TROPHIES_EMPTY_STATE_TAG = "trophies_empty_state"
internal const val TROPHIES_DETAIL_DIALOG_TAG = "trophies_detail_dialog"
internal const val TROPHIES_OVERVIEW_LIST_TAG = "trophies_overview_list"
internal const val TROPHIES_FAMILY_LIST_TAG_PREFIX = "trophies_family_list_"
internal const val TROPHIES_VIEW_ALL_TAG_PREFIX = "trophies_view_all_"
internal const val TROPHIES_FAMILY_DETAIL_TAG_PREFIX = "trophies_family_detail_"
private const val TROPHIES_QUOTE = "\""

internal fun previewTrophies(trophies: List<TrophyCardUi>): List<TrophyCardUi> {
    return trophies.take(TROPHIES_PREVIEW_COUNT)
}

internal fun trophyCardSemanticsLabel(
    name: String,
    categoryName: String?,
    conditionLabel: String,
): String {
    return listOfNotNull(name, categoryName, conditionLabel).joinToString(separator = ". ")
}

internal fun trophyStateLineText(
    isUnlocked: Boolean,
    unlockedDateText: String?,
    conditionLabel: String,
): String {
    return if (isUnlocked) {
        unlockedDateText.orEmpty()
    } else {
        conditionLabel
    }
}

internal fun trophyProgressFraction(
    currentValue: Int,
    target: Int,
): Float {
    return currentValue.coerceAtMost(target).toFloat() / target.toFloat()
}

internal fun formatUnlockedDateLabel(
    unlockedAt: Long,
    currentLocale: Locale,
): String {
    return DateFormat.getDateInstance(DateFormat.MEDIUM, currentLocale).format(Date(unlockedAt))
}

internal fun formatUnlockedDateCompactLabel(
    unlockedAt: Long,
    currentLocale: Locale,
): String {
    return DateFormat.getDateInstance(DateFormat.SHORT, currentLocale).format(Date(unlockedAt))
}

internal fun resolveAppPackageName(
    applicationId: String,
    isDebug: Boolean,
): String {
    return if (isDebug && applicationId.endsWith(TROPHIES_DEBUG_PACKAGE_SUFFIX)) {
        applicationId.removeSuffix(TROPHIES_DEBUG_PACKAGE_SUFFIX)
    } else {
        applicationId
    }
}

internal fun quotedShareValue(value: String): String = TROPHIES_QUOTE + value + TROPHIES_QUOTE

internal fun buildTrophyShareMessage(
    shareTitle: String,
    shareDescription: String,
    unlockedDateText: String?,
    shareCta: String,
): String {
    return listOfNotNull(
        shareTitle,
        shareDescription,
        unlockedDateText,
        EMPTY,
        shareCta,
    ).joinToString(separator = NEW_LINE)
}
