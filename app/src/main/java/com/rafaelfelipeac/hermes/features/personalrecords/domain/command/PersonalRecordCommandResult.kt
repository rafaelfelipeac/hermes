package com.rafaelfelipeac.hermes.features.personalrecords.domain.command

sealed interface PersonalRecordCommandResult {
    data object Changed : PersonalRecordCommandResult

    data object NoChange : PersonalRecordCommandResult
}
