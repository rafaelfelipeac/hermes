package com.rafaelfelipeac.hermes.features.activity.presentation

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import java.time.LocalDate

data class ActivityActionFilterContext(
    val primaryFilter: ActivityPrimaryFilter,
    val categoryId: Long?,
    val categories: List<CategoryUi>,
    val categoryAliasesById: Map<Long, Set<String>>,
    val weekStartDate: LocalDate?,
)

internal fun filterActions(
    actions: List<UserActionRecord>,
    context: ActivityActionFilterContext,
    formatter: ActivityUiFormatter,
): List<UserActionRecord> {
    val categoryNameById = context.categories.associate { it.id to it.name }

    return actions.filter { action ->
        when (context.primaryFilter) {
            ActivityPrimaryFilter.ALL -> true
            ActivityPrimaryFilter.COMPLETIONS -> action.isCompletionAction()
            ActivityPrimaryFilter.PLANNING -> action.isPlanningAction()
            ActivityPrimaryFilter.CATEGORIES -> action.isCategoryAction()
            ActivityPrimaryFilter.CATEGORY -> {
                val selectedCategoryName = context.categoryId?.let(categoryNameById::get)
                val categoryIds = categoryIdsForAction(action, formatter)
                val categoryNames = categoryNamesForAction(action, formatter)
                val aliases = context.categoryId?.let(context.categoryAliasesById::get).orEmpty()

                (context.categoryId != null && context.categoryId in categoryIds) ||
                    (selectedCategoryName != null && selectedCategoryName in categoryNames) ||
                    aliases.any { it in categoryNames }
            }
            ActivityPrimaryFilter.SETTINGS -> action.isSettingsAction()
            ActivityPrimaryFilter.WEEK ->
                context.weekStartDate != null &&
                    weekStartDateForAction(action, formatter) == context.weekStartDate
        }
    }
}

internal fun buildCategoryAliasesById(
    actions: List<UserActionRecord>,
    categories: List<CategoryUi>,
    formatter: ActivityUiFormatter,
): Map<Long, Set<String>> {
    val aliases =
        categories.associate { category ->
            category.id to mutableSetOf(category.name)
        }.toMutableMap()

    actions.forEach { action ->
        if (action.entityType.toUserActionEntityTypeOrNull() != UserActionEntityType.CATEGORY) return@forEach
        val categoryId = action.entityId ?: return@forEach
        val metadata = formatter.parseMetadata(action.metadata)
        val names = aliases.getOrPut(categoryId) { mutableSetOf() }

        metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() }?.let(names::add)

        if (action.actionType.toUserActionTypeOrNull() == UserActionType.UPDATE_CATEGORY_NAME) {
            metadata[OLD_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
            metadata[NEW_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
        }
    }

    return aliases.mapValues { (_, names) -> names.toSet() }
}

private fun categoryNamesForAction(
    action: UserActionRecord,
    formatter: ActivityUiFormatter,
): Set<String> {
    val metadata = formatter.parseMetadata(action.metadata)

    return setOfNotNull(
        metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() },
        metadata[NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
        metadata[OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
    )
}

private fun categoryIdsForAction(
    action: UserActionRecord,
    formatter: ActivityUiFormatter,
): Set<Long> {
    val metadata = formatter.parseMetadata(action.metadata)

    return setOfNotNull(
        metadata[CATEGORY_ID]?.toLongOrNull(),
        metadata[OLD_CATEGORY_ID]?.toLongOrNull(),
        metadata[NEW_CATEGORY_ID]?.toLongOrNull(),
    )
}

internal fun weekStartDateForAction(
    action: UserActionRecord,
    formatter: ActivityUiFormatter,
): LocalDate? {
    val metadata = formatter.parseMetadata(action.metadata)

    return metadata[WEEK_START_DATE]?.let { value ->
        runCatching { LocalDate.parse(value) }.getOrNull()
    }
}

private fun UserActionRecord.isCompletionAction(): Boolean {
    val actionType = actionType.toUserActionTypeOrNull() ?: return false
    return actionType in completionActions
}

private fun UserActionRecord.isPlanningAction(): Boolean {
    val actionType = actionType.toUserActionTypeOrNull() ?: return false
    return actionType in planningActions
}

private fun UserActionRecord.isCategoryAction(): Boolean {
    return entityType.toUserActionEntityTypeOrNull() == UserActionEntityType.CATEGORY
}

private fun UserActionRecord.isSettingsAction(): Boolean {
    val entityType = entityType.toUserActionEntityTypeOrNull()
    return entityType == UserActionEntityType.SETTINGS || entityType == UserActionEntityType.APP
}

private val completionActions =
    setOf(
        UserActionType.COMPLETE_WORKOUT,
        UserActionType.COMPLETE_WEEK_WORKOUTS,
        UserActionType.UNDO_COMPLETE_WORKOUT,
        UserActionType.INCOMPLETE_WORKOUT,
        UserActionType.UNDO_INCOMPLETE_WORKOUT,
    )

private val planningActions =
    setOf(
        UserActionType.CREATE_WORKOUT,
        UserActionType.UPDATE_WORKOUT,
        UserActionType.DELETE_WORKOUT,
        UserActionType.UNDO_DELETE_WORKOUT,
        UserActionType.REORDER_WORKOUT,
        UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
        UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY,
        UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS,
        UserActionType.CREATE_REST_DAY,
        UserActionType.CREATE_BUSY,
        UserActionType.CREATE_SICK,
        UserActionType.UPDATE_REST_DAY,
        UserActionType.UPDATE_BUSY,
        UserActionType.UPDATE_SICK,
        UserActionType.DELETE_REST_DAY,
        UserActionType.DELETE_BUSY,
        UserActionType.DELETE_SICK,
        UserActionType.UNDO_DELETE_REST_DAY,
        UserActionType.UNDO_DELETE_BUSY,
        UserActionType.UNDO_DELETE_SICK,
        UserActionType.CONVERT_WORKOUT_TO_REST_DAY,
        UserActionType.CONVERT_REST_DAY_TO_WORKOUT,
        UserActionType.COPY_LAST_WEEK,
        UserActionType.UNDO_COPY_LAST_WEEK,
        UserActionType.OPEN_WEEK,
    )
