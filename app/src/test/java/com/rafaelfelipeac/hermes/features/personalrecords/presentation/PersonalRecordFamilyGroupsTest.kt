package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PersonalRecordFamilyGroupsTest {
    @Test
    fun buildFamilyGroups_ordersCategoriesAndFamiliesAndPlacesUncategorizedLast() {
        val laterCategory = category(id = 2L, sortOrder = 2, colorId = COLOR_CYCLING)
        val firstCategory = category(id = 1L, sortOrder = 1, colorId = COLOR_RUN)
        val groups =
            buildFamilyGroups(
                categories = listOf(laterCategory, firstCategory),
                families =
                    listOf(
                        family(id = 20L, categoryId = 2L, sortOrder = 0),
                        family(id = 12L, categoryId = 1L, sortOrder = 2),
                        family(id = 11L, categoryId = 1L, sortOrder = 1),
                        family(id = 30L, categoryId = null, sortOrder = 0),
                    ),
            )

        assertEquals(listOf(1L, 2L, null), groups.map { it.category?.id })
        assertEquals(listOf(11L, 12L), groups.first().families.map { it.id })
    }

    @Test
    fun buildFamilyGroups_omitsCategoriesWithoutFamilies() {
        val groups =
            buildFamilyGroups(
                categories = listOf(category(id = 1L, sortOrder = 0, colorId = COLOR_RUN)),
                families = listOf(family(id = 30L, categoryId = null, sortOrder = 0)),
            )

        assertEquals(1, groups.size)
        assertEquals(null, groups.single().category)
    }

    private fun category(
        id: Long,
        sortOrder: Int,
        colorId: String,
    ) = Category(
        id = id,
        name = "Category $id",
        colorId = colorId,
        sortOrder = sortOrder,
        isHidden = false,
        isSystem = false,
    )

    private fun family(
        id: Long,
        categoryId: Long?,
        sortOrder: Int,
    ) = PersonalRecordFamily(
        id = id,
        categoryId = categoryId,
        title = "Family $id",
        metricType = DISTANCE,
        defaultUnit = KILOMETER,
        comparisonRule = HIGHER_IS_BETTER,
        manualCurrentEntryId = null,
        sortOrder = sortOrder,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
