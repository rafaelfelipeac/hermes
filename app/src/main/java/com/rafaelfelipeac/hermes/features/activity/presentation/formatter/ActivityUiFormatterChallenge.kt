package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.util.Locale

internal class ActivityUiFormatterChallenge(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun buildChallengeTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label = challengeTitleLabel(metadata)
        val quotedLabel = label?.let { shared.quoteValue(it) ?: it }

        if (actionType.isChallengeProgressAction() && metadata.isChallengeCompletionTransition()) {
            return challengeTitleOrFallback(
                quotedLabel,
                R.string.activity_action_complete_challenge_named,
                R.string.activity_action_complete_challenge,
            )
        }

        return when (actionType) {
            UserActionType.CREATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_create_challenge_named,
                    R.string.activity_action_create_challenge,
                )

            UserActionType.UPDATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_update_challenge_named,
                    R.string.activity_action_update_challenge,
                )

            UserActionType.ARCHIVE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_archive_challenge_named,
                    R.string.activity_action_archive_challenge,
                )

            UserActionType.REACTIVATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_reactivate_challenge_named,
                    R.string.activity_action_reactivate_challenge,
                )

            UserActionType.DELETE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_delete_challenge_named,
                    R.string.activity_action_delete_challenge,
                )

            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_create_challenge_progress_entry_named,
                    R.string.activity_action_create_challenge_progress_entry,
                )

            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_update_challenge_progress_entry_named,
                    R.string.activity_action_update_challenge_progress_entry,
                )

            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_delete_challenge_progress_entry_named,
                    R.string.activity_action_delete_challenge_progress_entry,
                )

            UserActionType.RESTORE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_restore_challenge_named,
                    R.string.activity_action_restore_challenge,
                )

            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_restore_challenge_progress_entry_named,
                    R.string.activity_action_restore_challenge_progress_entry,
                )

            else -> null
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun buildChallengeSubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val targetQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_NEW_VALUE]
                    ?: metadata[UserActionMetadataKeys.NEW_VALUE],
                currentLocale,
            )
        val targetType = challengeTargetType(metadata[UserActionMetadataKeys.CHALLENGE_TARGET_TYPE])
        val oldTargetType = challengeTargetType(metadata[UserActionMetadataKeys.OLD_TYPE])
        val newTargetType =
            challengeTargetType(metadata[UserActionMetadataKeys.NEW_TYPE])
                ?: targetType
        val oldQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_OLD_VALUE]
                    ?: metadata[UserActionMetadataKeys.OLD_VALUE],
                currentLocale,
            )
        val startDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_START_DATE], currentLocale)
        val endDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_END_DATE], currentLocale)
        val progressQuantity =
            formatChallengeQuantity(metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY], currentLocale)
        val progressDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE], currentLocale)
        val oldProgressQuantity =
            formatChallengeQuantity(metadata[UserActionMetadataKeys.CHALLENGE_OLD_VALUE], currentLocale)
        val newProgressQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_NEW_VALUE]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY],
                currentLocale,
            )
        val oldProgressDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_OLD_DATE], currentLocale)
        val newProgressDate =
            formatChallengeDate(
                metadata[UserActionMetadataKeys.CHALLENGE_NEW_DATE]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE],
                currentLocale,
            )
        val recovered = metadata[UserActionMetadataKeys.CHALLENGE_RECOVERED]?.toBooleanStrictOrNull() == true
        val oldCategory = metadata[UserActionMetadataKeys.OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val newCategory = metadata[UserActionMetadataKeys.NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val category =
            metadata[UserActionMetadataKeys.CHALLENGE_CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: newCategory
                ?: oldCategory

        return when (actionType) {
            UserActionType.CREATE_CHALLENGE,
            -> {
                val details =
                    buildList {
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(shared.activitySubtitleSeparator())
            }

            UserActionType.UPDATE_CHALLENGE -> {
                val details =
                    buildList {
                        challengeTargetChangeSubtitle(
                            oldTargetType = oldTargetType ?: targetType,
                            oldQuantity = oldQuantity,
                            newTargetType = newTargetType,
                            newQuantity = targetQuantity,
                        )?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        if (oldCategory.isNullOrBlank() || newCategory.isNullOrBlank() || oldCategory == newCategory) {
                            challengeCategorySubtitle(category)?.let(::add)
                        } else {
                            val oldQuoted = shared.quoteValue(oldCategory)
                            val newQuoted = shared.quoteValue(newCategory)
                            if (oldQuoted != null && newQuoted != null) {
                                add(
                                    stringProvider.get(
                                        R.string.activity_subtitle_challenge_category_change,
                                        oldQuoted,
                                        newQuoted,
                                    ),
                                )
                            }
                        }
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(shared.activitySubtitleSeparator())
            }

            UserActionType.ARCHIVE_CHALLENGE,
            UserActionType.REACTIVATE_CHALLENGE,
            UserActionType.DELETE_CHALLENGE,
            -> {
                val details =
                    buildList {
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(shared.activitySubtitleSeparator())
            }

            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
            -> {
                val details =
                    buildList {
                        challengeProgressActionSubtitle(
                            actionType = actionType,
                            values =
                                ChallengeProgressSubtitleValues(
                                    progressQuantity = progressQuantity,
                                    progressDate = progressDate,
                                    oldProgressQuantity = oldProgressQuantity,
                                    oldProgressDate = oldProgressDate,
                                    newProgressQuantity = newProgressQuantity,
                                    newProgressDate = newProgressDate,
                                ),
                        )?.let(::add)
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        if (recovered) {
                            add(stringProvider.get(R.string.activity_subtitle_challenge_recovered))
                        }
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(shared.activitySubtitleSeparator())
            }

            UserActionType.RESTORE_CHALLENGE -> null
            else -> null
        }
    }

    private data class ChallengeProgressSubtitleValues(
        val progressQuantity: String?,
        val progressDate: String?,
        val oldProgressQuantity: String?,
        val oldProgressDate: String?,
        val newProgressQuantity: String?,
        val newProgressDate: String?,
    ) {
        val hasPreviousProgress: Boolean =
            !oldProgressQuantity.isNullOrBlank() ||
                !oldProgressDate.isNullOrBlank()
    }

    private fun challengeProgressActionSubtitle(
        actionType: UserActionType,
        values: ChallengeProgressSubtitleValues,
    ): String? {
        return when (actionType) {
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_added,
                    values.progressQuantity,
                    values.progressDate,
                )

            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY -> {
                if (values.hasPreviousProgress) {
                    stringProvider.get(
                        R.string.activity_subtitle_challenge_progress_updated,
                        values.oldProgressQuantity.orEmpty(),
                        values.oldProgressDate.orEmpty(),
                        values.newProgressQuantity.orEmpty(),
                        values.newProgressDate.orEmpty(),
                    )
                } else {
                    challengeProgressQuantityDateSubtitle(
                        R.string.activity_subtitle_challenge_progress_updated_current,
                        values.newProgressQuantity,
                        values.newProgressDate,
                    )
                }
            }

            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_deleted,
                    values.progressQuantity,
                    values.progressDate,
                )

            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_restored,
                    values.progressQuantity,
                    values.progressDate,
                )

            else -> null
        }
    }

    private fun challengeProgressQuantityDateSubtitle(
        subtitleRes: Int,
        progressQuantity: String?,
        progressDate: String?,
    ): String? {
        if (progressQuantity.isNullOrBlank() && progressDate.isNullOrBlank()) return null
        return stringProvider.get(
            subtitleRes,
            progressQuantity.orEmpty(),
            progressDate.orEmpty(),
        )
    }

    private fun challengeTitleOrFallback(
        quotedLabel: String?,
        namedTitleRes: Int,
        fallbackTitleRes: Int,
    ): String {
        return if (quotedLabel == null) {
            stringProvider.get(fallbackTitleRes)
        } else {
            stringProvider.get(namedTitleRes, quotedLabel)
        }
    }

    private fun UserActionType?.isChallengeProgressAction(): Boolean {
        return this in challengeProgressActions
    }

    private fun Map<String, String>.isChallengeCompletionTransition(): Boolean {
        return this[UserActionMetadataKeys.WAS_COMPLETED] == false.toString() &&
            this[UserActionMetadataKeys.IS_COMPLETED] == true.toString()
    }

    private fun challengeTitleLabel(metadata: Map<String, String>): String? {
        return metadata[UserActionMetadataKeys.CHALLENGE_TITLE]
            ?.takeIf { it.isNotBlank() }
    }

    private fun challengeTargetType(raw: String?): ChallengeTargetType? {
        return raw
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ChallengeTargetType.valueOf(it) }.getOrNull() }
    }

    private fun challengeTargetSubtitle(
        targetType: ChallengeTargetType?,
        quantity: String?,
    ): String? {
        if (quantity.isNullOrBlank()) return null
        return when (targetType ?: ChallengeTargetType.DAILY) {
            ChallengeTargetType.DAILY ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_daily, quantity)
            ChallengeTargetType.TOTAL ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_total, quantity)
        }
    }

    private fun challengeTargetChangeSubtitle(
        oldTargetType: ChallengeTargetType?,
        oldQuantity: String?,
        newTargetType: ChallengeTargetType?,
        newQuantity: String?,
    ): String? {
        val oldValue = challengeTargetValue(oldTargetType, oldQuantity)
        val newValue = challengeTargetValue(newTargetType, newQuantity)
        if (oldValue.isNullOrBlank() || newValue.isNullOrBlank() || oldValue == newValue) return null
        return stringProvider.get(
            R.string.activity_subtitle_challenge_target_change,
            oldValue,
            newValue,
        )
    }

    private fun challengeTargetValue(
        targetType: ChallengeTargetType?,
        quantity: String?,
    ): String? {
        if (quantity.isNullOrBlank()) return null
        return when (targetType ?: ChallengeTargetType.DAILY) {
            ChallengeTargetType.DAILY ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_value_daily, quantity)
            ChallengeTargetType.TOTAL ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_value_total, quantity)
        }
    }

    private fun challengeDateSubtitle(
        startDate: String?,
        endDate: String?,
    ): String? {
        if (startDate == null && endDate == null) return null
        return stringProvider.get(
            R.string.activity_subtitle_challenge_dates,
            startDate.orEmpty(),
            endDate.orEmpty(),
        )
    }

    private fun challengeCategorySubtitle(category: String?): String? {
        return category
            ?.let(shared::quoteValue)
            ?.let { quotedCategory ->
                stringProvider.get(
                    R.string.activity_subtitle_challenge_category,
                    quotedCategory,
                )
            }
    }

    private fun formatChallengeQuantity(
        raw: String?,
        currentLocale: Locale,
    ): String? {
        val scaled = raw?.toLongOrNull() ?: return null
        return ChallengeQuantity.format(scaled, currentLocale)
    }

    private fun formatChallengeDate(
        raw: String?,
        currentLocale: Locale,
    ): String? {
        return shared.formatDate(raw, currentLocale)
    }

    private val challengeProgressActions =
        setOf(
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
        )
}
