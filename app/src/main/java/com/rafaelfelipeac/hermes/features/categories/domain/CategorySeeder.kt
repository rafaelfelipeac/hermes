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
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
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

            restoreDefaults()
        }

        suspend fun restoreDefaults(): Int {
            val existing = repository.getCategories()
            val existingIds = existing.map { it.id }.toSet()
            var nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val defaults = buildStarterCategories()
            val missing =
                defaults.filter { category ->
                    category.id !in existingIds
                }

            missing.forEach { category ->
                repository.insertCategory(category.copy(sortOrder = nextOrder++))
            }

            return missing.size
        }

        suspend fun syncLocalizedNames(
            previousLanguage: AppLanguage? = null,
            newLanguage: AppLanguage? = null,
            force: Boolean = false,
        ) {
            val existing = repository.getCategories()
            val defaultsById = buildStarterCategories().associateBy { it.id }
            val previousTag = languageTag(previousLanguage)
            val newTag = languageTag(newLanguage)

            existing.forEach { category ->
                if (!category.isSystem) return@forEach

                val defaults = defaultsById[category.id] ?: return@forEach
                val previousName = getDefaultNameForLanguage(category.id, previousTag)
                val newName = getDefaultNameForLanguage(category.id, newTag)

                if (force && newName != null) {
                    if (category.name != newName) {
                        repository.updateCategoryName(category.id, newName)
                    }
                    return@forEach
                }

                if (previousName != null && newName != null && category.name == previousName && category.name != newName) {
                    repository.updateCategoryName(category.id, newName)
                }
            }
        }

        suspend fun syncDefaultColors() {
            val existing = repository.getCategories()
            val defaultsById = buildStarterCategories().associateBy { it.id }

            existing.forEach { category ->
                if (!category.isSystem) return@forEach
                val defaults = defaultsById[category.id] ?: return@forEach
                if (category.colorId != defaults.colorId) {
                    repository.updateCategoryColor(category.id, defaults.colorId)
                }
            }
        }

        private fun getDefaultNameForLanguage(
            categoryId: Long,
            languageTag: String?,
        ): String? {
            return when (categoryId) {
                UNCATEGORIZED_ID -> stringProvider.getForLanguage(languageTag, R.string.category_uncategorized)
                2L -> stringProvider.getForLanguage(languageTag, R.string.categories_category_run)
                3L -> stringProvider.getForLanguage(languageTag, R.string.categories_category_cycling)
                4L -> stringProvider.getForLanguage(languageTag, R.string.categories_category_strength)
                5L -> stringProvider.getForLanguage(languageTag, R.string.categories_category_swim)
                6L -> stringProvider.getForLanguage(languageTag, R.string.categories_category_mobility)
                7L -> stringProvider.getForLanguage(languageTag, R.string.category_other)
                else -> null
            }
        }

        private fun languageTag(language: AppLanguage?): String? {
            return when (language) {
                null -> null
                AppLanguage.SYSTEM -> null
                else -> language.tag
            }
        }

        private fun buildStarterCategories(): List<Category> {
            return listOf(
                buildUncategorizedCategory(),
                Category(
                    id = 2L,
                    name = stringProvider.get(R.string.categories_category_run),
                    colorId = COLOR_RUN,
                    sortOrder = 1,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 3L,
                    name = stringProvider.get(R.string.categories_category_cycling),
                    colorId = COLOR_CYCLING,
                    sortOrder = 2,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 4L,
                    name = stringProvider.get(R.string.categories_category_strength),
                    colorId = COLOR_STRENGTH,
                    sortOrder = 3,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 5L,
                    name = stringProvider.get(R.string.categories_category_swim),
                    colorId = COLOR_SWIM,
                    sortOrder = 4,
                    isHidden = false,
                    isSystem = true,
                ),
                Category(
                    id = 6L,
                    name = stringProvider.get(R.string.categories_category_mobility),
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
