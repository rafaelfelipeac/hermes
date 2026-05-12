# Project Architecture Audit

Date: 2026-05-11

Scope: whole-project research pass focused on organization, architecture, simplification, maintainability, and the path toward making Hermes a strong example Android codebase.

## Executive Summary

Hermes already has a healthy baseline: a single Android app module with Compose, Material 3, Hilt, Room, DataStore, Coroutines/Flow, `StateFlow` screen state, versioned backup decoders, Detekt, Ktlint, GitHub Actions, localized resources, and repo-local skills that encode project rules.

The main opportunity is not a rewrite. The next quality step is to tighten boundaries and reduce large orchestration files. The codebase has grown from a small app into several cross-cutting product areas: weekly planning, categories, events, activity, trophies, progress, backup, settings, and debug/demo tooling. Some files now act as informal subsystems, but the package structure still presents them as single classes or generic `core` helpers.

The recommended strategy is incremental extraction:

1. Make dependency direction explicit: `core` should not depend on feature presentation/domain types except for intentional app-level integration points.
2. Move feature-specific UI components out of `core.ui` or make them generic through stable UI contracts.
3. Extract weekly-training commands and activity logging workflows from the ViewModel into focused application/domain services.
4. Split formatter/engine/demo-data hotspots by responsibility.
5. Replace broad Detekt suppressions with targeted refactors and small executable guardrails.
6. Gradually move mock-heavy ViewModel tests toward fakes for stateful repositories.

## Baseline Observations

- Single Gradle module: `:app` only.
- Feature packages: `activity`, `app`, `backup`, `categories`, `events`, `progress`, `settings`, `trophies`, and `weeklytraining`.
- Core packages: database, DI, navigation, strings, UI/theme/components, user actions, debug.
- CI runs build, unit tests, Detekt, and Ktlint.
- Current top code hotspots by line count include:
  - `DemoDataSeeder.kt`: 1396 lines.
  - `WeeklyTrainingViewModel.kt`: 1195 lines.
  - `TrophiesScreen.kt`: 1068 lines.
  - `SettingsScreen.kt`: 1045 lines.
  - `ActivityUiFormatter.kt`: 916 lines.
  - `WeeklyTrainingScreen.kt`: 823 lines.
  - `EventsScreen.kt`: 796 lines.
  - `ProgressScreen.kt`: 770 lines.
  - `WeeklyTrainingContent.kt`: 768 lines.
- Test coverage is broad but unevenly shaped: 29 unit test files and 12 androidTest files. Many ViewModel tests rely on MockK despite the project rule preferring fakes.

## Findings

### P1: `core.ui` Depends On Feature Types

Evidence:

- `core/ui/components/calendar/weeklytraining/WeeklyTrainingContent.kt` imports `SlotModePolicy`, `TimeSlot`, `WorkoutId`, and `WorkoutUi` from feature packages.
- `core/ui/components/calendar/WeeklyCalendarHeader.kt`, `WorkoutIndicatorColors.kt`, `WeeklyTrainingRow.kt`, `WeeklyTrainingDragDrop.kt`, `AddWorkoutDialog.kt`, and `AddRaceEventDialog.kt` also import feature domain or presentation models.
- `core/ui/theme/CategoryColors.kt` imports `CategoryDefaults` from the categories feature.
- `core/ui/preview/*` imports feature preview models.

Why it matters:

`core` looks like a reusable foundation, but it currently points back into feature packages. That makes package names less truthful and makes it harder to later split modules, move UI components, or reason about dependency direction.

Recommendation:

- Move feature-specific components from `core/ui/components/calendar/weeklytraining` into `features/weeklytraining/presentation/components`.
- Move feature-specific dialogs into owning feature packages, or define small generic UI contracts if they truly belong in shared UI.
- Keep only genuinely reusable primitives in `core.ui`: empty state, title chip, generic calendar shells, theme tokens, and low-level drawing helpers.
- For `CategoryColors`, either move category color identity into a shared non-feature domain package or invert the dependency so the categories feature maps category color IDs to theme colors outside `core`.

Suggested first step:

Move only `WeeklyTrainingContent`, `WeeklyTrainingRow`, `WeeklyTrainingSection`, and drag/drop helpers together. Keep their package-local tests with them. This is mostly package/file movement and should not change behavior.

### P1: Weekly Training ViewModel Is Doing Application Workflow Work

Evidence:

- `WeeklyTrainingViewModel` suppresses `LargeClass` and `TooManyFunctions`.
- It combines reactive state, category seeding, category backfill, scheduling, undo, copy last week, optimistic completion, Activity logging, conversion decisions, race-event date changes, and snackbar messages.
- Long methods remain for completion updates, details updates, and race-event updates.

Why it matters:

The ViewModel is the app's busiest coordination point. It is still testable, but it mixes UI state orchestration with business workflows and logging policy. Each new weekly-training feature now risks increasing the same file's size and complexity.

Recommendation:

Extract application-level command classes or use cases for workflows that mutate state:

- `UpdateWorkoutCompletionUseCase`
- `UpdateWorkoutDetailsUseCase`
- `UpdateRaceEventUseCase`
- `MoveWorkoutUseCase`
- `CopyPreviousDisplayWeekUseCase`
- `RestoreWeeklyUndoUseCase`

These should accept repositories/loggers, return a small result model, and leave the ViewModel responsible for state reads, launching coroutines, and translating results into UI messages.

Suggested first step:

Extract completion handling first because it has concurrency, optimistic state, Activity logging, and week-complete milestone behavior. Keep the public ViewModel API unchanged while moving the command body behind one injected collaborator.

### P1: Repository Interface Contains Business Logic And Deprecated Transition API

Evidence:

- `WeeklyTrainingRepository` suppresses `TooManyFunctions`.
- It exposes deprecated rest-day methods while the app has moved to `EventType`.
- It includes a default implementation for `replaceWorkoutsForDisplayWeek`, containing filtering, deletion, and insertion workflow logic.

Why it matters:

Repository interfaces should describe persistence operations or domain-facing contracts. Keeping workflow logic as interface defaults blurs test boundaries and makes fake implementations harder to keep faithful.

Recommendation:

- Remove deprecated rest-day overloads after confirming no callers remain.
- Move display-week replacement workflow fully into `WeeklyTrainingRepositoryImpl` or a dedicated use case.
- Split read/query and write/command responsibilities if the interface keeps growing.

Suggested first step:

Delete deprecated overloads and replace any tests still calling them. Then move the default `replaceWorkoutsForDisplayWeek` body out of the interface so fakes and production cannot diverge.

### P1: App Shell Is Acting As A Manual Navigation Graph

Evidence:

- `HermesAppContent` owns top-level destination state, pending settings routes, pending workout drafts, pending event drafts, pending trophy deep links, snackbar routing, and tab reselection behavior.
- It switches screens with a large `when` instead of a navigation graph.

Why it matters:

Manual navigation is still acceptable for this app size, but the app has already gained cross-feature flows: weekly/events -> settings categories -> return to draft, progress/trophies -> activity, trophy celebration -> trophy detail. Future navigation work will be risky if all route state remains in one composable.

Recommendation:

- Introduce an app-level route model and a small `AppNavigator`/state holder for cross-feature flows.
- Keep `NavigationSuiteScaffold`, but move route transitions and pending payload rules out of `HermesAppContent`.
- Defer full Navigation Compose unless deep links, back stacks, or Android system back behavior become a near-term priority.

Suggested first step:

Extract a pure `HermesAppNavigationState` reducer with actions like `OpenTab`, `OpenCategoriesFromWorkoutDraft`, `OpenActivityFromProgress`, and `OpenTrophyFromCelebration`. Test it as a plain Kotlin unit.

### P2: Debug Demo Data Is A Large Cross-Feature Fixture System Hidden In `core.debug`

Evidence:

- `DemoDataSeeder` suppresses `LargeClass`, `LongParameterList`, and `TooManyFunctions`.
- It imports categories, settings, weekly-training data entities, weekly-training domain enums, user action metadata, and string resources.
- It directly writes DAOs and settings repository state.

Why it matters:

Debug seeding is valuable for development, screenshots, and manual QA. Today it is a large cross-feature object in `core`, which makes it hard to reuse seed scenarios in tests and hard to understand what product stories the seed data represents.

Recommendation:

- Split it into scenario builders:
  - `DemoWorkoutScenarioBuilder`
  - `DemoActivityHistoryBuilder`
  - `DemoTrophyScenarioBuilder`
  - `DemoCategoryScenarioBuilder`
- Keep one small `DemoDataSeeder` orchestrator.
- Prefer domain-level seed models first, then map to entities at the persistence edge.

Suggested first step:

Extract pure builders that return `WorkoutEntity` and `UserActionEntity` lists without injecting anything. This reduces class size and gives tests deterministic fixture builders.

### P2: Activity Formatting Is A Monolithic Mapping Surface

Evidence:

- `ActivityUiFormatter` suppresses `CyclomaticComplexMethod`, `LargeClass`, and `TooManyFunctions`.
- It parses metadata, formats time, builds titles, builds subtitles, maps settings values, maps categories, maps trophies, maps non-workout events, and owns fallback behavior.

Why it matters:

Activity is a cross-cutting contract. Every state-changing feature depends on formatter correctness. The current shape centralizes everything, which is simple at first but increases regression risk as more `UserActionType` values are added.

Recommendation:

- Split formatter responsibilities by entity/action family:
  - `WorkoutActivityFormatter`
  - `EventActivityFormatter`
  - `CategoryActivityFormatter`
  - `SettingsActivityFormatter`
  - `BackupActivityFormatter`
  - `TrophyActivityFormatter`
- Keep `ActivityUiFormatter` as the public coordinator.
- Add a table-style unit test that asserts every `UserActionType` has an expected title behavior or an explicit fallback decision.

Suggested first step:

Extract global/settings/backup title mapping first. It is low-risk and removes a clear block from the formatter without touching workout subtitles.

### P2: Trophy Engine Is Correctly Isolated But Too Dense

Evidence:

- `TrophyEngine` is a pure domain class with good test coverage, but it suppresses `CyclomaticComplexMethod`, `LongMethod`, and `NestedBlockDepth`.
- It contains nested private data classes and one large history reducer over user actions.

Why it matters:

The engine is a good architectural direction because it is pure and testable. The risk is readability: trophy rules are business logic, and future trophy families will be hard to review if all history reconstruction remains in one nested reducer.

Recommendation:

- Keep `TrophyEngine.compute` as the public entry point.
- Extract `TrophyHistoryBuilder`, `TrophyActionParser`, and metric-specific accumulator helpers.
- Move nested data classes into dedicated package-private files when they become meaningful concepts.

Suggested first step:

Extract parsing (`UserActionRecord` -> parsed action) and category alias resolution. This separates input normalization from metric accumulation.

### P2: Screen Files Are Larger Than The UI Architecture Wants

Evidence:

- `TrophiesScreen.kt`, `SettingsScreen.kt`, `WeeklyTrainingScreen.kt`, `EventsScreen.kt`, and `ProgressScreen.kt` are all large.
- Some prior learning notes already identify a pragmatic split for Settings-like screens: route orchestration, common components, and route-specific content.

Why it matters:

Compose allows local UI structure, but large screens hide reusable patterns and make visual changes harder to review. The project already has feature-specific UI conventions; the files should reflect those boundaries.

Recommendation:

- Split screen files by stable sub-surface, not by tiny composable count.
- Prefer package-private files for feature-specific chunks:
  - `TrophiesOverviewContent.kt`
  - `TrophyDetailContent.kt`
  - `ProgressTrendSection.kt`
  - `ProgressActivityPreviewSection.kt`
  - `EventsListContent.kt`
  - `EventCard.kt`
- Keep route-level collection, launchers, and screen-level state wiring in `*Screen.kt`.

Suggested first step:

Use `ProgressScreen` as a cleanup candidate because recent commits touched it and its section model is already moving toward structured content.

### P2: MockK Dependency Conflicts With The Project's Preferred Test Style

Evidence:

- `app/build.gradle.kts` includes MockK.
- Weekly-training and settings tests create relaxed mocks for repositories, seeders, loggers, and settings flows.
- The project rule says to prefer fakes over mocks and not introduce new mocking frameworks.

Why it matters:

MockK is already present, so this is not a dependency introduction problem. The issue is style drift: ViewModel tests are often more robust with mutable fake repositories because Hermes behavior depends heavily on `Flow`, state transitions, ordering, and side effects.

Recommendation:

- Keep MockK where it is genuinely lower-cost, such as simple call verification around logging.
- Introduce small fakes for stateful repositories:
  - `FakeWeeklyTrainingRepository`
  - `FakeSettingsRepository`
  - `FakeCategoryRepository`
  - `FakeUserActionLogger`
- Use fakes in new tests and migrate touched test files opportunistically.

Suggested first step:

Create `FakeWeeklyTrainingRepository` for weekly ViewModel tests. It can own a `MutableStateFlow<List<Workout>>`, record writes, and remove many relaxed mock assumptions.

### P2: Quality Rules Are Mostly Social, Not Enforced

Evidence:

- `AGENTS.md` and repo-local skills encode strong rules about `Dimens`, localized resources, Activity logging, backup compatibility, and ViewModel model placement.
- Detekt config is active but deliberately permissive in key areas: magic numbers are disabled, Composable/Preview complexity is ignored, and several source files suppress broad complexity rules.
- CI runs build/test/detekt/ktlint, but not Android instrumented tests.

Why it matters:

The codebase has good standards, but many of them rely on reviewer memory or agent skills. For a reference codebase, the most important conventions should be executable where practical.

Recommendation:

- Add lightweight custom checks or scripted CI checks for:
  - localized string key parity across `values-*`.
  - `core` importing `features.*` outside approved integration files.
  - new `values/strings.xml` keys missing from locales.
  - forbidden `Dimens.*` and `import Dimens.*`.
  - data classes/enums/sealed classes inside ViewModel files.
- Add a local `qualityCheck` Gradle task or documented command that bundles compile, unit tests, detekt, ktlint, and resource parity.

Suggested first step:

Create a small script under `tools/check-resource-parity` or a Gradle task that compares `strings.xml` keys across locales. This catches a frequent class of Hermes regressions cheaply.

### P3: CI Has Some Redundant Work And Could Better Express Intent

Evidence:

- The build workflow runs `./gradlew build`, then `./gradlew test`, then `./gradlew detekt`, then `./gradlew ktlintCheck`.
- `build` may already run part of the verification lifecycle, depending on task wiring.

Why it matters:

This is not a correctness issue, but CI should teach contributors what matters. A more explicit task list is easier to reason about and can avoid redundant runtime.

Recommendation:

- Replace separate workflow steps with one explicit command once task dependencies are clear, for example:
  - `./gradlew :app:assembleDebug :app:testDebugUnitTest detekt ktlintCheck`
- Consider adding a separate optional/manual instrumentation workflow for androidTests if GitHub runner cost is acceptable.

### P3: AppCompat Dependency May Be Unnecessary

Evidence:

- `app/build.gradle.kts` includes `androidx.appcompat`.
- The app is Compose + Material 3 with `ComponentActivity`/Hilt entry points.

Why it matters:

Unused dependencies make a reference project noisier. If AppCompat is not needed for locale, theme, or compatibility behavior, removing it simplifies the stack.

Recommendation:

- Run a usage check for `androidx.appcompat` imports.
- If unused, remove the dependency and verify build/tests.

### P3: README Feature List Has Drifted Behind Product Language

Evidence:

- README feature bullets still list "Training" and "Rest day" under item states, but the app now supports busy, sick, and race-event planning moments.
- The baseline docs/specs know about these expanded event types.

Why it matters:

This is documentation polish, but public docs are part of the reference-codebase impression.

Recommendation:

Update README feature bullets after the next user-facing release prep so public language matches shipped behavior.

## Recommended Roadmap

### Phase 1: Boundary Cleanup

Goal: make the package structure tell the truth.

- Move weekly-training UI components out of `core.ui`.
- Move feature preview data next to feature UI.
- Decide where category color IDs belong so `core.ui.theme` stops importing `features.categories`.
- Delete deprecated repository overloads after caller check.

Success signal:

- `core` imports no `features.*` packages except approved app/database integration points.
- No behavior change.

### Phase 2: Weekly Command Extraction

Goal: keep `WeeklyTrainingViewModel` as state orchestration, not workflow implementation.

- Extract completion command first.
- Extract details/conversion command second.
- Extract move/copy/undo flows after command result patterns are stable.
- Add fakes for weekly ViewModel tests as extraction support.

Success signal:

- `WeeklyTrainingViewModel` no longer needs broad `LargeClass` and `TooManyFunctions` suppressions.
- Main weekly behavior tests remain green and become easier to read.

### Phase 3: Cross-Cutting Formatter And Trophy Cleanup

Goal: make business rules reviewable in smaller units.

- Split Activity formatting by entity/action family.
- Extract Trophy action parsing/history building.
- Add exhaustive-ish mapping tests for Activity formatter coverage.

Success signal:

- Adding a `UserActionType` requires touching a focused formatter and a focused test.
- Trophy rules can be reviewed by metric area.

### Phase 4: Executable Guardrails

Goal: make project conventions enforceable.

- Add resource parity check.
- Add dependency-direction check.
- Add ViewModel nested-model check.
- Bundle local checks into a documented command.

Success signal:

- Frequent project rules fail fast locally and in CI.
- Repo-local skills become review guidance, not the only enforcement layer.

## Explicit Non-Goals

- Do not split into Gradle modules yet. The current project can get most benefits through package cleanup first.
- Do not adopt Navigation Compose just to be "standard." Use it only if route/back-stack needs justify the migration.
- Do not remove MockK in one sweep. Prefer fakes for new/touched tests and let the old tests migrate gradually.
- Do not rewrite backup compatibility. It is already one of the better-structured areas and should stay schema-versioned.

## Open Questions

- Should `core.database` remain the allowed place where Room aggregates feature entities and DAOs, or should database ownership move toward a data module/package if the app ever becomes multi-module?
- Should category color identity be considered core design-system data or categories-domain data? The current code treats it as both.
- Should debug/demo seeders remain production-source debug-gated code, or should part of the scenario-building move to test fixtures shared by debug and tests?

## Verification Notes

This audit was based on source inspection, line-count hotspot analysis, dependency-direction search, Detekt suppression search, test-style search, CI inspection, and key file reads. No production code behavior was changed.
