package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel
    @Inject
    constructor(
        repository: UserActionRepository,
        private val stringProvider: StringProvider,
    ) : ViewModel() {
        private val locale = MutableStateFlow(Locale.getDefault())
        private val formatter = ActivityUiFormatter(stringProvider)

        val state: StateFlow<ActivityState> =
            combine(repository.observeActions(), locale) { actions, currentLocale ->
                ActivityState(sections = buildSections(actions, currentLocale))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = ActivityState(),
            )

        fun updateLocale(currentLocale: Locale) {
            locale.value = currentLocale
        }

        private fun buildSections(
            actions: List<UserActionRecord>,
            currentLocale: Locale,
        ): List<ActivitySectionUi> {
            if (actions.isEmpty()) return emptyList()

            val zoneId = ZoneId.systemDefault()
            val grouped =
                actions.groupBy { action ->
                    Instant.ofEpochMilli(action.timestamp).atZone(zoneId).toLocalDate()
                }

            return grouped.entries
                .sortedByDescending { it.key }
                .map { (date, records) ->
                    ActivitySectionUi(
                        date = date,
                        items = records.map { record -> toUi(record, zoneId, currentLocale) },
                    )
                }
        }

        private fun toUi(
            record: UserActionRecord,
            zoneId: ZoneId,
            currentLocale: Locale,
        ): ActivityItemUi {
            val metadata = formatter.parseMetadata(record.metadata)
            val time = formatter.formatTime(record.timestamp, zoneId, currentLocale)
            val title = formatter.buildTitle(record, metadata)
            val subtitle = formatter.buildSubtitle(record, metadata, currentLocale)

            return ActivityItemUi(
                id = record.id,
                title = title,
                subtitle = subtitle,
                time = time,
            )
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
