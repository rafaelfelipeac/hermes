package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily

data class PersonalRecordsState(
    val categories: List<Category> = emptyList(),
    val families: List<PersonalRecordFamily> = emptyList(),
    val entries: List<PersonalRecordEntry> = emptyList(),
)
