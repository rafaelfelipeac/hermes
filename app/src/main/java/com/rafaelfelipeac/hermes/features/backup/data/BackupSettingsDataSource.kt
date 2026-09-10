package com.rafaelfelipeac.hermes.features.backup.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.rafaelfelipeac.hermes.features.settings.data.DISTANCE_UNIT_KEY
import com.rafaelfelipeac.hermes.features.settings.data.LANGUAGE_KEY
import com.rafaelfelipeac.hermes.features.settings.data.PACE_UNIT_KEY
import com.rafaelfelipeac.hermes.features.settings.data.SLOT_MODE_POLICY_KEY
import com.rafaelfelipeac.hermes.features.settings.data.THEME_MODE_KEY
import com.rafaelfelipeac.hermes.features.settings.data.WEEK_START_DAY_KEY
import com.rafaelfelipeac.hermes.features.settings.data.WEIGHT_UNIT_KEY
import com.rafaelfelipeac.hermes.features.settings.data.settingsDataStore
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SettingsSnapshot
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

open class BackupSettingsDataSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.settingsDataStore

        open suspend fun snapshot(): SettingsSnapshot {
            val prefs = dataStore.data.first()
            return SettingsSnapshot(
                themeMode = prefs[THEME_MODE_KEY]?.enumValueOrNull<ThemeMode>() ?: ThemeMode.SYSTEM,
                language = prefs[LANGUAGE_KEY]?.let(AppLanguage::fromTag) ?: AppLanguage.SYSTEM,
                slotModePolicy =
                    prefs[SLOT_MODE_POLICY_KEY]?.enumValueOrNull<SlotModePolicy>()
                        ?: SlotModePolicy.AUTO_WHEN_MULTIPLE,
                weekStartDay = WeekStartDay.fromStoredValue(prefs[WEEK_START_DAY_KEY]),
                distanceUnit = prefs[DISTANCE_UNIT_KEY]?.enumValueOrNull<DistanceUnit>() ?: DistanceUnit.KILOMETERS,
                paceUnit = prefs[PACE_UNIT_KEY]?.enumValueOrNull<PaceUnit>() ?: PaceUnit.MIN_PER_KM,
                weightUnit = prefs[WEIGHT_UNIT_KEY]?.enumValueOrNull<WeightUnit>() ?: WeightUnit.KILOGRAMS,
            )
        }

        open suspend fun replace(snapshot: SettingsSnapshot) {
            dataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = snapshot.themeMode.name
                prefs[LANGUAGE_KEY] = snapshot.language.tag
                prefs[SLOT_MODE_POLICY_KEY] = snapshot.slotModePolicy.name
                prefs[WEEK_START_DAY_KEY] = snapshot.weekStartDay.name
                prefs[DISTANCE_UNIT_KEY] = snapshot.distanceUnit.name
                prefs[PACE_UNIT_KEY] = snapshot.paceUnit.name
                prefs[WEIGHT_UNIT_KEY] = snapshot.weightUnit.name
            }
        }
    }

private inline fun <reified T : Enum<T>> String.enumValueOrNull(): T? = runCatching { enumValueOf<T>(this) }.getOrNull()
