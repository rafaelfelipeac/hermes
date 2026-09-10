package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

abstract class ActivityUiFormatterTestFixture {
    private val stringProvider =
        object : StringProvider {
            override fun get(
                id: Int,
                vararg args: Any,
            ): String {
                val firstArg = args.firstOrNull()?.toString().orEmpty()
                return firstArgStrings[id]?.invoke(firstArg)
                    ?: argsStrings[id]?.invoke(args)
                    ?: id.toString()
            }

            override fun getForLanguage(
                languageTag: String?,
                id: Int,
                vararg args: Any,
            ): String = get(id, *args)
        }

    protected val formatter = ActivityUiFormatter(stringProvider)

    private val firstArgStrings =
        mapOf<Int, (String) -> String>(
            R.string.activity_action_create_race_event to { "You created the event $it." },
            R.string.activity_action_create_workout to { "You created the workout $it." },
            R.string.activity_value_quoted to { "\"$it\"" },
            R.string.activity_action_change_distance_unit to { "You changed the distance unit." },
            R.string.activity_action_use_pace_calculator to { "You used the pace calculator." },
            R.string.activity_action_create_personal_record_entry to { "You added a $it PR result." },
            R.string.activity_action_restore_challenge_named to { "You restored the challenge $it." },
            R.string.activity_action_create_challenge_progress_entry to { "You logged challenge progress." },
            R.string.activity_action_update_challenge_progress_entry to { "You updated challenge progress." },
            R.string.activity_action_delete_challenge_progress_entry to { "You deleted challenge progress." },
            R.string.activity_action_restore_challenge_progress_entry to { "You restored challenge progress." },
            R.string.activity_action_complete_challenge to { "You completed a challenge." },
            R.string.activity_action_create_challenge_progress_entry_named to { "You logged progress for $it." },
            R.string.activity_action_update_challenge_progress_entry_named to { "You updated progress for $it." },
            R.string.activity_action_delete_challenge_progress_entry_named to { "You deleted progress for $it." },
            R.string.activity_action_restore_challenge_progress_entry_named to { "You restored progress for $it." },
            R.string.activity_action_complete_challenge_named to { "You completed the challenge $it." },
            R.string.activity_action_share_trophy to { "You started sharing the trophy $it." },
            R.string.activity_subtitle_challenge_recovered to { "Recovered." },
            R.string.challenge_target_type_daily to { "Daily" },
            R.string.challenge_target_type_total to { "Total" },
            R.string.activity_value_unknown to { "Unknown" },
            R.string.activity_subtitle_separator to { "\n" },
            R.string.activity_time_pattern to { "HH:mm" },
            R.string.activity_week_date_pattern to { "MMM d, uuuu" },
            R.string.activity_subtitle_week to { "Week of $it." },
            R.string.settings_unit_kilometers to { "km" },
            R.string.settings_unit_miles to { "mi" },
            R.string.personal_records_metric_distance to { "Distance" },
            R.string.activity_workout_fallback to { "untitled" },
            R.string.day_monday to { "Monday" },
            R.string.day_tuesday to { "Tuesday" },
            R.string.day_wednesday to { "Wednesday" },
            R.string.day_thursday to { "Thursday" },
            R.string.day_friday to { "Friday" },
            R.string.day_saturday to { "Saturday" },
            R.string.day_sunday to { "Sunday" },
            R.string.weekly_training_slot_morning to { "Morning" },
            R.string.weekly_training_slot_afternoon to { "Afternoon" },
            R.string.weekly_training_slot_night to { "Evening" },
        )

    private val argsStrings =
        mapOf<Int, (Array<out Any>) -> String>(
            R.string.activity_subtitle_challenge_progress_added to { "Added ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_updated to {
                "Changed ${it[0]} on ${it[1]} to ${it[2]} on ${it[3]}."
            },
            R.string.activity_subtitle_challenge_progress_updated_current to { "Updated ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_deleted to { "Deleted ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_restored to { "Restored ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_target_daily to { "Target: ${it[0]} per day." },
            R.string.activity_subtitle_challenge_target_total to { "Target: ${it[0]} total." },
            R.string.activity_subtitle_challenge_target_change to { "Target changed from ${it[0]} to ${it[1]}." },
            R.string.activity_subtitle_challenge_target_value_daily to { "${it[0]} per day" },
            R.string.activity_subtitle_challenge_target_value_total to { "${it[0]} total" },
            R.string.activity_subtitle_challenge_category to { "Category ${it[0]}." },
            R.string.activity_subtitle_challenge_dates to { "${it[0]} to ${it[1]}." },
            R.string.activity_subtitle_change_value to { "From ${it[0]} to ${it[1]}." },
            R.string.activity_subtitle_move to { "From ${it[0]} to ${it[1]}." },
        )

    protected fun personalRecordEntryRecord(): UserActionRecord {
        return UserActionRecord(
            id = 1L,
            actionType = UserActionType.CREATE_PERSONAL_RECORD_ENTRY.name,
            entityType = UserActionEntityType.PERSONAL_RECORD.name,
            entityId = 42L,
            metadata = null,
            timestamp = 0L,
        )
    }

    protected fun challengeProgressRecord(actionType: UserActionType): UserActionRecord {
        return UserActionRecord(
            id = 1L,
            actionType = actionType.name,
            entityType = UserActionEntityType.CHALLENGE.name,
            entityId = 42L,
            metadata = null,
            timestamp = 0L,
        )
    }

    protected fun challengeRecord(actionType: UserActionType): UserActionRecord = challengeProgressRecord(actionType)

    protected fun challengeMetadata(): Map<String, String> {
        return mapOf(
            UserActionMetadataKeys.CHALLENGE_TITLE to "Strength",
            UserActionMetadataKeys.CHALLENGE_TARGET_TYPE to "DAILY",
            UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "10",
            UserActionMetadataKeys.CHALLENGE_CATEGORY_NAME to "Strength",
            UserActionMetadataKeys.CHALLENGE_START_DATE to "2026-08-01",
            UserActionMetadataKeys.CHALLENGE_END_DATE to "2026-08-31",
        )
    }

    protected fun challengeProgressMetadata(): Map<String, String> {
        return challengeMetadata() +
            mapOf(
                UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY to "3",
                UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE to "2026-08-31",
                UserActionMetadataKeys.CHALLENGE_RECOVERED to "true",
            )
    }
}
