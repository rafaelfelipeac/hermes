package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_FAMILY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_PERSONAL_RECORD_FAMILY
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.repository.PersonalRecordsRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalRecordsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addEntry_logsPersonalRecordSeriesTitle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val family = sampleFamily()
            val repository = FakePersonalRecordsRepository(initialFamilies = listOf(family))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, FakeCategoryRepository(), logger)

            viewModel.addEntry(
                PersonalRecordEntryInput(
                    familyId = family.id,
                    value = 5.0,
                    unit = KILOMETER,
                    recordDate = LocalDate.parse("2024-01-01"),
                    note = null,
                ),
            )
            advanceUntilIdle()

            assertEquals(CREATE_PERSONAL_RECORD_ENTRY, logger.actions.single().actionType)
            assertEquals(family.title, logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_TITLE))
        }

    @Test
    fun updateFamily_updatesRepositoryAndLogsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val category = sampleCategory(id = 2L, name = "Strength")
            val repository = FakePersonalRecordsRepository(initialFamilies = listOf(sampleFamily()))
            val categoryRepository = FakeCategoryRepository(initialCategories = listOf(sampleCategory(), category))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, categoryRepository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }

            viewModel.updateFamily(
                familyId = 1L,
                categoryId = category.id,
                title = "5K Run Updated",
                comparisonRule = HIGHER_IS_BETTER,
            )

            advanceUntilIdle()

            val updatedFamily = repository.families.single()
            assertEquals("5K Run Updated", updatedFamily.title)
            assertEquals(category.id, updatedFamily.categoryId)
            assertEquals(DISTANCE, updatedFamily.metricType)
            assertEquals(KILOMETER, updatedFamily.defaultUnit)
            assertEquals(UPDATE_PERSONAL_RECORD_FAMILY, logger.actions.single().actionType)
            assertTrue(logger.actions.single().metadata?.values?.contains("5K Run Updated") == false)
            assertEquals(category.id.toString(), logger.actions.single().metadata?.get(PERSONAL_RECORD_CATEGORY_ID))
            assertEquals(
                DISTANCE.name,
                logger.actions.single().metadata?.get(
                    PERSONAL_RECORD_METRIC_TYPE,
                ),
            )
            assertEquals(
                KILOMETER.name,
                logger.actions.single().metadata?.get(
                    com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT,
                ),
            )
            stateJob.cancel()
        }

    @Test
    fun deleteFamily_removesFamilyAndEntriesAndLogsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(sampleFamily()),
                    initialEntries = listOf(sampleEntry(familyId = 1L)),
                )
            val categoryRepository = FakeCategoryRepository(initialCategories = listOf(sampleCategory()))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, categoryRepository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }

            viewModel.deleteFamily(1L)

            advanceUntilIdle()

            assertTrue(repository.families.isEmpty())
            assertTrue(repository.entries.isEmpty())
            assertEquals(DELETE_PERSONAL_RECORD_FAMILY, logger.actions.single().actionType)
            assertEquals("1", logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_ID))
            stateJob.cancel()
        }

    @Test
    fun updateEntry_updatesRepositoryAndLogsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(sampleFamily(metricType = TIME, defaultUnit = SECOND)),
                    initialEntries = listOf(sampleEntry(familyId = 1L, unit = SECOND, value = 298.0)),
                )
            val categoryRepository = FakeCategoryRepository(initialCategories = listOf(sampleCategory()))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, categoryRepository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }
            val updatedDate = LocalDate.of(2024, 2, 1)

            viewModel.updateEntry(
                entryId = 10L,
                input =
                    PersonalRecordEntryInput(
                        familyId = 1L,
                        value = 305.0,
                        unit = SECOND,
                        recordDate = updatedDate,
                        note = "New PR",
                    ),
            )

            advanceUntilIdle()

            val updatedEntry = repository.entries.single()
            assertEquals(305.0, updatedEntry.value, 0.0)
            assertEquals(updatedDate, updatedEntry.recordDate)
            assertEquals("New PR", updatedEntry.note)
            assertEquals(UPDATE_PERSONAL_RECORD_ENTRY, logger.actions.single().actionType)
            assertEquals("298.0", logger.actions.single().metadata?.get(PERSONAL_RECORD_OLD_VALUE))
            assertEquals("305.0", logger.actions.single().metadata?.get(PERSONAL_RECORD_NEW_VALUE))
            assertEquals(updatedDate.toString(), logger.actions.single().metadata?.get(PERSONAL_RECORD_RECORD_DATE))
            assertEquals("5K", logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_TITLE))
            stateJob.cancel()
        }

    @Test
    fun updateEntry_keepsOriginalFamilyWhenInputRequestsAnotherFamily() =
        runTest(mainDispatcherRule.testDispatcher) {
            val originalFamily = sampleFamily(id = 1L)
            val otherFamily = sampleFamily(id = 2L)
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(originalFamily, otherFamily),
                    initialEntries = listOf(sampleEntry(familyId = originalFamily.id)),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, FakeCategoryRepository(), logger)

            viewModel.updateEntry(
                entryId = 10L,
                input =
                    PersonalRecordEntryInput(
                        familyId = otherFamily.id,
                        value = 6.0,
                        unit = KILOMETER,
                        recordDate = LocalDate.parse("2024-02-01"),
                        note = null,
                    ),
            )
            advanceUntilIdle()

            assertEquals(originalFamily.id, repository.entries.single().familyId)
            assertEquals(originalFamily.id.toString(), logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_ID))
        }

    @Test
    fun deleteEntry_removesEntryAndLogsAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(sampleFamily()),
                    initialEntries = listOf(sampleEntry(familyId = 1L)),
                )
            val categoryRepository = FakeCategoryRepository(initialCategories = listOf(sampleCategory()))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, categoryRepository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }

            viewModel.deleteEntry(10L)

            advanceUntilIdle()

            assertTrue(repository.entries.isEmpty())
            assertEquals(DELETE_PERSONAL_RECORD_ENTRY, logger.actions.single().actionType)
            assertEquals("10", logger.actions.single().metadata?.get(PERSONAL_RECORD_ENTRY_ID))
            assertEquals("1", logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_ID))
            assertEquals("5K", logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_TITLE))
            stateJob.cancel()
        }

    @Test
    fun setManualCurrentEntry_updatesSelectedEntryAndLogsSeriesTitle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val family = sampleFamily(comparisonRule = MANUAL)
            val entry = sampleEntry(familyId = family.id)
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(family),
                    initialEntries = listOf(entry),
                )
            val logger = RecordingUserActionLogger()
            val viewModel =
                createViewModel(
                    repository = repository,
                    categoryRepository = FakeCategoryRepository(),
                    logger = logger,
                )

            viewModel.setManualCurrentEntry(family.id, entry.id)
            advanceUntilIdle()

            assertEquals(entry.id, repository.families.single().manualCurrentEntryId)
            assertEquals(SET_CURRENT_PERSONAL_RECORD_ENTRY, logger.actions.single().actionType)
            assertEquals(entry.value.toString(), logger.actions.single().metadata?.get(PERSONAL_RECORD_NEW_VALUE))
            assertEquals(
                entry.recordDate.toString(),
                logger.actions.single().metadata?.get(PERSONAL_RECORD_RECORD_DATE),
            )
            assertEquals(family.title, logger.actions.single().metadata?.get(PERSONAL_RECORD_FAMILY_TITLE))
        }

    @Test
    fun deleteEntry_clearsManualCurrentEntryReference() =
        runTest(mainDispatcherRule.testDispatcher) {
            val family = sampleFamily(comparisonRule = MANUAL, manualCurrentEntryId = 10L)
            val repository =
                FakePersonalRecordsRepository(
                    initialFamilies = listOf(family),
                    initialEntries = listOf(sampleEntry(familyId = family.id)),
                )
            val viewModel =
                createViewModel(
                    repository = repository,
                    categoryRepository = FakeCategoryRepository(),
                    logger = RecordingUserActionLogger(),
                )

            viewModel.deleteEntry(10L)
            advanceUntilIdle()

            assertEquals(null, repository.families.single().manualCurrentEntryId)
        }

    private fun createViewModel(
        repository: FakePersonalRecordsRepository,
        categoryRepository: FakeCategoryRepository,
        logger: RecordingUserActionLogger,
    ) = PersonalRecordsViewModel(repository, categoryRepository, logger)

    private fun sampleCategory(
        id: Long = 1L,
        name: String = "Run",
    ) = Category(
        id = id,
        name = name,
        colorId = "color-$id",
        sortOrder = 0,
        isHidden = false,
        isSystem = true,
    )

    private fun sampleFamily(
        id: Long = 1L,
        categoryId: Long? = null,
        metricType: PersonalRecordMetricType = DISTANCE,
        defaultUnit: PersonalRecordUnit = KILOMETER,
        comparisonRule: PersonalRecordComparisonRule = HIGHER_IS_BETTER,
        manualCurrentEntryId: Long? = null,
    ) = PersonalRecordFamily(
        id = id,
        categoryId = categoryId,
        title = "5K",
        metricType = metricType,
        defaultUnit = defaultUnit,
        comparisonRule = comparisonRule,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = 0,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )

    private fun sampleEntry(
        id: Long = 10L,
        familyId: Long,
        unit: com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit = KILOMETER,
        value: Double = 5.0,
    ) = PersonalRecordEntry(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit,
        customUnitLabel = null,
        recordDate = LocalDate.parse("2024-01-01"),
        note = null,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )

    private class RecordingUserActionLogger : UserActionLogger {
        val actions = CopyOnWriteArrayList<UserAction>()

        override suspend fun log(action: UserAction) {
            actions += action
        }
    }

    private class FakeCategoryRepository(
        initialCategories: List<Category> = emptyList(),
    ) : CategoryRepository {
        private val categoriesFlow = MutableStateFlow(initialCategories)

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow.asStateFlow()

        override suspend fun getCategories(): List<Category> = categoriesFlow.value

        override suspend fun getCategory(id: Long): Category? = categoriesFlow.value.firstOrNull { it.id == id }

        override suspend fun getCount(): Int = categoriesFlow.value.size

        override suspend fun insertCategory(category: Category): Long {
            val nextId = (categoriesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
            categoriesFlow.value = categoriesFlow.value + category.copy(id = nextId)
            return nextId
        }

        override suspend fun insertCategories(categories: List<Category>): List<Long> {
            return categories.map { insertCategory(it) }
        }

        override suspend fun updateCategory(category: Category) {
            categoriesFlow.value = categoriesFlow.value.map { if (it.id == category.id) category else it }
        }

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) {
            categoriesFlow.value =
                categoriesFlow.value.map {
                    if (it.id == id) it.copy(name = name) else it
                }
        }

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) {
            categoriesFlow.value =
                categoriesFlow.value.map {
                    if (it.id == id) it.copy(colorId = colorId) else it
                }
        }

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) {
            categoriesFlow.value =
                categoriesFlow.value.map {
                    if (it.id == id) it.copy(isHidden = isHidden) else it
                }
        }

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) {
            categoriesFlow.value =
                categoriesFlow.value.map {
                    if (it.id == id) it.copy(sortOrder = sortOrder) else it
                }
        }

        override suspend fun deleteCategory(id: Long) {
            categoriesFlow.value = categoriesFlow.value.filterNot { it.id == id }
        }
    }

    private class FakePersonalRecordsRepository(
        initialFamilies: List<PersonalRecordFamily> = emptyList(),
        initialEntries: List<PersonalRecordEntry> = emptyList(),
    ) : PersonalRecordsRepository {
        private val familiesFlow = MutableStateFlow(initialFamilies)
        private val entriesFlow = MutableStateFlow(initialEntries)

        val families: List<PersonalRecordFamily>
            get() = familiesFlow.value

        val entries: List<PersonalRecordEntry>
            get() = entriesFlow.value

        override fun observeFamilies(): Flow<List<PersonalRecordFamily>> = familiesFlow.asStateFlow()

        override fun observeEntries(): Flow<List<PersonalRecordEntry>> = entriesFlow.asStateFlow()

        override fun observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntry>> =
            entriesFlow.map { entries -> entries.filter { it.familyId == familyId } }

        override suspend fun getFamilies(): List<PersonalRecordFamily> = families

        override suspend fun getEntries(): List<PersonalRecordEntry> = entries

        override suspend fun getFamily(id: Long): PersonalRecordFamily? = families.firstOrNull { it.id == id }

        override suspend fun getEntry(id: Long): PersonalRecordEntry? = entries.firstOrNull { it.id == id }

        override suspend fun insertFamily(family: PersonalRecordFamily): Long {
            val nextId = (families.maxOfOrNull { it.id } ?: 0L) + 1L
            familiesFlow.value = familiesFlow.value + family.copy(id = nextId)
            return nextId
        }

        override suspend fun updateFamily(family: PersonalRecordFamily) {
            familiesFlow.value = familiesFlow.value.map { if (it.id == family.id) family else it }
        }

        override suspend fun reassignCategory(
            categoryId: Long,
            newCategoryId: Long?,
        ) {
            familiesFlow.value =
                familiesFlow.value.map { family ->
                    if (family.categoryId == categoryId) family.copy(categoryId = newCategoryId) else family
                }
        }

        override suspend fun deleteFamily(id: Long) {
            familiesFlow.value = familiesFlow.value.filterNot { it.id == id }
            entriesFlow.value = entriesFlow.value.filterNot { it.familyId == id }
        }

        override suspend fun insertEntry(entry: PersonalRecordEntry): Long {
            val nextId = (entries.maxOfOrNull { it.id } ?: 0L) + 1L
            entriesFlow.value = entriesFlow.value + entry.copy(id = nextId)
            return nextId
        }

        override suspend fun updateEntry(entry: PersonalRecordEntry) {
            entriesFlow.value = entriesFlow.value.map { if (it.id == entry.id) entry else it }
        }

        override suspend fun deleteEntry(id: Long) {
            entriesFlow.value = entriesFlow.value.filterNot { it.id == id }
        }
    }
}
