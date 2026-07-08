package com.rafaelfelipeac.hermes.features.personalrecords.domain.model

import java.time.Instant

data class PersonalRecordFamily(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val metricType: PersonalRecordMetricType,
    val defaultUnit: PersonalRecordUnit,
    val comparisonRule: PersonalRecordComparisonRule,
    val manualCurrentEntryId: Long?,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
