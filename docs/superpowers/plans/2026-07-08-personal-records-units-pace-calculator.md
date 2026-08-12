# Personal Records, Units And Pace Calculator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build flexible category-first Personal Records with historical entries, basic unit preferences, and a separate Pace Calculator destination in Browse.

**Architecture:** Ship this in vertical slices so persisted contracts stabilize before UI depends on them. Units live in Settings/DataStore, Personal Records use Room tables plus repository/domain models, Backup exports/imports the new state in schema v5, Activity logs family/entry/unit mutations without free text, and Browse hosts two separate destinations.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Room, DataStore Preferences, Hilt, kotlinx.coroutines `StateFlow`, kotlinx.serialization JSON, existing Hermes fake-first test style.

---

## Source Spec

Read this before implementation:

- `/Users/rafaelcordeiro/Projects/hermes-docs/docs/specs/personal-records-pace-calculator-spec.md`
- `AGENTS.md`
- `docs/backup-compatibility-policy.md`

This work changes public navigation and persisted schema. The product decision has been approved in the spec discussion: two Browse cards, PR families plus entries, history, basic units in Settings, and no calculator-to-PR save in v1.

## File Structure

Create or modify these areas:

- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/DistanceUnit.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/PaceUnit.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/WeightUnit.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/repository/SettingsRepository.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/data/SettingsRepositoryImpl.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsState.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsViewModel.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsScreen.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/repository/PersonalRecordsRepository.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordBestSelector.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordValueNormalizer.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/local/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/PersonalRecordsRepositoryImpl.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/di/PersonalRecordsModule.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/pacecalculator/domain/PaceCalculator.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/pacecalculator/presentation/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/core/database/HermesDatabase.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/core/database/Migrations.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/model/UserActionType.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/model/UserActionEntityType.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/metadata/UserActionMetadataKeys.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/formatter/ActivityUiFormatter.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/ActivityActionFilterContext.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/domain/model/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/data/*`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/browse/presentation/BrowseDestination.kt`
- `app/src/main/java/com/rafaelfelipeac/hermes/features/browse/presentation/BrowseScreen.kt`
- all `app/src/main/res/values*/strings.xml`
- `docs/backup-compatibility-policy.md`
- `LEARNING.md`

## Task 1: Unit Preference Domain And Settings Repository

**Files:**
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/DistanceUnit.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/PaceUnit.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/model/WeightUnit.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/domain/repository/SettingsRepository.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/data/SettingsDataStore.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/data/SettingsRepositoryImpl.kt`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/settings/data/SettingsRepositoryImplTest.kt` if unit-testable with current patterns, otherwise add repository coverage through ViewModel in Task 2.

- [ ] **Step 1: Add unit enums**

Create:

```kotlin
package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class DistanceUnit {
    KILOMETERS,
    MILES,
}
```

Create:

```kotlin
package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class PaceUnit {
    MIN_PER_KM,
    MIN_PER_MI,
}
```

Create:

```kotlin
package com.rafaelfelipeac.hermes.features.settings.domain.model

enum class WeightUnit {
    KILOGRAMS,
    POUNDS,
}
```

- [ ] **Step 2: Extend repository contract**

Add flows, initial getters and setters to `SettingsRepository`:

```kotlin
val distanceUnit: Flow<DistanceUnit>
val paceUnit: Flow<PaceUnit>
val weightUnit: Flow<WeightUnit>

fun initialDistanceUnit(): DistanceUnit
fun initialPaceUnit(): PaceUnit
fun initialWeightUnit(): WeightUnit

suspend fun setDistanceUnit(unit: DistanceUnit)
suspend fun setPaceUnit(unit: PaceUnit)
suspend fun setWeightUnit(unit: WeightUnit)
```

- [ ] **Step 3: Add DataStore keys**

In `SettingsDataStore.kt`, add string preference keys following the existing style:

```kotlin
internal val DISTANCE_UNIT_KEY = stringPreferencesKey("distance_unit")
internal val PACE_UNIT_KEY = stringPreferencesKey("pace_unit")
internal val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")
```

- [ ] **Step 4: Implement repository defaults and persistence**

Default values:

```kotlin
private fun defaultDistanceUnit(): DistanceUnit = DistanceUnit.KILOMETERS
private fun defaultPaceUnit(): PaceUnit = PaceUnit.MIN_PER_KM
private fun defaultWeightUnit(): WeightUnit = WeightUnit.KILOGRAMS
```

Persist enum names using the same `runCatching { valueOf(raw) }.getOrNull()` pattern used for theme and slot mode.

- [ ] **Step 5: Run focused compile**

Run: `rtk ./gradlew :app:compileDebugKotlin`

Expected: compile fails only if imports or Settings contract updates are incomplete. Fix before moving on.

## Task 2: Unit Settings State, Logging And UI

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsState.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsScreen.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/model/UserActionType.kt`
- Modify: `app/src/main/res/values*/strings.xml`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/settings/presentation/SettingsViewModelTest.kt`

- [ ] **Step 1: Add failing ViewModel tests**

Add tests that verify:

- initial state exposes default distance, pace and weight units.
- `setDistanceUnit(MILES)` persists and logs `CHANGE_DISTANCE_UNIT`.
- `setPaceUnit(MIN_PER_MI)` persists and logs `CHANGE_PACE_UNIT`.
- `setWeightUnit(POUNDS)` persists and logs `CHANGE_WEIGHT_UNIT`.
- no log is emitted when setting the same current value.

Use existing fake repository/action logger patterns from `SettingsViewModelTest`.

- [ ] **Step 2: Add action types**

Append:

```kotlin
CHANGE_DISTANCE_UNIT,
CHANGE_PACE_UNIT,
CHANGE_WEIGHT_UNIT,
```

near the other Settings action types in `UserActionType`.

- [ ] **Step 3: Extend `SettingsState`**

Add:

```kotlin
val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
val paceUnit: PaceUnit = PaceUnit.MIN_PER_KM,
val weightUnit: WeightUnit = WeightUnit.KILOGRAMS,
```

- [ ] **Step 4: Combine unit flows into `SettingsViewModel.state`**

Include the three new flows in the existing settings combine chain and set initial values from repository initial getters.

- [ ] **Step 5: Add setters with Activity logging**

Add `setDistanceUnit`, `setPaceUnit`, and `setWeightUnit` methods mirroring `setThemeMode`:

```kotlin
if (previous != unit) {
    userActionLogger.log(
        actionType = CHANGE_DISTANCE_UNIT,
        entityType = SETTINGS,
        metadata = mapOf(OLD_VALUE to previous.name, NEW_VALUE to unit.name),
    )
}
```

- [ ] **Step 6: Add Settings UI entry**

Add a Units settings row or detail screen in `SettingsScreen.kt`. Keep it consistent with existing settings detail routes and use string resources for all labels.

- [ ] **Step 7: Run tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*SettingsViewModelTest"`

Expected: PASS.

## Task 3: Personal Records Domain Model And Best Selection

**Files:**
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/PersonalRecordFamily.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/PersonalRecordEntry.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/PersonalRecordMetricType.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/PersonalRecordUnit.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/model/PersonalRecordComparisonRule.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordValueNormalizer.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordBestSelector.kt`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordBestSelectorTest.kt`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/PersonalRecordValueNormalizerTest.kt`

- [ ] **Step 1: Write failing normalizer tests**

Cover:

- `1.0 KILOMETER` normalizes to `1000.0`.
- `1.0 MILE` normalizes to `1609.344`.
- `100.0 POUND` normalizes to kilograms using `45.359237`.
- `240.0 WATT` stays `240.0`.
- `52.0 REP` stays `52.0`.

- [ ] **Step 2: Implement metric/unit enums**

Use:

```kotlin
enum class PersonalRecordMetricType { DISTANCE, TIME, WEIGHT, POWER, REPS, CUSTOM }
enum class PersonalRecordComparisonRule { HIGHER_IS_BETTER, LOWER_IS_BETTER, MANUAL }
enum class PersonalRecordUnit { KILOMETER, MILE, METER, SECOND, MINUTE, HOUR, KILOGRAM, POUND, WATT, REP, CUSTOM }
```

- [ ] **Step 3: Implement normalizer**

Add a pure Kotlin object/function that returns canonical numeric values for comparison. Do not format display text here.

- [ ] **Step 4: Write failing best-selector tests**

Cover:

- higher-is-better chooses max normalized value.
- lower-is-better chooses min normalized value.
- manual uses `manualCurrentEntryId`.
- manual falls back to newest record date.
- ties prefer most recent `recordDate`.

- [ ] **Step 5: Implement domain models and selector**

Keep models in dedicated files, not ViewModel files. Selector should accept a family and entries and return `PersonalRecordEntry?`.

- [ ] **Step 6: Run focused tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*PersonalRecord*Test"`

Expected: PASS.

## Task 4: Room Tables, DAO, Repository And Migration

**Files:**
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/local/PersonalRecordFamilyEntity.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/local/PersonalRecordEntryEntity.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/local/PersonalRecordDao.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/data/PersonalRecordsRepositoryImpl.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/domain/repository/PersonalRecordsRepository.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/di/PersonalRecordsModule.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/database/HermesDatabase.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/database/Migrations.kt`
- Test: add repository Room tests under `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/data/`.

- [ ] **Step 1: Write migration/repository tests**

Add tests for:

- inserting a family and entries.
- observing families with entries.
- deleting a family deletes entries or repository deletes entries first.
- migration 4->5 creates both tables.

- [ ] **Step 2: Add entities**

Family table fields:

- `id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL`
- `categoryId INTEGER`
- `title TEXT NOT NULL`
- `metricType TEXT NOT NULL`
- `defaultUnit TEXT NOT NULL`
- `comparisonRule TEXT NOT NULL`
- `manualCurrentEntryId INTEGER`
- `sortOrder INTEGER NOT NULL`
- `createdAt INTEGER NOT NULL`
- `updatedAt INTEGER NOT NULL`

Entry table fields:

- `id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL`
- `familyId INTEGER NOT NULL`
- `value REAL NOT NULL`
- `unit TEXT NOT NULL`
- `customUnitLabel TEXT`
- `recordDate TEXT NOT NULL`
- `note TEXT`
- `createdAt INTEGER NOT NULL`
- `updatedAt INTEGER NOT NULL`

- [ ] **Step 3: Add DAO**

DAO should expose:

- `observeFamilies(): Flow<List<PersonalRecordFamilyEntity>>`
- `observeEntries(): Flow<List<PersonalRecordEntryEntity>>`
- `observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntryEntity>>`
- `insertFamily`
- `updateFamily`
- `deleteFamily`
- `insertEntry`
- `updateEntry`
- `deleteEntry`
- `getFamilies`
- `getEntries`

- [ ] **Step 4: Add migration 4->5**

Create `MIGRATION_4_5`, append it to `ALL_MIGRATIONS`, and bump `DATABASE_VERSION` to 5.

- [ ] **Step 5: Add repository and Hilt binding**

Repository maps entities to domain and keeps Room out of UI/ViewModels.

- [ ] **Step 6: Run data tests**

Run: `rtk ./gradlew :app:connectedDebugAndroidTest`

Expected: PASS for new repository/migration tests. If emulator is unavailable, run `rtk ./gradlew :app:compileDebugAndroidTestKotlin` and report that instrumentation was not executed.

## Task 5: Backup Schema V5

**Files:**
- Create: backup record models for Personal Record families and entries.
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/data/BackupV5Decoder.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/domain/model/BackupSnapshot.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/data/BackupJsonCodec.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/data/BackupJsonKeys.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/backup/data/BackupRepositoryImpl.kt`
- Modify: `docs/backup-compatibility-policy.md`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/backup/data/BackupJsonCodecTest.kt`
- Test: `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/backup/data/BackupRepositoryImplTest.kt`

- [ ] **Step 1: Write failing backup codec tests**

Cover:

- v5 encode includes `personalRecordFamilies`, `personalRecordEntries`, and unit settings.
- v5 decode returns families, entries, and units.
- v4 decode returns empty record family/entry lists and default units.

- [ ] **Step 2: Extend backup models and keys**

Add backup records mirroring the Room/entity fields but using JSON-friendly strings/numbers.

- [ ] **Step 3: Add `BackupV5Decoder`**

Use existing decoder style. Validate required arrays and enum strings.

- [ ] **Step 4: Update repository export/import**

Inject `PersonalRecordDao` or repository where consistent with existing backup code. Export/import categories before personal records so category references are valid.

- [ ] **Step 5: Update backup policy doc**

Add:

```markdown
- `schemaVersion = 5` adds Personal Record families, Personal Record entries, and unit preferences.
```

- [ ] **Step 6: Run backup tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*BackupJsonCodecTest"`

Expected: PASS.

## Task 6: Activity Logging And Formatter Coverage

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/model/UserActionType.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/model/UserActionEntityType.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/useraction/metadata/UserActionMetadataKeys.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/formatter/ActivityUiFormatter.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/ActivityActionFilterContext.kt`
- Modify: all `app/src/main/res/values*/strings.xml`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/activity/presentation/formatter/ActivityUiFormatterTest.kt`

- [ ] **Step 1: Add action/entity enums**

Add:

- `CREATE_PERSONAL_RECORD_FAMILY`
- `UPDATE_PERSONAL_RECORD_FAMILY`
- `DELETE_PERSONAL_RECORD_FAMILY`
- `CREATE_PERSONAL_RECORD_ENTRY`
- `UPDATE_PERSONAL_RECORD_ENTRY`
- `DELETE_PERSONAL_RECORD_ENTRY`
- `PERSONAL_RECORD` entity type

- [ ] **Step 2: Add metadata keys**

Add keys for family id, entry id, metric type, unit, comparison rule, record date, normalized value, and category data if missing.

- [ ] **Step 3: Add formatter tests**

Verify formatter creates localized titles/subtitles without exposing note body or user-authored family title.

- [ ] **Step 4: Implement formatter mappings**

Keep copy generic enough to avoid logging private text, for example "You added a personal record result." with metadata in subtitle when safe.

- [ ] **Step 5: Run formatter tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*ActivityUiFormatterTest"`

Expected: PASS.

## Task 7: Personal Records ViewModel And Presentation State

**Files:**
- Create presentation state/model files under `features/personalrecords/presentation/`.
- Create `PersonalRecordsViewModel.kt`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsViewModelTest.kt`

- [ ] **Step 1: Add fake repository and logger in tests**

Use fakes, not mocks. Fake repository should expose mutable flows for families, entries and categories as needed.

- [ ] **Step 2: Write ViewModel tests**

Cover:

- empty state.
- sections include only categories with families.
- uncategorized section appears only for uncategorized families.
- current best shown per family.
- create family logs action without title metadata.
- create entry logs action without note metadata.
- delete family deletes entries through repository path.

- [ ] **Step 3: Implement presentation state models**

Keep data classes in dedicated files, not inside `PersonalRecordsViewModel.kt`.

- [ ] **Step 4: Implement ViewModel**

Use `stateIn` with `SharingStarted.WhileSubscribed`, following Hermes conventions.

- [ ] **Step 5: Run ViewModel tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*PersonalRecordsViewModelTest"`

Expected: PASS.

## Task 8: Personal Records Compose UI

**Files:**
- Create `PersonalRecordsScreen.kt`
- Create focused supporting composables if the screen grows large.
- Modify `BrowseDestination.kt`
- Modify `BrowseScreen.kt`
- Modify `HermesAppContent.kt` only if destination back behavior needs special handling.
- Modify strings in all locales.
- Test: `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsScreenTest.kt`

- [ ] **Step 1: Add UI tests**

Cover:

- empty screen shows empty state and create action.
- category section appears when a family exists.
- category without families is absent.
- tapping a family opens detail.
- detail shows current best and history.

- [ ] **Step 2: Add Browse destination**

Add `PERSONAL_RECORDS` enum value and Browse card with localized strings.

- [ ] **Step 3: Implement shelf screen**

Use category sections and family cards. Avoid showing categories with no families.

- [ ] **Step 4: Implement family detail**

Show current best hero, history list, `Add result`, and `Edit family`.

- [ ] **Step 5: Implement create/edit dialogs or screens**

Use existing Hermes dialog patterns. All labels use string resources. All spacing uses `Dimens`.

- [ ] **Step 6: Run Compose compile**

Run: `rtk ./gradlew :app:compileDebugKotlin`

Expected: PASS.

## Task 9: Pace Calculator Domain And UI

**Files:**
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/pacecalculator/domain/PaceCalculator.kt`
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/features/pacecalculator/presentation/PaceCalculatorScreen.kt`
- Modify: `BrowseDestination.kt`
- Modify: `BrowseScreen.kt`
- Modify: all strings.
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/pacecalculator/domain/PaceCalculatorTest.kt`
- Test: `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/pacecalculator/presentation/PaceCalculatorScreenTest.kt`

- [ ] **Step 1: Add failing calculator tests**

Cover:

- distance + time -> pace.
- distance + pace -> finish time.
- time + pace -> distance.
- 5K preset converts to 5000 meters.
- 1 mile preset converts to 1609.344 meters.
- rounding displays stable minute/second values.

- [ ] **Step 2: Implement pure calculator**

Keep calculations in domain, not Compose.

- [ ] **Step 3: Add Browse destination and card**

Add `PACE_CALCULATOR` destination separate from `PERSONAL_RECORDS`.

- [ ] **Step 4: Implement UI**

Segmented modes:

- Pace.
- Time.
- Distance.

No save-to-PR action in v1.

- [ ] **Step 5: Run calculator tests**

Run: `rtk ./gradlew :app:testDebugUnitTest --tests "*PaceCalculatorTest"`

Expected: PASS.

## Task 10: Localization, Demo Data, Learning Notes And Final Verification

**Files:**
- Modify: all `app/src/main/res/values*/strings.xml`
- Modify: demo seeders only if useful for manual QA.
- Modify: `LEARNING.md`

- [ ] **Step 1: Audit string parity**

Run a resource key comparison across all `values*/strings.xml` files. Fix missing keys and avoid English placeholder text in localized files.

- [ ] **Step 2: Add optional demo data**

If manual QA needs populated PR shelves, add debug/demo records through an existing seeder path. Keep seed values realistic and category-backed.

- [ ] **Step 3: Update `LEARNING.md`**

Append a concise note explaining the family/entry split and why PRs are category-first instead of sport-enum-first.

- [ ] **Step 4: Run focused verification**

Run:

```bash
rtk ./gradlew :app:testDebugUnitTest --tests "*PersonalRecord*Test" --tests "*PaceCalculatorTest" --tests "*SettingsViewModelTest" --tests "*BackupJsonCodecTest" --tests "*ActivityUiFormatterTest"
rtk ./gradlew :app:compileDebugKotlin
rtk ./gradlew :app:detekt
rtk ./gradlew :app:lintDebug
```

Expected: all pass.

- [ ] **Step 5: Run instrumentation if an emulator is available**

Run: `rtk ./gradlew :app:connectedDebugAndroidTest`

Expected: PASS. If no emulator is available, record that instrumentation was not run.

## Plan Self-Review

Spec coverage:

- Two Browse cards: Tasks 8 and 9.
- Flexible category-first PRs: Tasks 3, 4, 7, 8.
- PR family plus historical entries: Tasks 3, 4, 7, 8.
- Current best by comparison rule: Task 3 and Task 7.
- Units in Settings: Tasks 1 and 2.
- Backup schema: Task 5.
- Activity logging: Task 6 and ViewModel tests in Task 7.
- Localization: Tasks 2, 6, 8, 9, 10.
- No calculator save-to-PR in v1: Task 9.

Known risk:

- This is a large schema-heavy release. Do not batch all tasks into one commit. Use task-level commits or checkpoint commits.
