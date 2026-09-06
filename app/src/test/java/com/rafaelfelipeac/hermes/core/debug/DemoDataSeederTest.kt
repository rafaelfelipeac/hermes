@file:Suppress(
    "ArgumentListWrapping",
    "CyclomaticComplexMethod",
    "ImportOrdering",
    "LongParameterList",
    "MaxLineLength",
    "NoUnusedImports",
    "PropertyWrapping",
    "UnusedImports",
    "UnusedPrivateMember",
    "Wrapping",
)

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_END_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_LIFECYCLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.features.backup.data.BackupJsonCodec
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeDao
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DemoDataSeederTest {
    @Test
    fun seed_createsImportableSnapshotWithValidReferences() =
        runTest {
            val harness = createHarness()

            assertTrue(harness.seeder.seed())

            assertEquals(7, harness.categoryRepository.categories.value.size)
            assertEquals(6, harness.personalRecordDao.families.value.size)
            assertEquals(18, harness.personalRecordDao.entries.value.size)
            assertEquals(6, harness.challengeRepository.challenges.value.size)
            assertEquals(31, harness.challengeRepository.progressEntries.value.size)

            val snapshot = harness.snapshot()

            assertCoreReferences(snapshot)
            assertActivityReferences(snapshot)
            assertImportable(snapshot)
        }

    @Test
    fun seedChallenges_keepsChallengeReferencesValid() =
        runTest {
            val harness = createHarness()

            assertTrue(harness.seeder.seedChallenges())

            val snapshot = harness.snapshot()

            assertEquals(7, snapshot.categories.size)
            assertEquals(6, snapshot.challenges.size)
            assertEquals(31, snapshot.challengeProgressEntries.size)
            assertCoreReferences(snapshot)
            assertImportable(snapshot)
        }

    @Test
    fun seedLockedTrophies_keepsWorkoutCategoryReferencesValid() =
        runTest {
            val harness = createHarness()

            assertTrue(harness.seeder.seedLockedTrophies())

            val snapshot = harness.snapshot()
            val categoryIds = snapshot.categories.mapTo(mutableSetOf()) { it.id }

            assertEquals(24, snapshot.workouts.size)
            assertTrue(snapshot.workouts.all { workout ->
                workout.categoryId == null || workout.categoryId in categoryIds
            })
            assertTrue(snapshot.userActions.isEmpty())
            assertTrue(snapshot.personalRecordFamilies.isEmpty())
            assertTrue(snapshot.personalRecordEntries.isEmpty())
            assertTrue(snapshot.challenges.isEmpty())
            assertTrue(snapshot.challengeProgressEntries.isEmpty())
            assertImportable(snapshot)
        }

    @Test
    fun seedCompletedTrophies_generatesImportableSnapshotAndValidActivityReferences() =
        runTest {
            val harness = createHarness()

            assertTrue(harness.seeder.seedCompletedTrophies())

            val snapshot = harness.snapshot()

            assertCoreReferences(snapshot)
            assertCompletedTrophyActivityReferences(snapshot)
            assertImportable(snapshot)

            val challengeCreationActions =
                snapshot.userActions.filter { it.actionType == "CREATE_CHALLENGE" }
            assertTrue(challengeCreationActions.size >= 15)
            assertTrue(
                challengeCreationActions.all { action ->
                    val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
                    metadata[CHALLENGE_ID] == action.entityId.toString() &&
                        metadata[CHALLENGE_TITLE]?.isNotBlank() == true &&
                        metadata[CHALLENGE_TARGET_TYPE] == ChallengeTargetType.TOTAL.name &&
                        metadata[CHALLENGE_TARGET_QUANTITY]?.toLongOrNull() != null &&
                        metadata[CHALLENGE_START_DATE]?.isNotBlank() == true &&
                        metadata[CHALLENGE_END_DATE]?.isNotBlank() == true &&
                        metadata[CHALLENGE_LIFECYCLE] == ChallengeLifecycle.ACTIVE.name
                },
            )
        }

    private fun assertCoreReferences(snapshot: BackupSnapshot) {
        val categoryIds = snapshot.categories.mapTo(mutableSetOf()) { it.id }
        val familyIds = snapshot.personalRecordFamilies.mapTo(mutableSetOf()) { it.id }
        val entryIds = snapshot.personalRecordEntries.mapTo(mutableSetOf()) { it.id }
        val challengeIds = snapshot.challenges.mapTo(mutableSetOf()) { it.id }

        assertTrue(snapshot.workouts.all { workout ->
            workout.categoryId == null || workout.categoryId in categoryIds
        })
        assertTrue(snapshot.challenges.all { challenge ->
            challenge.categoryId == null || challenge.categoryId in categoryIds
        })
        assertTrue(snapshot.challengeProgressEntries.all { entry ->
            entry.challengeId in challengeIds
        })
        assertTrue(snapshot.personalRecordFamilies.all { family ->
            family.categoryId == null || family.categoryId in categoryIds
        })
        assertTrue(snapshot.personalRecordEntries.all { entry ->
            entry.familyId in familyIds
        })
        assertTrue(
            snapshot.personalRecordFamilies.all { family ->
                family.manualCurrentEntryId == null || family.manualCurrentEntryId in entryIds
            },
        )
        assertTrue(
            snapshot.personalRecordFamilies.all { family ->
                family.manualCurrentEntryId == null ||
                    snapshot.personalRecordEntries.first { it.id == family.manualCurrentEntryId }.familyId == family.id
            },
        )
    }

    private fun assertActivityReferences(snapshot: BackupSnapshot) {
        val categoryIds = snapshot.categories.mapTo(mutableSetOf()) { it.id }
        val familyIds = snapshot.personalRecordFamilies.mapTo(mutableSetOf()) { it.id }
        val entryIds = snapshot.personalRecordEntries.mapTo(mutableSetOf()) { it.id }

        val familyActions = snapshot.userActions.filter { it.actionType == "CREATE_PERSONAL_RECORD_FAMILY" }
        assertTrue(familyActions.all { action ->
            val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
            metadata[PERSONAL_RECORD_FAMILY_ID] == action.entityId.toString() &&
                metadata[PERSONAL_RECORD_CATEGORY_ID]?.toLongOrNull() in categoryIds
        })

        val entryActions = snapshot.userActions.filter { it.actionType == "CREATE_PERSONAL_RECORD_ENTRY" }
        assertTrue(entryActions.all { action ->
            val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
            val familyId = metadata[PERSONAL_RECORD_FAMILY_ID]?.toLongOrNull()
            val entryId = metadata[PERSONAL_RECORD_ENTRY_ID]?.toLongOrNull()
            familyId in familyIds &&
                entryId in entryIds &&
                metadata[PERSONAL_RECORD_FAMILY_TITLE]?.isNotBlank() == true
        })
    }

    private fun assertCompletedTrophyActivityReferences(snapshot: BackupSnapshot) {
        val categoryIds = snapshot.categories.mapTo(mutableSetOf()) { it.id }
        val familyActionIds =
            snapshot.userActions
                .filter { it.actionType == "CREATE_PERSONAL_RECORD_FAMILY" }
                .mapTo(mutableSetOf()) { it.entityId }

        val familyActions = snapshot.userActions.filter { it.actionType == "CREATE_PERSONAL_RECORD_FAMILY" }
        assertTrue(familyActions.all { action ->
            val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
            metadata[PERSONAL_RECORD_FAMILY_ID] == action.entityId.toString() &&
                metadata[PERSONAL_RECORD_CATEGORY_ID]?.toLongOrNull() in categoryIds
        })

        val entryActions = snapshot.userActions.filter { it.actionType == "CREATE_PERSONAL_RECORD_ENTRY" }
        assertTrue(entryActions.all { action ->
            val metadata = UserActionMetadataSerializer.fromJson(action.metadata)
            val familyId = metadata[PERSONAL_RECORD_FAMILY_ID]?.toLongOrNull()
            val entryId = metadata[PERSONAL_RECORD_ENTRY_ID]?.toLongOrNull()
            familyId in familyActionIds &&
                entryId == action.entityId &&
                metadata[PERSONAL_RECORD_FAMILY_TITLE]?.isNotBlank() == true
        })
    }

    private fun assertImportable(snapshot: BackupSnapshot) {
        val encoded = BackupJsonCodec.encode(snapshot)
        val decoded = BackupJsonCodec.decode(encoded)

        assertTrue(decoded is BackupDecodeResult.Success)
        val restored = (decoded as BackupDecodeResult.Success).snapshot
        assertEquals(snapshot.schemaVersion, restored.schemaVersion)
        assertEquals(snapshot.categories.size, restored.categories.size)
        assertEquals(snapshot.workouts.size, restored.workouts.size)
        assertEquals(snapshot.personalRecordFamilies.size, restored.personalRecordFamilies.size)
        assertEquals(snapshot.personalRecordEntries.size, restored.personalRecordEntries.size)
        assertEquals(snapshot.challenges.size, restored.challenges.size)
        assertEquals(snapshot.challengeProgressEntries.size, restored.challengeProgressEntries.size)
        assertEquals(snapshot.userActions.size, restored.userActions.size)
    }

    private fun createHarness(): Harness {
        val stringProvider = TestStringProvider
        val categoryRepository = FakeCategoryRepository()
        val categorySeeder = CategorySeeder(categoryRepository, stringProvider)
        val workoutDao = FakeWorkoutDao()
        val userActionDao = FakeUserActionDao()
        val personalRecordDao = FakePersonalRecordDao()
        val challengeRepository = FakeChallengeRepository()
        val settingsRepository = FakeSettingsRepository()

        return Harness(
            categoryRepository = categoryRepository,
            workoutDao = workoutDao,
            userActionDao = userActionDao,
            personalRecordDao = personalRecordDao,
            challengeRepository = challengeRepository,
            settingsRepository = settingsRepository,
            seeder =
                DemoDataSeeder(
                    workoutDao = workoutDao,
                    userActionDao = userActionDao,
                    personalRecordDao = personalRecordDao,
                    categorySeeder = categorySeeder,
                    challengeRepository = challengeRepository,
                    settingsRepository = settingsRepository,
                    workoutSeeder = DemoWorkoutSeeder(workoutDao, stringProvider),
                    personalRecordSeeder = DemoPersonalRecordSeeder(personalRecordDao, userActionDao, stringProvider),
                    activitySeeder = DemoActivitySeeder(userActionDao, stringProvider),
                    challengeSeeder = DemoChallengeSeeder(challengeRepository, stringProvider),
                    trophySeeder = DemoTrophySeeder(userActionDao, stringProvider),
                ),
        )
    }

    private inner class Harness(
        val categoryRepository: FakeCategoryRepository,
        val workoutDao: FakeWorkoutDao,
        val userActionDao: FakeUserActionDao,
        val personalRecordDao: FakePersonalRecordDao,
        val challengeRepository: FakeChallengeRepository,
        val settingsRepository: FakeSettingsRepository,
        val seeder: DemoDataSeeder,
    ) {
        fun snapshot(): BackupSnapshot {
            return BackupSnapshot(
                schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V6,
                exportedAt = "2026-09-06T12:00:00Z",
                workouts = workoutDao.workouts.value.map { it.toBackupRecord() },
                categories = categoryRepository.categories.value.map { it.toBackupRecord() },
                personalRecordFamilies = personalRecordDao.families.value.map { it.toBackupRecord() },
                personalRecordEntries = personalRecordDao.entries.value.map { it.toBackupRecord() },
                userActions = userActionDao.actions.value.mapIndexed { index, action ->
                    action.toBackupRecord(index + 1L)
                },
                challenges = challengeRepository.challenges.value.map { it.toBackupChallengeRecord() },
                challengeProgressEntries = challengeRepository.progressEntries.value.map { it.toBackupChallengeProgressRecord() },
                settings = settingsRepository.toBackupRecord(),
            )
        }
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

    private class FakeWorkoutDao : WorkoutDao {
        val workouts = MutableStateFlow(emptyList<WorkoutEntity>())
        private var nextId = 1L

        override suspend fun getAll(): List<WorkoutEntity> = workouts.value

        override fun observeAll(): Flow<List<WorkoutEntity>> = MutableStateFlow(workouts.value)

        override fun observeByEventType(eventType: String): Flow<List<WorkoutEntity>> =
            MutableStateFlow(workouts.value.filter { it.eventType == eventType })

        override suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<WorkoutEntity> =
            workouts.value.filter { it.weekStartDate == weekStartDate }

        override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<WorkoutEntity>> =
            MutableStateFlow(workouts.value.filter { it.weekStartDate == weekStartDate })

        override suspend fun getWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): List<WorkoutEntity> =
            workouts.value.filter { it.weekStartDate in weekStartDates }

        override fun observeWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): Flow<List<WorkoutEntity>> =
            MutableStateFlow(workouts.value.filter { it.weekStartDate in weekStartDates })

        override suspend fun insert(workout: WorkoutEntity): Long {
            val stored = workout.copy(id = nextId++)
            workouts.value = workouts.value + stored
            return stored.id
        }

        override suspend fun insertAll(workouts: List<WorkoutEntity>): List<Long> {
            return workouts.map { insert(it) }
        }

        override suspend fun insertAllReplace(workouts: List<WorkoutEntity>): List<Long> {
            return workouts.map { insertOrReplace(it) }
        }

        override suspend fun insertOrReplace(workout: WorkoutEntity): Long {
            val id = if (workout.id == 0L) nextId++ else workout.id
            val stored = workout.copy(id = id)
            nextId = maxOf(nextId, id + 1)
            workouts.value = workouts.value.filterNot { it.id == id } + stored
            return id
        }

        override suspend fun update(workout: WorkoutEntity) {
            workouts.value = workouts.value.map { if (it.id == workout.id) workout else it }
        }

        override suspend fun updateCompletion(
            id: Long,
            isCompleted: Boolean,
        ) {
            workouts.value =
                workouts.value.map {
                    if (it.id == id) it.copy(isCompleted = isCompleted) else it
                }
        }

        override suspend fun updateDayAndOrder(
            id: Long,
            dayOfWeek: Int?,
            timeSlot: String?,
            order: Int,
        ) {
            workouts.value =
                workouts.value.map {
                    if (it.id == id) {
                        it.copy(dayOfWeek = dayOfWeek, timeSlot = timeSlot, sortOrder = order)
                    } else {
                        it
                    }
                }
        }

        override suspend fun updateSchedule(
            id: Long,
            weekStartDate: LocalDate,
            dayOfWeek: Int?,
            timeSlot: String?,
            order: Int,
        ) {
            workouts.value =
                workouts.value.map {
                    if (it.id == id) {
                        it.copy(
                            weekStartDate = weekStartDate,
                            dayOfWeek = dayOfWeek,
                            timeSlot = timeSlot,
                            sortOrder = order,
                        )
                    } else {
                        it
                    }
                }
        }

        override suspend fun updateDetails(
            id: Long,
            type: String,
            description: String,
            isRestDay: Boolean,
            eventType: String,
            categoryId: Long?,
        ) {
            workouts.value =
                workouts.value.map {
                    if (it.id == id) {
                        it.copy(
                            type = type,
                            description = description,
                            isRestDay = isRestDay,
                            eventType = eventType,
                            categoryId = categoryId,
                        )
                    } else {
                        it
                    }
                }
        }

        override suspend fun assignNullCategoryTo(uncategorizedId: Long) {
            workouts.value =
                workouts.value.map {
                    if (it.categoryId == null && !it.isRestDay) it.copy(categoryId = uncategorizedId) else it
                }
        }

        override suspend fun reassignCategory(
            deletedCategoryId: Long,
            uncategorizedId: Long,
        ) {
            workouts.value =
                workouts.value.map {
                    if (it.categoryId == deletedCategoryId) it.copy(categoryId = uncategorizedId) else it
                }
        }

        override suspend fun deleteById(id: Long) {
            workouts.value = workouts.value.filterNot { it.id == id }
        }

        override suspend fun deleteByIds(ids: List<Long>) {
            workouts.value = workouts.value.filterNot { it.id in ids }
        }

        override suspend fun deleteByWeekStartDate(weekStartDate: LocalDate) {
            workouts.value = workouts.value.filterNot { it.weekStartDate == weekStartDate }
        }

        override suspend fun deleteAll() {
            workouts.value = emptyList()
        }
    }

    private class FakeUserActionDao : UserActionDao {
        val actions = MutableStateFlow(emptyList<UserActionEntity>())
        private var nextId = 1L

        override suspend fun getAll(): List<UserActionEntity> = actions.value.sortedByDescending { it.timestamp }

        override suspend fun insert(action: UserActionEntity): Long {
            val stored = action.copy(id = nextId++)
            actions.value = actions.value + stored
            return stored.id
        }

        override suspend fun insertAll(actions: List<UserActionEntity>): List<Long> {
            return actions.map { insert(it) }
        }

        override fun observeAll(): Flow<List<UserActionEntity>> =
            MutableStateFlow(actions.value.sortedByDescending { it.timestamp })

        override suspend fun deleteAll() {
            actions.value = emptyList()
        }
    }

    private class FakePersonalRecordDao : PersonalRecordDao {
        val families = MutableStateFlow(emptyList<PersonalRecordFamilyEntity>())
        val entries = MutableStateFlow(emptyList<PersonalRecordEntryEntity>())
        private var nextFamilyId = 1L
        private var nextEntryId = 1L

        override fun observeFamilies(): Flow<List<PersonalRecordFamilyEntity>> = families

        override fun observeEntries(): Flow<List<PersonalRecordEntryEntity>> = entries

        override fun observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntryEntity>> =
            MutableStateFlow(entries.value.filter { it.familyId == familyId })

        override suspend fun getFamilies(): List<PersonalRecordFamilyEntity> = families.value

        override suspend fun getEntries(): List<PersonalRecordEntryEntity> = entries.value

        override suspend fun getFamily(id: Long): PersonalRecordFamilyEntity? =
            families.value.firstOrNull { it.id == id }

        override suspend fun getEntry(id: Long): PersonalRecordEntryEntity? =
            entries.value.firstOrNull { it.id == id }

        override suspend fun insertFamily(family: PersonalRecordFamilyEntity): Long {
            val stored = family.copy(id = nextFamilyId++)
            families.value = families.value + stored
            return stored.id
        }

        override suspend fun insertFamilies(families: List<PersonalRecordFamilyEntity>): List<Long> {
            return families.map { insertFamily(it) }
        }

        override suspend fun updateFamily(family: PersonalRecordFamilyEntity) {
            families.value = families.value.map { if (it.id == family.id) family else it }
        }

        override suspend fun reassignCategory(
            categoryId: Long,
            newCategoryId: Long?,
        ) {
            families.value =
                families.value.map {
                    if (it.categoryId == categoryId) it.copy(categoryId = newCategoryId) else it
                }
        }

        override suspend fun deleteFamily(id: Long) {
            families.value = families.value.filterNot { it.id == id }
        }

        override suspend fun insertEntry(entry: PersonalRecordEntryEntity): Long {
            val stored = entry.copy(id = nextEntryId++)
            entries.value = entries.value + stored
            return stored.id
        }

        override suspend fun insertEntries(entries: List<PersonalRecordEntryEntity>): List<Long> {
            return entries.map { insertEntry(it) }
        }

        override suspend fun updateEntry(entry: PersonalRecordEntryEntity) {
            entries.value = entries.value.map { if (it.id == entry.id) entry else it }
        }

        override suspend fun deleteEntry(id: Long) {
            entries.value = entries.value.filterNot { it.id == id }
        }

        override suspend fun deleteEntriesForFamily(familyId: Long) {
            entries.value = entries.value.filterNot { it.familyId == familyId }
        }

        override suspend fun deleteAllFamilies() {
            families.value = emptyList()
        }

        override suspend fun deleteAllEntries() {
            entries.value = emptyList()
        }
    }

    private class FakeChallengeRepository : ChallengeRepository {
        val challenges = MutableStateFlow(emptyList<Challenge>())
        val progressEntries = MutableStateFlow(emptyList<ChallengeProgressEntry>())
        private var nextChallengeId = 1L
        private var nextProgressEntryId = 1L

        override fun observeActiveChallenges(): Flow<List<Challenge>> =
            MutableStateFlow(challenges.value.filter { it.lifecycle == ChallengeLifecycle.ACTIVE })

        override fun observeArchivedChallenges(): Flow<List<Challenge>> =
            MutableStateFlow(challenges.value.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED })

        override fun observeChallenge(id: Long): Flow<Challenge?> =
            MutableStateFlow(challenges.value.firstOrNull { it.id == id })

        override fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntry>> =
            MutableStateFlow(progressEntries.value.filter { it.challengeId == challengeId })

        override fun observeAllProgressEntries(): Flow<List<ChallengeProgressEntry>> = progressEntries

        override suspend fun getActiveChallenges(): List<Challenge> =
            challenges.value.filter { it.lifecycle == ChallengeLifecycle.ACTIVE }

        override suspend fun getArchivedChallenges(): List<Challenge> =
            challenges.value.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED }

        override suspend fun getChallenge(id: Long): Challenge? =
            challenges.value.firstOrNull { it.id == id }

        override suspend fun getChallengeDateBounds(id: Long) = error("Not needed in test")

        override suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntry> =
            progressEntries.value.filter { it.challengeId == challengeId }

        override suspend fun getAllChallenges(): List<Challenge> = challenges.value

        override suspend fun getAllProgressEntries(): List<ChallengeProgressEntry> = progressEntries.value

        override suspend fun insertChallenge(challenge: Challenge): Long {
            val stored = challenge.copy(id = nextChallengeId++)
            challenges.value = challenges.value + stored
            return stored.id
        }

        override suspend fun updateChallenge(challenge: Challenge) {
            challenges.value = challenges.value.map { if (it.id == challenge.id) challenge else it }
        }

        override suspend fun archiveChallenge(
            id: Long,
            archivedAt: Instant,
        ) {
            challenges.value =
                challenges.value.map {
                    if (it.id == id) it.copy(lifecycle = ChallengeLifecycle.ARCHIVED, archivedAt = archivedAt) else it
                }
        }

        override suspend fun reactivateChallenge(id: Long) {
            challenges.value =
                challenges.value.map {
                    if (it.id == id) it.copy(lifecycle = ChallengeLifecycle.ACTIVE, archivedAt = null) else it
                }
        }

        override suspend fun deleteChallenge(id: Long) {
            challenges.value = challenges.value.filterNot { it.id == id }
        }

        override suspend fun insertProgressEntry(entry: ChallengeProgressEntry): Long {
            val stored = entry.copy(id = nextProgressEntryId++)
            progressEntries.value = progressEntries.value + stored
            return stored.id
        }

        override suspend fun restoreProgressEntry(entry: ChallengeProgressEntry): Long {
            return insertProgressEntry(entry)
        }

        override suspend fun updateProgressEntry(entry: ChallengeProgressEntry) {
            progressEntries.value = progressEntries.value.map { if (it.id == entry.id) entry else it }
        }

        override suspend fun deleteProgressEntry(id: Long) {
            progressEntries.value = progressEntries.value.filterNot { it.id == id }
        }

        override suspend fun replaceChallenges(challenges: List<Challenge>) {
            this.challenges.value = challenges
        }

        override suspend fun replaceProgressEntries(entries: List<ChallengeProgressEntry>) {
            progressEntries.value = entries
        }

        override suspend fun deleteAllChallenges() {
            challenges.value = emptyList()
        }

        override suspend fun deleteAllProgressEntries() {
            progressEntries.value = emptyList()
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode: MutableStateFlow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM)
        override val language: MutableStateFlow<AppLanguage> = MutableStateFlow(AppLanguage.SYSTEM)
        override val slotModePolicy: MutableStateFlow<SlotModePolicy> = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
        override val weekStartDay: MutableStateFlow<WeekStartDay> = MutableStateFlow(WeekStartDay.MONDAY)
        override val distanceUnit: MutableStateFlow<DistanceUnit> = MutableStateFlow(DistanceUnit.KILOMETERS)
        override val paceUnit: MutableStateFlow<PaceUnit> = MutableStateFlow(PaceUnit.MIN_PER_KM)
        override val weightUnit: MutableStateFlow<WeightUnit> = MutableStateFlow(WeightUnit.KILOGRAMS)
        override val lastBackupExportedAt: MutableStateFlow<String?> = MutableStateFlow(null)
        override val lastBackupImportedAt: MutableStateFlow<String?> = MutableStateFlow(null)
        override val backupFolderUri: MutableStateFlow<String?> = MutableStateFlow(null)
        override val lastSeenTrophyCelebrationToken: MutableStateFlow<String?> = MutableStateFlow(null)

        override fun initialThemeMode(): ThemeMode = ThemeMode.SYSTEM

        override fun initialLanguage(): AppLanguage = AppLanguage.SYSTEM

        override fun initialSlotModePolicy(): SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE

        override fun initialWeekStartDay(): WeekStartDay = WeekStartDay.MONDAY

        override fun initialDistanceUnit(): DistanceUnit = DistanceUnit.KILOMETERS

        override fun initialPaceUnit(): PaceUnit = PaceUnit.MIN_PER_KM

        override fun initialWeightUnit(): WeightUnit = WeightUnit.KILOGRAMS

        override suspend fun setThemeMode(mode: ThemeMode) {
            themeMode.value = mode
        }

        override suspend fun setLanguage(language: AppLanguage) {
            this.language.value = language
        }

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) {
            slotModePolicy.value = policy
        }

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) {
            this.weekStartDay.value = weekStartDay
        }

        override suspend fun setDistanceUnit(distanceUnit: DistanceUnit) {
            this.distanceUnit.value = distanceUnit
        }

        override suspend fun setPaceUnit(paceUnit: PaceUnit) {
            this.paceUnit.value = paceUnit
        }

        override suspend fun setWeightUnit(weightUnit: WeightUnit) {
            this.weightUnit.value = weightUnit
        }

        override suspend fun setLastBackupExportedAt(value: String) {
            lastBackupExportedAt.value = value
        }

        override suspend fun setLastBackupImportedAt(value: String) {
            lastBackupImportedAt.value = value
        }

        override suspend fun setBackupFolderUri(value: String?) {
            backupFolderUri.value = value
        }

        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) {
            lastSeenTrophyCelebrationToken.value = value
        }
    }

    private fun WorkoutEntity.toBackupRecord(): BackupWorkoutRecord {
        return BackupWorkoutRecord(
            id = id,
            weekStartDate = weekStartDate.toString(),
            dayOfWeek = dayOfWeek,
            timeSlot = timeSlot,
            sortOrder = sortOrder,
            eventType = eventType,
            type = type,
            description = description,
            isCompleted = isCompleted,
            categoryId = categoryId,
        )
    }

    private fun Category.toBackupRecord(): BackupCategoryRecord {
        return BackupCategoryRecord(
            id = id,
            name = name,
            colorId = colorId,
            sortOrder = sortOrder,
            isHidden = isHidden,
            isSystem = isSystem,
        )
    }

    private fun PersonalRecordFamilyEntity.toBackupRecord(): BackupPersonalRecordFamilyRecord {
        return BackupPersonalRecordFamilyRecord(
            id = id,
            categoryId = categoryId,
            title = title,
            metricType = metricType.name,
            defaultUnit = defaultUnit.name,
            comparisonRule = comparisonRule.name,
            manualCurrentEntryId = manualCurrentEntryId,
            sortOrder = sortOrder,
            createdAt = Instant.ofEpochMilli(createdAt).toString(),
            updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
        )
    }

    private fun PersonalRecordEntryEntity.toBackupRecord(): BackupPersonalRecordEntryRecord {
        return BackupPersonalRecordEntryRecord(
            id = id,
            familyId = familyId,
            value = value,
            unit = unit.name,
            customUnitLabel = customUnitLabel,
            recordDate = recordDate.toString(),
            note = note,
            createdAt = Instant.ofEpochMilli(createdAt).toString(),
            updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
        )
    }

    private fun Challenge.toBackupChallengeRecord(): BackupChallengeRecord {
        return BackupChallengeRecord(
            id = id,
            categoryId = categoryId,
            title = title,
            description = description,
            targetType = targetType.name,
            targetQuantity = targetQuantity,
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            lifecycle = lifecycle.name,
            archivedAt = archivedAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
        )
    }

    private fun ChallengeProgressEntry.toBackupChallengeProgressRecord(): BackupChallengeProgressEntryRecord {
        return BackupChallengeProgressEntryRecord(
            id = id,
            challengeId = challengeId,
            quantity = quantity,
            entryDate = entryDate.toString(),
            occurredAt = occurredAt.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
        )
    }

    private fun UserActionEntity.toBackupRecord(id: Long): BackupUserActionRecord {
        return BackupUserActionRecord(
            id = id,
            actionType = actionType,
            entityType = entityType,
            entityId = entityId,
            metadata = metadata,
            timestamp = timestamp,
        )
    }

    private fun FakeSettingsRepository.toBackupRecord(): BackupSettingsRecord? {
        return BackupSettingsRecord(
            themeMode = themeMode.value.name,
            languageTag = language.value.tag,
            slotModePolicy = slotModePolicy.value.name,
            weekStartDay = weekStartDay.value.name,
            distanceUnit = distanceUnit.value.name,
            paceUnit = paceUnit.value.name,
            weightUnit = weightUnit.value.name,
        )
    }

    private object TestStringProvider : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String {
            return when (id) {
                R.string.category_uncategorized -> "Uncategorized"
                R.string.categories_category_run -> "Run"
                R.string.categories_category_cycling -> "Cycling"
                R.string.categories_category_strength -> "Strength"
                R.string.categories_category_swim -> "Swim"
                R.string.categories_category_mobility -> "Mobility"
                R.string.category_other -> "Other"
                R.string.mock_workout_type_strength -> "Strength"
                R.string.mock_workout_type_upper -> "Upper"
                R.string.mock_workout_type_cardio -> "Cardio"
                R.string.mock_workout_type_yoga -> "Yoga"
                R.string.mock_workout_type_hiits -> "HIITs"
                R.string.mock_workout_type_mobility -> "Mobility"
                R.string.mock_workout_type_long_run -> "Long run"
                R.string.mock_workout_type_core -> "Core"
                R.string.mock_workout_description_strength -> "Strength session"
                R.string.mock_workout_description_upper -> "Upper session"
                R.string.mock_workout_description_cardio -> "Cardio session"
                R.string.mock_workout_description_yoga -> "Yoga session"
                R.string.mock_workout_description_hiits -> "HIITs session"
                R.string.mock_workout_description_mobility -> "Mobility session"
                R.string.mock_workout_description_long_run -> "Long run session"
                R.string.mock_workout_description_core -> "Core session"
                R.string.mock_personal_record_5_km -> "5K PR"
                R.string.mock_personal_record_longest_run -> "Longest run"
                R.string.mock_personal_record_deadlift -> "Deadlift"
                R.string.mock_personal_record_cycling_power -> "Cycling power"
                R.string.mock_personal_record_push_ups -> "Push-ups"
                R.string.mock_personal_record_weekly_consistency -> "Weekly consistency"
                R.string.mock_personal_record_sessions_unit -> "sessions"
                else -> id.toString()
            }
        }

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = get(id, *args)
    }
}
