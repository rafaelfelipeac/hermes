package com.rafaelfelipeac.hermes.features.categories.domain

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_MOBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_OTHER
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_STRENGTH
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_SWIM
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_UNCATEGORIZED
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class CategorySeeder
    @Inject
    constructor(
        private val repository: CategoryRepository,
        private val stringProvider: StringProvider,
    ) {
        suspend fun ensureSeeded() {
            val count = repository.getCount()
            if (count == 0) {
                repository.insertCategories(buildStarterCategories())
                return
            }

            if (repository.getCategory(UNCATEGORIZED_ID) == null) {
                repository.insertCategory(buildUncategorizedCategory())
            }
        }

        private fun buildStarterCategories(): List<Category> {
            return listOf(
                buildUncategorizedCategory(),
                Category(
                    id = 2L,
                    name = stringProvider.get(R.string.category_run),
                    colorId = COLOR_RUN,
                    sortOrder = 1,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 3L,
                    name = stringProvider.get(R.string.category_cycling),
                    colorId = COLOR_CYCLING,
                    sortOrder = 2,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 4L,
                    name = stringProvider.get(R.string.category_strength),
                    colorId = COLOR_STRENGTH,
                    sortOrder = 3,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 5L,
                    name = stringProvider.get(R.string.category_swim),
                    colorId = COLOR_SWIM,
                    sortOrder = 4,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 6L,
                    name = stringProvider.get(R.string.category_mobility),
                    colorId = COLOR_MOBILITY,
                    sortOrder = 5,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 7L,
                    name = stringProvider.get(R.string.category_other),
                    colorId = COLOR_OTHER,
                    sortOrder = 6,
                    isHidden = false,
                    isSystem = true,
                ),
            )
        }

        private fun buildUncategorizedCategory(): Category {
            return Category(
                id = UNCATEGORIZED_ID,
                name = stringProvider.get(R.string.category_uncategorized),
                colorId = COLOR_UNCATEGORIZED,
                sortOrder = 0,
                isHidden = false,
                isSystem = true,
            )
        }
    }
