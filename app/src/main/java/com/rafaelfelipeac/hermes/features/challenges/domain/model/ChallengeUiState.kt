package com.rafaelfelipeac.hermes.features.challenges.domain.model

data class ChallengeUiState(
    val activeChallenges: List<Challenge> = emptyList(),
    val archivedChallenges: List<Challenge> = emptyList(),
    val challengeCalculations: Map<Long, ChallengeCalculationResult> = emptyMap(),
    val allProgressEntries: List<ChallengeProgressEntry> = emptyList(),
    val selectedChallengeId: Long? = null,
    val selectedChallenge: Challenge? = null,
    val progressEntries: List<ChallengeProgressEntry> = emptyList(),
    val calculation: ChallengeCalculationResult? = null,
    val editorState: ChallengeEditorState = ChallengeEditorState(),
    val isLoading: Boolean = false,
    val validationMessage: String? = null,
)
