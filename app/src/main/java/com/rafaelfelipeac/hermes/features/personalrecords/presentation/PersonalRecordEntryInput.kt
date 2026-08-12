package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import java.time.LocalDate

data class PersonalRecordEntryInput(
    val familyId: Long,
    val value: Double,
    val unit: PersonalRecordUnit,
    val recordDate: LocalDate,
    val note: String?,
    val customUnitLabel: String? = null,
)
