package com.rafaelfelipeac.hermes.features.challenges.presentation

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry

data class ChallengeUndoState(
    val id: Long,
    val message: ChallengeUndoMessage,
    val action: PendingChallengeUndoAction,
)

enum class ChallengeUndoMessage {
    DeletedChallenge,
    DeletedProgressEntry,
}

sealed class PendingChallengeUndoAction {
    data class DeleteChallenge(
        val challenge: Challenge,
        val progressEntries: List<ChallengeProgressEntry>,
    ) : PendingChallengeUndoAction()

    data class DeleteProgressEntry(
        val challengeId: Long,
        val entry: ChallengeProgressEntry,
    ) : PendingChallengeUndoAction()
}
