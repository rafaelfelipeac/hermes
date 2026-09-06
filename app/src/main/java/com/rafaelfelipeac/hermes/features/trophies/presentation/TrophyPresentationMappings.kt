@file:Suppress("CyclomaticComplexMethod", "LongMethod")

package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.ui.graphics.Color
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId

internal fun TrophyFamily.toUi(): TrophyFamilyUi {
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

internal fun trophyAccentColor(trophy: TrophyCardUi): Color {
    return trophy.categoryColorId?.let(::categoryAccentColor) ?: trophyFamilyAccentColor(trophy.family)
}

internal fun trophyIcon(trophyId: TrophyId): androidx.compose.ui.graphics.vector.ImageVector {
    return when (trophyId) {
        TrophyId.CHALLENGE_ACCEPTED,
        TrophyId.CHALLENGE_GOAL_SETTER,
        TrophyId.CHALLENGE_GOAL_ARCHITECT,
        -> Icons.Outlined.AddTask
        TrophyId.FIRST_CHALLENGE_WIN,
        TrophyId.CHALLENGE_MOMENTUM,
        TrophyId.CHALLENGE_VETERAN,
        -> Icons.Outlined.TrackChanges
        TrophyId.BACK_ON_TRACK,
        TrophyId.COMEBACK_MOMENTUM,
        TrophyId.NEVER_OUT,
        -> Icons.Outlined.RestartAlt
        TrophyId.FULL_TIME,
        TrophyId.SEASON_BUILDER,
        TrophyId.SEASON_ANCHOR,
        -> Icons.Outlined.CalendarMonth
        TrophyId.MATCH_FITNESS,
        TrophyId.ENGINE_ROOM,
        TrophyId.WORKHORSE,
        -> Icons.Outlined.FitnessCenter
        TrophyId.IN_FORM,
        TrophyId.LOCKED_IN,
        TrophyId.STEADY_RHYTHM,
        -> Icons.Outlined.Repeat
        TrophyId.COMEBACK_WEEK -> Icons.Outlined.Explore
        TrophyId.GAME_PLAN,
        TrophyId.TACTICAL_BOARD,
        TrophyId.FIELD_MARSHAL,
        -> Icons.Outlined.Build
        TrophyId.BACK_IN_FORMATION -> Icons.Outlined.ContentCopy
        TrophyId.HOLD_THE_LINE -> Icons.Outlined.CheckCircle
        TrophyId.TEAM_SHEET -> Icons.Outlined.GridView
        TrophyId.KIT_BAG -> Icons.Outlined.Archive
        TrophyId.KICKOFF,
        TrophyId.SET_PIECE,
        TrophyId.PROGRAM_BUILDER,
        -> Icons.Filled.Add
        TrophyId.PROTECTED_TIME -> Icons.Outlined.EventBusy
        TrophyId.EVENT_PLANNER,
        TrophyId.EVENT_CALENDAR,
        TrophyId.EVENT_SEASON,
        -> Icons.Outlined.Flag
        TrophyId.RACE_READY,
        TrophyId.RACE_SHARP,
        TrophyId.RACE_FINISH,
        -> Icons.Outlined.SportsScore
        TrophyId.FIRST_BENCHMARK,
        TrophyId.BENCHMARK_BUILDER,
        TrophyId.RECORD_LIBRARY,
        -> Icons.AutoMirrored.Outlined.LibraryBooks
        TrophyId.ON_THE_BOARD,
        TrophyId.FORM_BOOK,
        TrophyId.RECORD_KEEPER,
        -> Icons.Outlined.Leaderboard
        TrophyId.PACE_SETTER,
        TrophyId.SPLIT_STRATEGIST,
        TrophyId.PACE_MASTER,
        -> Icons.Outlined.Calculate
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        -> Icons.Default.EmojiEvents
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        -> Icons.Outlined.Home
        TrophyId.TRAINING_BLOCK -> Icons.Outlined.Inventory2
    }
}

@StringRes
fun trophyNameRes(trophyId: TrophyId): Int {
    return when (trophyId) {
        TrophyId.CHALLENGE_ACCEPTED -> R.string.trophies_name_challenge_accepted
        TrophyId.CHALLENGE_GOAL_SETTER -> R.string.trophies_name_challenge_goal_setter
        TrophyId.CHALLENGE_GOAL_ARCHITECT -> R.string.trophies_name_challenge_goal_architect
        TrophyId.FIRST_CHALLENGE_WIN -> R.string.trophies_name_first_challenge_win
        TrophyId.CHALLENGE_MOMENTUM -> R.string.trophies_name_challenge_momentum
        TrophyId.CHALLENGE_VETERAN -> R.string.trophies_name_challenge_veteran
        TrophyId.BACK_ON_TRACK -> R.string.trophies_name_back_on_track
        TrophyId.COMEBACK_MOMENTUM -> R.string.trophies_name_comeback_momentum
        TrophyId.NEVER_OUT -> R.string.trophies_name_never_out
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
        TrophyId.KICKOFF -> R.string.trophies_name_kickoff
        TrophyId.SET_PIECE -> R.string.trophies_name_set_piece
        TrophyId.PROGRAM_BUILDER -> R.string.trophies_name_program_builder
        TrophyId.PROTECTED_TIME -> R.string.trophies_name_protected_time
        TrophyId.EVENT_PLANNER -> R.string.trophies_name_event_planner
        TrophyId.EVENT_CALENDAR -> R.string.trophies_name_event_calendar
        TrophyId.EVENT_SEASON -> R.string.trophies_name_event_season
        TrophyId.RACE_READY -> R.string.trophies_name_race_ready
        TrophyId.RACE_SHARP -> R.string.trophies_name_race_sharp
        TrophyId.RACE_FINISH -> R.string.trophies_name_race_finish
        TrophyId.FIRST_BENCHMARK -> R.string.trophies_name_first_benchmark
        TrophyId.BENCHMARK_BUILDER -> R.string.trophies_name_benchmark_builder
        TrophyId.RECORD_LIBRARY -> R.string.trophies_name_record_library
        TrophyId.ON_THE_BOARD -> R.string.trophies_name_on_the_board
        TrophyId.FORM_BOOK -> R.string.trophies_name_form_book
        TrophyId.RECORD_KEEPER -> R.string.trophies_name_record_keeper
        TrophyId.PACE_SETTER -> R.string.trophies_name_pace_setter
        TrophyId.SPLIT_STRATEGIST -> R.string.trophies_name_split_strategist
        TrophyId.PACE_MASTER -> R.string.trophies_name_pace_master
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
        TrophyId.CHALLENGE_ACCEPTED,
        TrophyId.CHALLENGE_GOAL_SETTER,
        TrophyId.CHALLENGE_GOAL_ARCHITECT,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_challenge_creations_unlocked
            } else {
                R.string.trophies_desc_challenge_creations_locked
            }
        TrophyId.FIRST_CHALLENGE_WIN,
        TrophyId.CHALLENGE_MOMENTUM,
        TrophyId.CHALLENGE_VETERAN,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_challenge_completions_unlocked
            } else {
                R.string.trophies_desc_challenge_completions_locked
            }
        TrophyId.BACK_ON_TRACK,
        TrophyId.COMEBACK_MOMENTUM,
        TrophyId.NEVER_OUT,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_challenge_recoveries_unlocked
            } else {
                R.string.trophies_desc_challenge_recoveries_locked
            }
        TrophyId.FULL_TIME,
        TrophyId.SEASON_BUILDER,
        TrophyId.SEASON_ANCHOR,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_complete_weeks_unlocked
            } else {
                R.string.trophies_desc_complete_weeks_locked
            }
        TrophyId.MATCH_FITNESS,
        TrophyId.ENGINE_ROOM,
        TrophyId.WORKHORSE,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_workout_completions_unlocked
            } else {
                R.string.trophies_desc_workout_completions_locked
            }
        TrophyId.IN_FORM,
        TrophyId.LOCKED_IN,
        TrophyId.STEADY_RHYTHM,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_streak_weeks_unlocked
            } else {
                R.string.trophies_desc_streak_weeks_locked
            }
        TrophyId.COMEBACK_WEEK ->
            if (isUnlocked) {
                R.string.trophies_desc_comeback_weeks_unlocked
            } else {
                R.string.trophies_desc_comeback_weeks_locked
            }
        TrophyId.GAME_PLAN,
        TrophyId.TACTICAL_BOARD,
        TrophyId.FIELD_MARSHAL,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_planning_changes_unlocked
            } else {
                R.string.trophies_desc_planning_changes_locked
            }
        TrophyId.BACK_IN_FORMATION ->
            if (isUnlocked) {
                R.string.trophies_desc_copied_weeks_unlocked
            } else {
                R.string.trophies_desc_copied_weeks_locked
            }
        TrophyId.HOLD_THE_LINE ->
            if (isUnlocked) {
                R.string.trophies_desc_copied_completed_weeks_unlocked
            } else {
                R.string.trophies_desc_copied_completed_weeks_locked
            }
        TrophyId.TEAM_SHEET ->
            if (isUnlocked) {
                R.string.trophies_desc_category_actions_unlocked
            } else {
                R.string.trophies_desc_category_actions_locked
            }
        TrophyId.KIT_BAG ->
            if (isUnlocked) {
                R.string.trophies_desc_backups_unlocked
            } else {
                R.string.trophies_desc_backups_locked
            }
        TrophyId.KICKOFF,
        TrophyId.SET_PIECE,
        TrophyId.PROGRAM_BUILDER,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_workout_creations_unlocked
            } else {
                R.string.trophies_desc_workout_creations_locked
            }
        TrophyId.PROTECTED_TIME ->
            if (isUnlocked) {
                R.string.trophies_desc_protected_time_unlocked
            } else {
                R.string.trophies_desc_protected_time_locked
            }
        TrophyId.EVENT_PLANNER,
        TrophyId.EVENT_CALENDAR,
        TrophyId.EVENT_SEASON,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_race_event_creations_unlocked
            } else {
                R.string.trophies_desc_race_event_creations_locked
            }
        TrophyId.RACE_READY,
        TrophyId.RACE_SHARP,
        TrophyId.RACE_FINISH,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_race_event_completions_unlocked
            } else {
                R.string.trophies_desc_race_event_completions_locked
            }
        TrophyId.FIRST_BENCHMARK,
        TrophyId.BENCHMARK_BUILDER,
        TrophyId.RECORD_LIBRARY,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_personal_record_series_creations_unlocked
            } else {
                R.string.trophies_desc_personal_record_series_creations_locked
            }
        TrophyId.ON_THE_BOARD,
        TrophyId.FORM_BOOK,
        TrophyId.RECORD_KEEPER,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_personal_record_result_creations_unlocked
            } else {
                R.string.trophies_desc_personal_record_result_creations_locked
            }
        TrophyId.PACE_SETTER,
        TrophyId.SPLIT_STRATEGIST,
        TrophyId.PACE_MASTER,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_pace_calculations_unlocked
            } else {
                R.string.trophies_desc_pace_calculations_locked
            }
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_category_completions_unlocked
            } else {
                R.string.trophies_desc_category_completions_locked
            }
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        ->
            if (isUnlocked) {
                R.string.trophies_desc_category_presence_unlocked
            } else {
                R.string.trophies_desc_category_presence_locked
            }
        TrophyId.TRAINING_BLOCK ->
            if (isUnlocked) {
                R.string.trophies_desc_category_planning_unlocked
            } else {
                R.string.trophies_desc_category_planning_locked
            }
    }
}

@StringRes
internal fun trophyShareDescriptionRes(trophyId: TrophyId): Int {
    return when (trophyId) {
        TrophyId.CHALLENGE_ACCEPTED,
        TrophyId.CHALLENGE_GOAL_SETTER,
        TrophyId.CHALLENGE_GOAL_ARCHITECT,
        -> R.string.trophies_share_desc_challenge_creations
        TrophyId.FIRST_CHALLENGE_WIN,
        TrophyId.CHALLENGE_MOMENTUM,
        TrophyId.CHALLENGE_VETERAN,
        -> R.string.trophies_share_desc_challenge_completions
        TrophyId.BACK_ON_TRACK,
        TrophyId.COMEBACK_MOMENTUM,
        TrophyId.NEVER_OUT,
        -> R.string.trophies_share_desc_challenge_recoveries
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
        TrophyId.KICKOFF,
        TrophyId.SET_PIECE,
        TrophyId.PROGRAM_BUILDER,
        -> R.string.trophies_share_desc_workout_creations
        TrophyId.PROTECTED_TIME -> R.string.trophies_share_desc_protected_time
        TrophyId.EVENT_PLANNER,
        TrophyId.EVENT_CALENDAR,
        TrophyId.EVENT_SEASON,
        -> R.string.trophies_share_desc_race_event_creations
        TrophyId.RACE_READY,
        TrophyId.RACE_SHARP,
        TrophyId.RACE_FINISH,
        -> R.string.trophies_share_desc_race_event_completions
        TrophyId.FIRST_BENCHMARK,
        TrophyId.BENCHMARK_BUILDER,
        TrophyId.RECORD_LIBRARY,
        -> R.string.trophies_share_desc_personal_record_series_creations
        TrophyId.ON_THE_BOARD,
        TrophyId.FORM_BOOK,
        TrophyId.RECORD_KEEPER,
        -> R.string.trophies_share_desc_personal_record_result_creations
        TrophyId.PACE_SETTER,
        TrophyId.SPLIT_STRATEGIST,
        TrophyId.PACE_MASTER,
        -> R.string.trophies_share_desc_pace_calculations
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
        TrophyFamilyUi.CHALLENGES -> R.string.trophies_family_challenges
        TrophyFamilyUi.FOLLOW_THROUGH -> R.string.trophies_family_follow_through
        TrophyFamilyUi.CONSISTENCY -> R.string.trophies_family_consistency
        TrophyFamilyUi.ADAPTABILITY -> R.string.trophies_family_adaptability
        TrophyFamilyUi.MOMENTUM -> R.string.trophies_family_momentum
        TrophyFamilyUi.BUILDER -> R.string.trophies_family_builder
        TrophyFamilyUi.RACE_EVENTS -> R.string.trophies_family_race_events
        TrophyFamilyUi.PERSONAL_RECORDS -> R.string.trophies_family_personal_records
        TrophyFamilyUi.CATEGORIES -> R.string.trophies_family_categories
    }
}

internal fun celebrationToken(trophy: TrophyCardUi): String {
    return "${trophy.stableId}:${trophy.unlockedAt ?: 0L}"
}

private fun trophyFamilyAccentColor(family: TrophyFamilyUi): Color {
    return when (family) {
        TrophyFamilyUi.CHALLENGES -> Color(0xFF4A7A89)
        TrophyFamilyUi.FOLLOW_THROUGH -> Color(0xFF4277B8)
        TrophyFamilyUi.CONSISTENCY -> Color(0xFF2B917D)
        TrophyFamilyUi.ADAPTABILITY -> Color(0xFFB97B38)
        TrophyFamilyUi.MOMENTUM -> Color(0xFF7B63C8)
        TrophyFamilyUi.BUILDER -> Color(0xFF6F7E4A)
        TrophyFamilyUi.RACE_EVENTS -> Color(0xFFB44F3E)
        TrophyFamilyUi.PERSONAL_RECORDS -> Color(0xFF3F7C74)
        TrophyFamilyUi.CATEGORIES -> Color(0xFF8A5E3A)
    }
}
