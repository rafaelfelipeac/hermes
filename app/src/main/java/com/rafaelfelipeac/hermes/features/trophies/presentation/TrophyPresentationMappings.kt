package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.annotation.StringRes
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId

internal fun TrophyFamily.toUi(): TrophyFamilyUi {
    return when (this) {
        TrophyFamily.FOLLOW_THROUGH -> TrophyFamilyUi.FOLLOW_THROUGH
        TrophyFamily.CONSISTENCY -> TrophyFamilyUi.CONSISTENCY
        TrophyFamily.ADAPTABILITY -> TrophyFamilyUi.ADAPTABILITY
        TrophyFamily.MOMENTUM -> TrophyFamilyUi.MOMENTUM
        TrophyFamily.BUILDER -> TrophyFamilyUi.BUILDER
        TrophyFamily.CATEGORIES -> TrophyFamilyUi.CATEGORIES
    }
}

@StringRes
internal fun trophyNameRes(trophyId: TrophyId): Int {
    return when (trophyId) {
        TrophyId.FULL_TIME -> R.string.trophies_name_full_time
        TrophyId.SEASON_BUILDER -> R.string.trophies_name_season_builder
        TrophyId.SEASON_ANCHOR -> R.string.trophies_name_season_anchor
        TrophyId.MATCH_FITNESS -> R.string.trophies_name_match_fitness
        TrophyId.ENGINE_ROOM -> R.string.trophies_name_engine_room
        TrophyId.WORKHORSE -> R.string.trophies_name_workhorse
        TrophyId.IN_FORM -> R.string.trophies_name_in_form
        TrophyId.LOCKED_IN -> R.string.trophies_name_locked_in
        TrophyId.STEADY_RHYTHM -> R.string.trophies_name_steady_rhythm
        TrophyId.COMEBACK_WEEK -> R.string.trophies_name_comeback_week
        TrophyId.GAME_PLAN -> R.string.trophies_name_game_plan
        TrophyId.TACTICAL_BOARD -> R.string.trophies_name_tactical_board
        TrophyId.FIELD_MARSHAL -> R.string.trophies_name_field_marshal
        TrophyId.BACK_IN_FORMATION -> R.string.trophies_name_back_in_formation
        TrophyId.HOLD_THE_LINE -> R.string.trophies_name_hold_the_line
        TrophyId.TEAM_SHEET -> R.string.trophies_name_team_sheet
        TrophyId.KIT_BAG -> R.string.trophies_name_kit_bag
        TrophyId.PODIUM_PLACE -> R.string.trophies_name_podium_place
        TrophyId.IN_ROTATION -> R.string.trophies_name_in_rotation
        TrophyId.MAINSTAY -> R.string.trophies_name_mainstay
        TrophyId.HOME_GROUND -> R.string.trophies_name_home_ground
        TrophyId.LOCAL_FAVORITE -> R.string.trophies_name_local_favorite
        TrophyId.TERRITORY -> R.string.trophies_name_territory
        TrophyId.TRAINING_BLOCK -> R.string.trophies_name_training_block
    }
}

@StringRes
internal fun trophyDescriptionRes(
    trophyId: TrophyId,
    isUnlocked: Boolean,
): Int {
    return when (trophyId) {
        TrophyId.FULL_TIME,
        TrophyId.SEASON_BUILDER,
        TrophyId.SEASON_ANCHOR,
        -> if (isUnlocked) R.string.trophies_desc_complete_weeks_unlocked else R.string.trophies_desc_complete_weeks_locked
        TrophyId.MATCH_FITNESS,
        TrophyId.ENGINE_ROOM,
        TrophyId.WORKHORSE,
        -> if (isUnlocked) R.string.trophies_desc_workout_completions_unlocked else R.string.trophies_desc_workout_completions_locked
        TrophyId.IN_FORM,
        TrophyId.LOCKED_IN,
        TrophyId.STEADY_RHYTHM,
        -> if (isUnlocked) R.string.trophies_desc_streak_weeks_unlocked else R.string.trophies_desc_streak_weeks_locked
        TrophyId.COMEBACK_WEEK ->
            if (isUnlocked) R.string.trophies_desc_comeback_weeks_unlocked else R.string.trophies_desc_comeback_weeks_locked
        TrophyId.GAME_PLAN,
        TrophyId.TACTICAL_BOARD,
        TrophyId.FIELD_MARSHAL,
        -> if (isUnlocked) R.string.trophies_desc_planning_changes_unlocked else R.string.trophies_desc_planning_changes_locked
        TrophyId.BACK_IN_FORMATION ->
            if (isUnlocked) R.string.trophies_desc_copied_weeks_unlocked else R.string.trophies_desc_copied_weeks_locked
        TrophyId.HOLD_THE_LINE ->
            if (isUnlocked) R.string.trophies_desc_copied_completed_weeks_unlocked else R.string.trophies_desc_copied_completed_weeks_locked
        TrophyId.TEAM_SHEET ->
            if (isUnlocked) R.string.trophies_desc_category_actions_unlocked else R.string.trophies_desc_category_actions_locked
        TrophyId.KIT_BAG ->
            if (isUnlocked) R.string.trophies_desc_backups_unlocked else R.string.trophies_desc_backups_locked
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        -> if (isUnlocked) R.string.trophies_desc_category_completions_unlocked else R.string.trophies_desc_category_completions_locked
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        -> if (isUnlocked) R.string.trophies_desc_category_presence_unlocked else R.string.trophies_desc_category_presence_locked
        TrophyId.TRAINING_BLOCK ->
            if (isUnlocked) R.string.trophies_desc_category_planning_unlocked else R.string.trophies_desc_category_planning_locked
    }
}

@StringRes
internal fun trophyShareDescriptionRes(trophyId: TrophyId): Int {
    return when (trophyId) {
        TrophyId.FULL_TIME,
        TrophyId.SEASON_BUILDER,
        TrophyId.SEASON_ANCHOR,
        -> R.string.trophies_share_desc_complete_weeks
        TrophyId.MATCH_FITNESS,
        TrophyId.ENGINE_ROOM,
        TrophyId.WORKHORSE,
        -> R.string.trophies_share_desc_workout_completions
        TrophyId.IN_FORM,
        TrophyId.LOCKED_IN,
        TrophyId.STEADY_RHYTHM,
        -> R.string.trophies_share_desc_streak_weeks
        TrophyId.COMEBACK_WEEK -> R.string.trophies_share_desc_comeback_weeks
        TrophyId.GAME_PLAN,
        TrophyId.TACTICAL_BOARD,
        TrophyId.FIELD_MARSHAL,
        -> R.string.trophies_share_desc_planning_changes
        TrophyId.BACK_IN_FORMATION -> R.string.trophies_share_desc_copied_weeks
        TrophyId.HOLD_THE_LINE -> R.string.trophies_share_desc_copied_completed_weeks
        TrophyId.TEAM_SHEET -> R.string.trophies_share_desc_category_actions
        TrophyId.KIT_BAG -> R.string.trophies_share_desc_backups
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        -> R.string.trophies_share_desc_category_completions
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        -> R.string.trophies_share_desc_category_presence
        TrophyId.TRAINING_BLOCK -> R.string.trophies_share_desc_category_planning
    }
}

@StringRes
internal fun familyTitleRes(family: TrophyFamilyUi): Int {
    return when (family) {
        TrophyFamilyUi.FOLLOW_THROUGH -> R.string.trophies_family_follow_through
        TrophyFamilyUi.CONSISTENCY -> R.string.trophies_family_consistency
        TrophyFamilyUi.ADAPTABILITY -> R.string.trophies_family_adaptability
        TrophyFamilyUi.MOMENTUM -> R.string.trophies_family_momentum
        TrophyFamilyUi.BUILDER -> R.string.trophies_family_builder
        TrophyFamilyUi.CATEGORIES -> R.string.trophies_family_categories
    }
}

@StringRes
internal fun familyDescriptionRes(family: TrophyFamilyUi): Int {
    return when (family) {
        TrophyFamilyUi.FOLLOW_THROUGH -> R.string.trophies_family_follow_through_desc
        TrophyFamilyUi.CONSISTENCY -> R.string.trophies_family_consistency_desc
        TrophyFamilyUi.ADAPTABILITY -> R.string.trophies_family_adaptability_desc
        TrophyFamilyUi.MOMENTUM -> R.string.trophies_family_momentum_desc
        TrophyFamilyUi.BUILDER -> R.string.trophies_family_builder_desc
        TrophyFamilyUi.CATEGORIES -> R.string.trophies_family_categories_desc
    }
}

internal fun celebrationToken(trophy: TrophyCardUi): String {
    return "${trophy.stableId}:${trophy.unlockedAt ?: 0L}"
}
