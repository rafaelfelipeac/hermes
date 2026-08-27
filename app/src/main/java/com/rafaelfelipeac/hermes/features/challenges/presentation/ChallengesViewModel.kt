@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

@file:Suppress("LongMethod", "MaxLineLength", "TooManyFunctions", "ArgumentListWrapping")

package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ARCHIVED_AT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_END_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_FIRST_COMPLETION_AT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_LIFECYCLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_NEW_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_NEW_STATUS
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_OLD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_OLD_STATUS
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_RECOVERED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_UNIT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.ARCHIVE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REACTIVATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.features.challenges.domain.ChallengeCalculator
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeUiState
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel
    @Inject
    constructor(
        private val repository: ChallengeRepository,
        private val userActionLogger: UserActionLogger,
        private val stringProvider: StringProvider,
        private val clock: Clock,
    ) : ViewModel() {
        private val calculator = ChallengeCalculator()
        private val actionMutex = Mutex()
        private val editorState = MutableStateFlow(defaultEditorState(today = LocalDate.now(clock)))
        private val selectedChallengeId = MutableStateFlow<Long?>(null)
        private val undoState = MutableStateFlow<ChallengeUndoState?>(null)
        private var undoTimeoutJob: Job? = null
        private var undoCounter = 0L

        private val todayFlow: Flow<LocalDate> =
            flow {
                while (true) {
                    emit(LocalDate.now(clock))
                    delay(delayUntilNextMidnight(clock))
                }
            }

        private val selectedChallengeFlow: Flow<Challenge?> =
            selectedChallengeId.flatMapLatest { id ->
                if (id == null) {
                    flowOf(null)
                } else {
                    repository.observeChallenge(id)
                }
            }

        private val selectedProgressEntriesFlow: Flow<List<ChallengeProgressEntry>> =
            selectedChallengeId.flatMapLatest { id ->
                if (id == null) {
                    flowOf(emptyList())
                } else {
                    repository.observeProgressEntries(id)
                }
            }

        val undoUiState: StateFlow<ChallengeUndoState?> =
            undoState.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = null,
            )

        val state: StateFlow<ChallengeUiState> =
            combine(
                combine(
                    repository.observeActiveChallenges(),
                    repository.observeArchivedChallenges(),
                ) { active, archived ->
                    active to archived
                },
                combine(selectedChallengeFlow, selectedProgressEntriesFlow) { selectedChallenge, progressEntries ->
                    selectedChallenge to progressEntries
                },
                editorState,
                todayFlow,
            ) { challengeLists, selectedState, editor, today ->
                val (active, archived) = challengeLists
                val (selectedChallenge, progressEntries) = selectedState
                val calculation =
                    selectedChallenge?.let {
                        calculator.calculate(
                            challenge = it,
                            progressEntries = progressEntries,
                            today = today,
                        )
                    }

                ChallengeUiState(
                    activeChallenges = active,
                    archivedChallenges = archived,
                    selectedChallengeId = selectedChallenge?.id,
                    selectedChallenge = selectedChallenge,
                    progressEntries = progressEntries,
                    calculation = calculation,
                    editorState = editor,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = ChallengeUiState(),
            )

        fun selectChallenge(challengeId: Long?) {
            selectedChallengeId.value = challengeId
        }

        fun beginCreateChallenge() {
            editorState.value = defaultEditorState(today = LocalDate.now(clock))
        }

        fun beginEditChallenge(challengeId: Long) {
            viewModelScope.launch {
                val challenge = currentChallenge(challengeId) ?: return@launch
                editorState.value =
                    ChallengeEditorState(
                        challengeId = challenge.id,
                        title = challenge.title,
                        description = challenge.description.orEmpty(),
                        targetQuantityText = ChallengeQuantity.format(challenge.targetQuantity, Locale.getDefault()),
                        unit = challenge.unit,
                        startDate = challenge.startDate,
                        endDate = challenge.endDate,
                        lifecycle = challenge.lifecycle,
                        isDirty = false,
                        validationMessage = null,
                    )
            }
        }

        fun updateEditorTitle(title: String) {
            updateEditor { copy(title = title, isDirty = true, validationMessage = null) }
        }

        fun updateEditorDescription(description: String) {
            updateEditor { copy(description = description, isDirty = true, validationMessage = null) }
        }

        fun updateEditorTargetQuantity(targetQuantityText: String) {
            updateEditor { copy(targetQuantityText = targetQuantityText, isDirty = true, validationMessage = null) }
        }

        fun updateEditorUnit(unit: String) {
            updateEditor { copy(unit = unit, isDirty = true, validationMessage = null) }
        }

        fun updateEditorStartDate(date: LocalDate?) {
            updateEditor { copy(startDate = date, isDirty = true, validationMessage = null) }
        }

        fun updateEditorEndDate(date: LocalDate?) {
            updateEditor { copy(endDate = date, isDirty = true, validationMessage = null) }
        }

        fun updateEditorLifecycle(lifecycle: ChallengeLifecycle) {
            updateEditor { copy(lifecycle = lifecycle, isDirty = true, validationMessage = null) }
        }

        fun saveEditorChallenge() {
            viewModelScope.launch {
                actionMutex.withLock {
                    val editor = editorState.value
                    val title = editor.title.trim()
                    val description = editor.description.trim()
                    val unit = editor.unit.trim()
                    val startDate = editor.startDate
                    val endDate = editor.endDate
                    val targetQuantity =
                        ChallengeQuantity.parseLocalized(editor.targetQuantityText, Locale.getDefault())

                    when {
                        title.isBlank() -> {
                            setEditorValidation(R.string.challenge_validation_title_required)
                            return@withLock
                        }

                        unit.isBlank() -> {
                            setEditorValidation(R.string.challenge_validation_unit_required)
                            return@withLock
                        }

                        targetQuantity == null -> {
                            setEditorValidation(R.string.challenge_validation_quantity_required)
                            return@withLock
                        }

                        startDate == null || endDate == null -> {
                            setEditorValidation(R.string.challenge_validation_dates_required)
                            return@withLock
                        }

                        startDate.isAfter(endDate) -> {
                            setEditorValidation(R.string.challenge_validation_date_range_invalid)
                            return@withLock
                        }
                    }

                    val now = Instant.now(clock)
                    val existing = editor.challengeId?.let { repository.getChallenge(it) }
                    val wasCompleted = editor.challengeId?.let { completionState(it) }
                    val challenge =
                        Challenge(
                            id = existing?.id ?: 0L,
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            targetQuantity = targetQuantity,
                            unit = unit,
                            startDate = startDate!!,
                            endDate = endDate!!,
                            lifecycle = editor.lifecycle,
                            archivedAt = existing?.archivedAt,
                            createdAt = existing?.createdAt ?: now,
                            updatedAt = now,
                        )

                    if (existing == null) {
                        val challengeId = repository.insertChallenge(challenge)
                        userActionLogger.log(
                            actionType = CREATE_CHALLENGE,
                            entityType = CHALLENGE,
                            entityId = challengeId,
                            metadata = challengeMetadata(challenge, challengeId = challengeId),
                        )
                        selectedChallengeId.value = challengeId
                    } else {
                        repository.updateChallenge(challenge.copy(id = existing.id, createdAt = existing.createdAt))
                        val isCompleted = completionState(existing.id)
                        userActionLogger.log(
                            actionType = UPDATE_CHALLENGE,
                            entityType = CHALLENGE,
                            entityId = existing.id,
                            metadata =
                                challengeMetadata(
                                    challenge.copy(id = existing.id, createdAt = existing.createdAt),
                                    challengeId = existing.id,
                                ) +
                                    mapOf(
                                        CHALLENGE_OLD_VALUE to existing.targetQuantity.toString(),
                                        CHALLENGE_NEW_VALUE to challenge.targetQuantity.toString(),
                                        CHALLENGE_OLD_DATE to existing.endDate.toString(),
                                        CHALLENGE_NEW_DATE to challenge.endDate.toString(),
                                        CHALLENGE_OLD_STATUS to existing.lifecycle.name,
                                        CHALLENGE_NEW_STATUS to challenge.lifecycle.name,
                                    ) +
                                    completionMetadata(wasCompleted, isCompleted),
                        )
                    }

                    editorState.value = defaultEditorState(today = LocalDate.now(clock))
                }
            }
        }

        fun archiveChallenge(challengeId: Long) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val challenge = repository.getChallenge(challengeId) ?: return@withLock
                    if (challenge.lifecycle == ChallengeLifecycle.ARCHIVED) return@withLock
                    val wasCompleted = completionState(challengeId)
                    val archivedAt = Instant.now(clock)
                    repository.archiveChallenge(challengeId, archivedAt)
                    userActionLogger.log(
                        actionType = ARCHIVE_CHALLENGE,
                        entityType = CHALLENGE,
                        entityId = challengeId,
                        metadata =
                            challengeMetadata(
                                challenge.copy(lifecycle = ChallengeLifecycle.ARCHIVED, archivedAt = archivedAt),
                                challengeId,
                            ) +
                                mapOf(
                                    CHALLENGE_ARCHIVED_AT to archivedAt.toString(),
                                ) +
                                completionMetadata(wasCompleted, wasCompleted),
                    )
                }
            }
        }

        fun reactivateChallenge(challengeId: Long) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val challenge = repository.getChallenge(challengeId) ?: return@withLock
                    if (challenge.lifecycle == ChallengeLifecycle.ACTIVE) return@withLock
                    val wasCompleted = completionState(challengeId)
                    repository.reactivateChallenge(challengeId)
                    val isCompleted = completionState(challengeId)
                    userActionLogger.log(
                        actionType = REACTIVATE_CHALLENGE,
                        entityType = CHALLENGE,
                        entityId = challengeId,
                        metadata =
                            challengeMetadata(challenge.copy(lifecycle = ChallengeLifecycle.ACTIVE, archivedAt = null), challengeId) +
                                completionMetadata(wasCompleted, isCompleted),
                    )
                }
            }
        }

        fun deleteChallenge(challengeId: Long) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val challenge = repository.getChallenge(challengeId) ?: return@withLock
                    val progressEntries = repository.getProgressEntries(challengeId)
                    val wasCompleted = completionState(challengeId)
                    repository.deleteChallenge(challengeId)
                    setUndoAction(
                        action = PendingChallengeUndoAction.DeleteChallenge(challenge = challenge, progressEntries = progressEntries),
                        message = ChallengeUndoMessage.DeletedChallenge,
                    )
                    userActionLogger.log(
                        actionType = DELETE_CHALLENGE,
                        entityType = CHALLENGE,
                        entityId = challengeId,
                        metadata = challengeMetadata(challenge, challengeId) + completionMetadata(wasCompleted, false),
                    )
                    if (selectedChallengeId.value == challengeId) {
                        selectedChallengeId.value = null
                    }
                }
            }
        }

        fun addProgressEntry(
            challengeId: Long,
            quantityText: String,
            entryDate: LocalDate,
        ) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val challenge = repository.getChallenge(challengeId) ?: return@withLock
                    if (!canEditProgress(challenge, entryDate)) {
                        setEditorValidation(R.string.challenge_validation_progress_date_invalid)
                        return@withLock
                    }

                    val quantity = ChallengeQuantity.parseLocalized(quantityText, Locale.getDefault())
                    if (quantity == null) {
                        setEditorValidation(R.string.challenge_validation_quantity_required)
                        return@withLock
                    }

                    val wasCompleted = completionState(challengeId)
                    val now = Instant.now(clock)
                    val entry =
                        ChallengeProgressEntry(
                            id = 0L,
                            challengeId = challengeId,
                            quantity = quantity,
                            entryDate = entryDate,
                            occurredAt = now,
                            createdAt = now,
                            updatedAt = now,
                        )
                    val entryId = repository.insertProgressEntry(entry)
                    val isCompleted = completionState(challengeId)
                    userActionLogger.log(
                        actionType = CREATE_CHALLENGE_PROGRESS_ENTRY,
                        entityType = CHALLENGE,
                        entityId = entryId,
                        metadata =
                            challengeMetadata(challenge, challengeId) +
                                mapOf(
                                    CHALLENGE_PROGRESS_ENTRY_ID to entryId.toString(),
                                    CHALLENGE_PROGRESS_QUANTITY to quantity.toString(),
                                    CHALLENGE_PROGRESS_DATE to entryDate.toString(),
                                    CHALLENGE_FIRST_COMPLETION_AT to (state.value.calculation?.firstCompletionAt?.toString().orEmpty()),
                                    CHALLENGE_RECOVERED to (state.value.calculation?.recoveredCompletionAt != null).toString(),
                                ) +
                                completionMetadata(wasCompleted, isCompleted),
                    )
                }
            }
        }

        fun updateProgressEntry(
            entryId: Long,
            quantityText: String,
            entryDate: LocalDate,
        ) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val currentEntry = currentProgressEntry(entryId) ?: return@withLock
                    val challenge = repository.getChallenge(currentEntry.challengeId) ?: return@withLock
                    if (!canEditProgress(challenge, entryDate)) {
                        setEditorValidation(R.string.challenge_validation_progress_date_invalid)
                        return@withLock
                    }

                    val quantity = ChallengeQuantity.parseLocalized(quantityText, Locale.getDefault())
                    if (quantity == null) {
                        setEditorValidation(R.string.challenge_validation_quantity_required)
                        return@withLock
                    }

                    val wasCompleted = completionState(challenge.id)
                    val now = Instant.now(clock)
                    repository.updateProgressEntry(
                        currentEntry.copy(
                            quantity = quantity,
                            entryDate = entryDate,
                            updatedAt = now,
                        ),
                    )
                    val isCompleted = completionState(challenge.id)
                    userActionLogger.log(
                        actionType = UPDATE_CHALLENGE_PROGRESS_ENTRY,
                        entityType = CHALLENGE,
                        entityId = entryId,
                        metadata =
                            challengeMetadata(challenge, challenge.id) +
                                mapOf(
                                    CHALLENGE_PROGRESS_ENTRY_ID to entryId.toString(),
                                    CHALLENGE_OLD_VALUE to currentEntry.quantity.toString(),
                                    CHALLENGE_NEW_VALUE to quantity.toString(),
                                    CHALLENGE_OLD_DATE to currentEntry.entryDate.toString(),
                                    CHALLENGE_NEW_DATE to entryDate.toString(),
                                ) +
                                completionMetadata(wasCompleted, isCompleted),
                    )
                }
            }
        }

        fun deleteProgressEntry(entryId: Long) {
            viewModelScope.launch {
                actionMutex.withLock {
                    val entry = currentProgressEntry(entryId) ?: return@withLock
                    val wasCompleted = completionState(entry.challengeId)
                    repository.deleteProgressEntry(entryId)
                    setUndoAction(
                        action = PendingChallengeUndoAction.DeleteProgressEntry(challengeId = entry.challengeId, entry = entry),
                        message = ChallengeUndoMessage.DeletedProgressEntry,
                    )
                    val isCompleted = completionState(entry.challengeId)
                    userActionLogger.log(
                        actionType = DELETE_CHALLENGE_PROGRESS_ENTRY,
                        entityType = CHALLENGE,
                        entityId = entryId,
                        metadata =
                            mapOf(
                                CHALLENGE_ID to entry.challengeId.toString(),
                                CHALLENGE_PROGRESS_ENTRY_ID to entryId.toString(),
                                CHALLENGE_PROGRESS_QUANTITY to entry.quantity.toString(),
                                CHALLENGE_PROGRESS_DATE to entry.entryDate.toString(),
                            ) +
                                completionMetadata(wasCompleted, isCompleted),
                    )
                }
            }
        }

        fun restoreUndo() {
            val currentUndo = undoState.value ?: return
            clearUndoTimeout()

            viewModelScope.launch {
                actionMutex.withLock {
                    when (val action = currentUndo.action) {
                        is PendingChallengeUndoAction.DeleteChallenge -> {
                            repository.insertChallenge(action.challenge)
                            if (action.progressEntries.isNotEmpty()) {
                                action.progressEntries.forEach { repository.restoreProgressEntry(it) }
                            }
                            val wasCompleted = false
                            val isCompleted = completionState(action.challenge.id)
                            userActionLogger.log(
                                actionType = RESTORE_CHALLENGE_PROGRESS_ENTRY,
                                entityType = CHALLENGE,
                                entityId = action.challenge.id,
                                metadata =
                                    challengeMetadata(action.challenge, action.challenge.id) +
                                        completionMetadata(wasCompleted, isCompleted),
                            )
                            selectedChallengeId.value = action.challenge.id
                        }

                        is PendingChallengeUndoAction.DeleteProgressEntry -> {
                            val wasCompleted = completionState(action.entry.challengeId)
                            repository.restoreProgressEntry(action.entry)
                            val isCompleted = completionState(action.entry.challengeId)
                            userActionLogger.log(
                                actionType = RESTORE_CHALLENGE_PROGRESS_ENTRY,
                                entityType = CHALLENGE,
                                entityId = action.entry.id,
                                metadata =
                                    mapOf(
                                        CHALLENGE_ID to action.entry.challengeId.toString(),
                                        CHALLENGE_PROGRESS_ENTRY_ID to action.entry.id.toString(),
                                        CHALLENGE_PROGRESS_QUANTITY to action.entry.quantity.toString(),
                                        CHALLENGE_PROGRESS_DATE to action.entry.entryDate.toString(),
                                    ) +
                                        completionMetadata(wasCompleted, isCompleted),
                            )
                        }
                    }
                    undoState.value = null
                }
            }
        }

        fun clearUndo() {
            clearUndoTimeout()
            undoState.value = null
        }

        fun clearValidationMessage() {
            updateEditor { copy(validationMessage = null) }
        }

        private fun updateEditor(transform: ChallengeEditorState.() -> ChallengeEditorState) {
            editorState.value = editorState.value.transform()
        }

        private suspend fun currentChallenge(challengeId: Long): Challenge? {
            return state.value.activeChallenges.firstOrNull { it.id == challengeId }
                ?: state.value.archivedChallenges.firstOrNull { it.id == challengeId }
                ?: state.value.selectedChallenge?.takeIf { it.id == challengeId }
                ?: repository.getChallenge(challengeId)
        }

        private suspend fun currentProgressEntry(entryId: Long): ChallengeProgressEntry? {
            return state.value.progressEntries.firstOrNull { it.id == entryId }
                ?: state.value.selectedChallengeId?.let { challengeId ->
                    repository.getProgressEntries(challengeId).firstOrNull { it.id == entryId }
                }
        }

        private fun canEditProgress(
            challenge: Challenge,
            entryDate: LocalDate,
        ): Boolean {
            val today = LocalDate.now(clock)
            return entryDate <= today && !entryDate.isBefore(challenge.startDate) && !entryDate.isAfter(challenge.endDate)
        }

        private fun challengeMetadata(
            challenge: Challenge,
            challengeId: Long,
        ): Map<String, String> {
            return buildMap {
                put(CHALLENGE_ID, challengeId.toString())
                put(CHALLENGE_TITLE, challenge.title)
                challenge.description?.let { put(CHALLENGE_DESCRIPTION, it) }
                put(CHALLENGE_UNIT, challenge.unit)
                put(CHALLENGE_TARGET_QUANTITY, challenge.targetQuantity.toString())
                put(CHALLENGE_START_DATE, challenge.startDate.toString())
                put(CHALLENGE_END_DATE, challenge.endDate.toString())
                put(CHALLENGE_LIFECYCLE, challenge.lifecycle.name)
                challenge.archivedAt?.let { put(CHALLENGE_ARCHIVED_AT, it.toString()) }
            }
        }

        private suspend fun completionState(challengeId: Long): Boolean? {
            val challenge = repository.getChallenge(challengeId) ?: return null
            val entries = repository.getProgressEntries(challengeId)
            val result = calculator.calculate(challenge, entries, LocalDate.now(clock))
            return result.status == com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus.COMPLETED ||
                result.status == com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus.EXCEEDED
        }

        private fun completionMetadata(
            wasCompleted: Boolean?,
            isCompleted: Boolean?,
        ): Map<String, String> {
            return buildMap {
                wasCompleted?.let { put(WAS_COMPLETED, it.toString()) }
                isCompleted?.let { put(IS_COMPLETED, it.toString()) }
            }
        }

        private fun setEditorValidation(messageResId: Int) {
            editorState.value = editorState.value.copy(validationMessage = stringProvider.get(messageResId))
        }

        private fun setUndoAction(
            action: PendingChallengeUndoAction,
            message: ChallengeUndoMessage,
        ) {
            val newId = ++undoCounter
            undoState.value = ChallengeUndoState(id = newId, message = message, action = action)
            scheduleUndoTimeout(newId)
        }

        private fun scheduleUndoTimeout(undoId: Long) {
            clearUndoTimeout()
            undoTimeoutJob =
                viewModelScope.launch {
                    delay(UNDO_TIMEOUT_MS)
                    if (undoState.value?.id == undoId) {
                        undoState.value = null
                    }
                }
        }

        private fun clearUndoTimeout() {
            undoTimeoutJob?.cancel()
            undoTimeoutJob = null
        }

        private fun defaultEditorState(today: LocalDate): ChallengeEditorState {
            return ChallengeEditorState(
                challengeId = null,
                title = EMPTY,
                description = EMPTY,
                targetQuantityText = EMPTY,
                unit = EMPTY,
                startDate = today,
                endDate = today.plusDays(29),
                lifecycle = ChallengeLifecycle.ACTIVE,
                isDirty = false,
                validationMessage = null,
            )
        }

        private fun delayUntilNextMidnight(clock: Clock): Long {
            val zone = clock.zone
            val now = LocalDateTime.now(clock)
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            val millis = Duration.between(now.atZone(zone), nextMidnight).toMillis()
            return millis.coerceAtLeast(1L)
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
            const val UNDO_TIMEOUT_MS = 5_000L
        }
    }
