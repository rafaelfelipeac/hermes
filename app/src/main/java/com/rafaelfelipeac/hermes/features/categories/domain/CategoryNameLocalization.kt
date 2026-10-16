package com.rafaelfelipeac.hermes.features.categories.domain

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.MOBILITY_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.OTHER_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.SWIM_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage

internal object CategoryNameLocalization {
    fun localizedName(
        categoryId: Long,
        language: AppLanguage?,
        stringProvider: StringProvider,
    ): String? {
        val resourceId = resourceId(categoryId) ?: return null
        val languageTag = language?.takeUnless { it == AppLanguage.SYSTEM }?.tag
        return stringProvider.getForLanguage(languageTag, resourceId)
    }

    fun defaultNames(
        categoryId: Long,
        stringProvider: StringProvider,
    ): Set<String> {
        return AppLanguage.entries
            .asSequence()
            .filterNot { it == AppLanguage.SYSTEM }
            .mapNotNull { language -> localizedName(categoryId, language, stringProvider) }
            .toSet()
    }

    fun shouldTranslate(
        category: Category,
        targetName: String,
        defaultNames: Set<String>,
    ): Boolean {
        return category.isSystem &&
            category.name in defaultNames &&
            category.name != targetName
    }

    private fun resourceId(categoryId: Long): Int? {
        return when (categoryId) {
            UNCATEGORIZED_ID -> R.string.category_uncategorized
            RUN_ID -> R.string.categories_category_run
            CYCLING_ID -> R.string.categories_category_cycling
            STRENGTH_ID -> R.string.categories_category_strength
            SWIM_ID -> R.string.categories_category_swim
            MOBILITY_ID -> R.string.categories_category_mobility
            OTHER_ID -> R.string.category_other
            else -> null
        }
    }
}
