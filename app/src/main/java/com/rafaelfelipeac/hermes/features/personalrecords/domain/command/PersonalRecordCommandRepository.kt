package com.rafaelfelipeac.hermes.features.personalrecords.domain.command

interface PersonalRecordCommandRepository {
    suspend fun deleteEntry(entryId: Long): PersonalRecordCommandResult
}
