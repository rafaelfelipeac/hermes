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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategorySeederTest {
    @Test
    fun ensureSeeded_insertsStarterCategoriesForEmptyDatabase() =
        runTest {
            val repository = FakeCategoryRepository()
            val seeder = CategorySeeder(repository, FakeStringProvider())

            seeder.ensureSeeded()

            assertEquals(7, repository.categories.value.size)
        }

    @Test
    fun ensureSeeded_doesNotRestoreDeletedDefaultCategories() =
        runTest {
            val remainingCategory = systemCategory(id = 1L, name = "Uncategorized")
            val repository = FakeCategoryRepository(listOf(remainingCategory))
            val seeder = CategorySeeder(repository, FakeStringProvider())

            seeder.ensureSeeded()

            assertEquals(listOf(remainingCategory), repository.categories.value)
        }

    @Test
    fun restoreDefaults_recreatesDeletedDefaultCategories() =
        runTest {
            val remainingCategory = systemCategory(id = 1L, name = "Uncategorized")
            val repository = FakeCategoryRepository(listOf(remainingCategory))
            val seeder = CategorySeeder(repository, FakeStringProvider())

            val restoredCount = seeder.restoreDefaults()

            assertEquals(6, restoredCount)
            assertEquals(7, repository.categories.value.size)
        }

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

    private class FakeCategoryRepository(
        initialCategories: List<Category> = emptyList(),
    ) : CategoryRepository {
        val categories = MutableStateFlow(initialCategories)

        override fun observeCategories(): Flow<List<Category>> = categories

        override suspend fun getCategories(): List<Category> = categories.value

        override suspend fun getCategory(id: Long): Category? = categories.value.firstOrNull { it.id == id }

        override suspend fun getCount(): Int = categories.value.size

        override suspend fun insertCategory(category: Category): Long {
            categories.value += category
            return category.id
        }

        override suspend fun insertCategories(categories: List<Category>): List<Long> {
            this.categories.value += categories
            return categories.map(Category::id)
        }

        override suspend fun updateCategory(category: Category) = error("Not needed in test")

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) = error("Not needed in test")

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) = error("Not needed in test")

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) = error("Not needed in test")

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) = error("Not needed in test")

        override suspend fun deleteCategory(id: Long) = error("Not needed in test")
    }

    private class FakeStringProvider : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String = id.toString()

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = id.toString()
    }
}
