package com.rafaelfelipeac.hermes.features.categories.domain

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.ENGLISH
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.PORTUGUESE_BRAZIL
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategorySeederTest {
    @Test
    fun syncLocalizedNames_updatesSystemNameWhenMatchesPrevious() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val stringProvider = mockk<StringProvider>(relaxed = true)

            every { stringProvider.getForLanguage("en", R.string.categories_category_run) } returns "Run"
            every { stringProvider.getForLanguage("pt-BR", R.string.categories_category_run) } returns "Corrida"

            coEvery { repository.getCategories() } returns listOf(systemCategory(name = "Run"))

            val seeder = CategorySeeder(repository, stringProvider)

            seeder.syncLocalizedNames(previousLanguage = ENGLISH, newLanguage = PORTUGUESE_BRAZIL)

            coVerify(exactly = 1) { repository.updateCategoryName(2L, "Corrida") }
        }

    @Test
    fun syncLocalizedNames_doesNotUpdateCustomName() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val stringProvider = mockk<StringProvider>(relaxed = true)

            every { stringProvider.getForLanguage("en", R.string.categories_category_run) } returns "Run"
            every { stringProvider.getForLanguage("pt-BR", R.string.categories_category_run) } returns "Corrida"

            coEvery { repository.getCategories() } returns listOf(systemCategory(name = "My Run"))

            val seeder = CategorySeeder(repository, stringProvider)

            seeder.syncLocalizedNames(previousLanguage = ENGLISH, newLanguage = PORTUGUESE_BRAZIL)

            coVerify(exactly = 0) { repository.updateCategoryName(any(), any()) }
        }

    @Test
    fun syncLocalizedNames_forceUpdatesCustomName() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val stringProvider = mockk<StringProvider>(relaxed = true)

            every { stringProvider.getForLanguage("pt-BR", R.string.categories_category_run) } returns "Corrida"

            coEvery { repository.getCategories() } returns listOf(systemCategory(name = "My Run"))

            val seeder = CategorySeeder(repository, stringProvider)

            seeder.syncLocalizedNames(newLanguage = PORTUGUESE_BRAZIL, force = true)

            coVerify(exactly = 1) { repository.updateCategoryName(2L, "Corrida") }
        }

    @Test
    fun syncDefaultColors_updatesSystemColorsOnly() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val stringProvider = mockk<StringProvider>(relaxed = true)

            val categories =
                listOf(
                    systemCategory(name = "Run", colorId = "pink"),
                    nonSystemCategory(id = 99L, name = "Custom", colorId = "pink"),
                )

            coEvery { repository.getCategories() } returns categories

            val seeder = CategorySeeder(repository, stringProvider)

            seeder.syncDefaultColors()

            coVerify(exactly = 1) { repository.updateCategoryColor(2L, COLOR_RUN) }
            coVerify(exactly = 0) { repository.updateCategoryColor(99L, any()) }
        }

    @Test
    fun syncLocalizedNames_doesNotChangeColors() =
        runTest {
            val repository = mockk<CategoryRepository>(relaxed = true)
            val stringProvider = mockk<StringProvider>(relaxed = true)

            every { stringProvider.getForLanguage("en", R.string.categories_category_run) } returns "Run"
            every { stringProvider.getForLanguage("pt-BR", R.string.categories_category_run) } returns "Corrida"

            coEvery { repository.getCategories() } returns listOf(systemCategory(name = "Run", colorId = "pink"))

            val seeder = CategorySeeder(repository, stringProvider)

            seeder.syncLocalizedNames(previousLanguage = ENGLISH, newLanguage = PORTUGUESE_BRAZIL)

            coVerify(exactly = 0) { repository.updateCategoryColor(any(), any()) }
        }

    private fun systemCategory(
        id: Long = 2L,
        name: String,
        colorId: String = COLOR_RUN,
    ): Category {
        return Category(
            id = id,
            name = name,
            colorId = colorId,
            sortOrder = 1,
            isHidden = false,
            isSystem = true,
        )
    }

    private fun nonSystemCategory(
        id: Long,
        name: String,
        colorId: String,
    ): Category {
        return Category(
            id = id,
            name = name,
            colorId = colorId,
            sortOrder = 1,
            isHidden = false,
            isSystem = false,
        )
    }
}
