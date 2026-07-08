package com.rafaelfelipeac.hermes.features.personalrecords.domain.model

import java.time.Instant
import java.time.LocalDate

data class PersonalRecordEntry(
    val id: Long,
    val familyId: Long,
    val value: Double,
    val unit: PersonalRecordUnit,
    val customUnitLabel: String?,
    val recordDate: LocalDate,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
