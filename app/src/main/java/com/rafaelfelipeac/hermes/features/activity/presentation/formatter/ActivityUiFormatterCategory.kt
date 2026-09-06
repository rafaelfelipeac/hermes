package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

internal class ActivityUiFormatterCategory(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    fun buildCategoryTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label = buildCategoryLabel(metadata)

        return when (actionType) {
            UserActionType.CREATE_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_create_category,
                    shared.quoteValue(label) ?: label,
                )

            UserActionType.UPDATE_CATEGORY_NAME ->
                stringProvider.get(R.string.activity_action_update_category_name)

            UserActionType.UPDATE_CATEGORY_COLOR ->
                stringProvider.get(
                    R.string.activity_action_update_category_color,
                    shared.quoteValue(label) ?: label,
                )

            UserActionType.UPDATE_CATEGORY_VISIBILITY ->
                stringProvider.get(
                    R.string.activity_action_update_category_visibility,
                    shared.quoteValue(label) ?: label,
                )

            UserActionType.REORDER_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_reorder_category,
                    shared.quoteValue(label) ?: label,
                )

            UserActionType.DELETE_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_delete_category,
                    shared.quoteValue(label) ?: label,
                )

            UserActionType.RESTORE_DEFAULT_CATEGORIES ->
                stringProvider.get(R.string.categories_restore_defaults)

            else -> null
        }
    }

    fun buildWorkoutCategorySubtitle(metadata: Map<String, String>): String? {
        return metadata[UserActionMetadataKeys.CATEGORY_NAME]
            ?.takeIf { it.isNotBlank() }
            ?.let(shared::quoteValue)
            ?.let { quotedCategory ->
                stringProvider.get(
                    R.string.activity_subtitle_workout_category,
                    quotedCategory,
                )
            }
    }

    fun buildWorkoutCategoryChangeSubtitle(metadata: Map<String, String>): String? {
        val oldCategory = metadata[UserActionMetadataKeys.OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val newCategory = metadata[UserActionMetadataKeys.NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val fallbackCategory =
            metadata[UserActionMetadataKeys.CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: newCategory
                ?: oldCategory

        return when {
            oldCategory.isNullOrBlank() || newCategory.isNullOrBlank() ->
                fallbackCategory
                    ?.let(shared::quoteValue)
                    ?.let { quotedCategory ->
                        stringProvider.get(
                            R.string.activity_subtitle_workout_category,
                            quotedCategory,
                        )
                    }

            oldCategory == newCategory -> null

            else -> {
                val oldQuoted = shared.quoteValue(oldCategory)
                val newQuoted = shared.quoteValue(newCategory)
                if (oldQuoted == null || newQuoted == null) {
                    null
                } else {
                    stringProvider.get(
                        R.string.activity_subtitle_workout_category_change,
                        oldQuoted,
                        newQuoted,
                    )
                }
            }
        }
    }

    fun buildCategoryLabel(metadata: Map<String, String>): String {
        return metadata[UserActionMetadataKeys.CATEGORY_NAME]
            ?: metadata[UserActionMetadataKeys.NEW_VALUE]
            ?: metadata[UserActionMetadataKeys.OLD_VALUE]
            ?: stringProvider.get(R.string.activity_category_fallback)
    }
}
