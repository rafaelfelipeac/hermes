# Project Research Map

Date: 2026-05-11

Scope: research-only map of Hermes architecture, feature boundaries, coupling, tests, and future refactor research tracks. This document intentionally does not prescribe immediate code changes because the project is in the middle of feature work.

Related document: `docs/specs/project-architecture-audit-2026-05-11.md`.

## Purpose

Use this as a navigation document for future cleanup work. The architecture audit ranks the most important findings; this map explains where each part of the project currently lives, what it owns, what it depends on, and what should be researched before implementation.

## Current Shape

Hermes is a single-module Android app:

- Gradle module: `:app`.
- Production Kotlin files under `app/src/main/java`: 169 files.
- Unit test files under `app/src/test/java`: 29 files.
- Android UI/instrumentation test files under `app/src/androidTest/java`: 12 files.
- Main feature packages:
  - `activity`
  - `app`
  - `backup`
  - `categories`
  - `events`
  - `progress`
  - `settings`
  - `trophies`
  - `weeklytraining`
- Main core packages:
  - `database`
  - `debug`
  - `di`
  - `navigation`
  - `strings`
  - `ui`
  - `useraction`

The app is still small enough that a Gradle module split would be premature. Package-boundary cleanup is the more useful next step because it improves architecture without build-system churn.

## Feature Inventory

Approximate production file distribution inside `features`:

| Feature | Files | Current role |
| --- | ---: | --- |
| `weeklytraining` | 29 | Core planning model, week display, drag/drop, copy/undo, completion, event type support. |
| `backup` | 19 | Offline JSON snapshot export/import, schema versioning, validation, repository coordination. |
| `settings` | 17 | Theme, language, week start, slot mode, backup UI, release notes, debug actions. |
| `trophies` | 16 | Trophy engine, definitions, progress UI, celebrations. |
| `categories` | 14 | Category defaults, seeding, CRUD, ordering, visibility, localized system names. |
| `activity` | 13 | User-action timeline, filtering, formatter, activity preview source for Progress. |
| `events` | 6 | Race-event list and event-specific create/update/delete flows over workout storage. |
| `progress` | 4 | Read-only aggregation surface over workouts, user actions, trophies, categories. |
| `app` | 4 | App root, language application, top-level navigation shell. |

Approximate test distribution:

| Feature | Test files | Notes |
| --- | ---: | --- |
| `weeklytraining` | 10 | Broadest behavior coverage, but still mock-heavy and tied to a large ViewModel. |
| `trophies` | 5 | Strong domain tests for `TrophyEngine` and UI tests for content. |
| `settings` | 5 | Large ViewModel test class; backup/settings interactions are covered but mock-heavy. |
| `progress` | 3 | Recent feature tests; some time-dependent tests use `LocalDate.now()`. |
| `categories` | 3 | ViewModel, seeder, and UI tests. |
| `activity` | 3 | ViewModel, formatter, and UI content tests. |
| `events` | 2 | Large ViewModel test and content test. |
| `backup` | 2 | JSON codec unit tests and repository instrumentation test. |

## Boundary Map

### `core`

Intended role:

- Reusable app foundation: database shell, DI, navigation constants, strings, design system, user-action infrastructure, shared UI primitives.

Current drift:

- `core.ui` imports feature models from weekly training, categories, and settings.
- `core.debug` imports many feature/domain/data types.
- `core.database` imports feature entities and DAOs. This is acceptable in the current single-module setup, but it should be documented as an intentional integration point.
- `core.di.DatabaseModule` imports feature DAOs. Also acceptable as an integration point.

Research conclusion:

`core.database` and `core.di` can remain exceptions for now. `core.ui`, `core.ui.preview`, `core.ui.theme.CategoryColors`, and `core.debug` should be researched as boundary-cleanup candidates because they are not just infrastructure; they contain feature knowledge.

### `weeklytraining`

Intended role:

- Owns the planning domain: workouts, rest/busy/sick/race-event storage model, week display math, ordering, drag/drop behavior, completion and copy/undo workflows.

Current strengths:

- Domain/data/presentation folders exist.
- Persistence is abstracted behind `WeeklyTrainingRepository`.
- Many behavior tests exist.
- Complex week-start and display-week logic has been documented in `LEARNING.md`.

Current risks:

- `WeeklyTrainingViewModel` is the largest feature coordinator and mixes state, workflows, logging, undo, and policy decisions.
- `WeeklyTrainingRepository` has too many operations and still carries deprecated rest-day transition overloads.
- `WorkoutOrdering.kt` currently imports `WorkoutUi`, meaning domain code depends on a presentation model. This is a sharper boundary issue than normal feature-internal references because it reverses the intended domain -> presentation direction.
- Weekly-specific UI components live under `core.ui.components`.

Research tracks:

- Map weekly workflows into commands/use cases without changing behavior.
- Decide whether event-type planning should remain entirely under weekly training or have a shared planning-domain package consumed by Weekly and Events.
- Identify domain functions that accept `WorkoutUi` and replace them with domain models or lightweight input DTOs in a future refactor.

### `events`

Intended role:

- Dedicated screen for race-event planning and countdown context.

Current strengths:

- Events are backed by the same storage model as weekly items, avoiding another persistence model.
- Event-specific tests cover create/update/delete/undo behavior.

Current risks:

- `EventsViewModel` imports weekly-training presentation helpers (`UndoMessage`, `WorkoutUi`, action mapping helpers, and UI mapper alias).
- Race events are represented as `Workout` records with `EventType.RACE_EVENT`, which is pragmatic but keeps Events tightly coupled to weekly training naming and behavior.
- Date validation uses `LocalDate.now()` directly, which makes deterministic testing harder and may eventually need a clock abstraction.

Research conclusion:

Events does not need its own persistence right now. The better research question is whether Hermes needs a shared `planning` or `schedule` domain vocabulary so Events and Weekly can both consume it without depending on each other's presentation layer.

### `categories`

Intended role:

- Category defaults, localization-aware seeding, category CRUD/order/visibility, and category UI mapping.

Current strengths:

- Clear domain/data/presentation split.
- Category seeding has dedicated tests.
- User actions are logged from the ViewModel for state changes.

Current risks:

- `CategoriesViewModel` depends on `WeeklyTrainingRepository` to reassign workouts when deleting a category. The behavior is correct, but it couples category management to weekly storage.
- `CategoryDefaults` is imported widely by UI/theme/debug/tests.

Research conclusion:

The category-delete workflow may deserve a small application service later, for example `DeleteCategoryAndReassignWorkoutsUseCase`, so category UI does not need to know which repository owns affected items. Category color identity also needs an ownership decision: design-system concern or categories-domain concern.

### `backup`

Intended role:

- Portable offline snapshot of workouts, categories, user actions, and settings.

Current strengths:

- Versioned decoders (`BackupV1Decoder`, `BackupV2Decoder`, `BackupV3Decoder`).
- Compatibility policy exists in `docs/backup-compatibility-policy.md`.
- Import gates on `schemaVersion`, not `appVersion`.
- Replace import uses a Room transaction for core DB tables and applies settings separately.

Current risks:

- `BackupRepositoryImpl` is intentionally cross-feature and directly sees DAOs/entities/settings from multiple domains. That is acceptable for snapshot ownership, but it should remain the only place with this breadth.
- Validation is compact but grows with every schema/event/settings addition.
- Backup settings restore failure is non-fatal by design, so tests and docs should keep that behavior explicit.

Research conclusion:

Backup is not the first cleanup target. Future research should focus on schema-change playbooks and test matrices, not broad restructuring.

### `settings`

Intended role:

- Preferences, settings state, detail routes, backup folder/export/import UI, debug actions, release notes.

Current strengths:

- DataStore is behind `SettingsRepository`.
- Settings state follows `StateFlow` and `SharingStarted.WhileSubscribed`.
- UI has started splitting common components and backup screen content out of `SettingsScreen`.

Current risks:

- `SettingsScreen.kt` remains large and suppresses `TooManyFunctions`.
- `SettingsViewModelTest` is large and heavily MockK-based.
- `SettingsViewModel` has many one-shot event flows for debug/demo actions.

Research conclusion:

Settings is a good candidate for UI file organization and fake repository testing, but it is not as architecturally risky as weekly training.

### `activity`

Intended role:

- Timeline over persisted user actions, including filters and localized formatting.

Current strengths:

- User actions are persisted separately from feature state.
- Activity formatting is centralized, which gives one obvious place to inspect timeline behavior.
- Formatter tests exist.

Current risks:

- `ActivityUiFormatter` is monolithic and suppresses complexity/size rules.
- Formatter behavior is a cross-feature contract, so every new `UserActionType` increases regression risk.
- `ProgressViewModel` instantiates `ActivityUiFormatter` directly, so Activity formatting is reused outside Activity without an interface.

Research conclusion:

Activity should be mapped as a cross-cutting read model. A future split should preserve one public coordinator but move action-family formatting into smaller classes.

### `trophies`

Intended role:

- Turn user-action history into gentle progress/celebration surfaces.

Current strengths:

- `TrophyEngine` is pure Kotlin and tested.
- Trophy progress is derived from existing user actions, not extra persistence.
- Category-specific trophies use explicit context models.

Current risks:

- `TrophyEngine` has a dense history reducer and nested internal models.
- `TrophiesScreen.kt` is one of the largest UI files.
- `TrophyPresentationMappings.kt` suppresses complexity and long-method rules.

Research conclusion:

Trophies are architecturally healthier than their file sizes suggest because the engine is pure. Future research should focus on readability and rule-review ergonomics, not behavior redesign.

### `progress`

Intended role:

- Read-only aggregation screen over weekly data, categories, Activity preview, and trophies.

Current strengths:

- No new persistence.
- `ProgressSummaryBuilder` has focused tests.
- ViewModel state is derived from existing flows.

Current risks:

- `ProgressViewModel` currently depends on weekly repository, category repository, user-action repository, settings repository, trophy engine, and Activity formatter. That is expected for an aggregator, but it should remain read-only.
- Uses `LocalDate.now()` and `ZoneId.systemDefault()` directly.
- `ProgressScreen.kt` is already large despite the feature being newer.

Research conclusion:

Progress should be documented as an aggregator/read model. If it grows, extract builders/read-model assemblers before adding persistence.

### `app`

Intended role:

- App root, top-level theme/language application, and destination shell.

Current strengths:

- Simple top-level state avoids navigation-framework overhead.
- Cross-screen draft handoff is explicit and saveable.

Current risks:

- `HermesAppContent` is now a manual navigation graph with pending route/draft state and trophy snackbar routing.
- Public navigation changes are protected by `AGENTS.md`, so any navigation refactor needs explicit review.

Research conclusion:

Do not adopt Navigation Compose just for convention. Research a pure app-navigation reducer first; it can make the existing manual shell more testable without changing public navigation.

## Cross-Cutting Maps

### Time And Locale

Direct time/locale calls appear in production:

- `WeeklyTrainingViewModel`: `LocalDate.now()` for initial selected date.
- `EventsViewModel`: `LocalDate.now()` for past-event validation.
- `ProgressViewModel`: `LocalDate.now()`, `ZoneId.systemDefault()`, `Locale.getDefault()`.
- `ActivityViewModel`: `Locale.getDefault()`, `ZoneId.systemDefault()`.
- `DemoDataSeeder`: `LocalDate.now()`, `ZoneId.systemDefault()`.
- Several Compose screens/components use `LocalConfiguration` correctly, but some lower-level components still use `Locale.getDefault()`.

Research conclusion:

Hermes likely needs a small `DateTimeProvider` or `ClockProvider` only when these areas are touched. Do not introduce it globally as a standalone cleanup unless tests become flaky or app-language formatting drifts.

### Resource And Localization Guardrails

Observed:

- All `values*/strings.xml` files currently expose 509 unique named entries in a simple key-count check.
- Resource parity is important enough to automate because Hermes supports many locales.
- Current project rules require all localized files to stay in sync, including non-translatable helper strings.

Research conclusion:

Resource parity is an excellent first executable guardrail. It is low-risk, documentation-friendly, and catches a real recurring failure class.

### Compose Guardrails

Observed:

- Most inspected UI code follows explicit `MaterialTheme.colorScheme` / `typography` / `shapes` imports.
- The grep output for hardcoded `dp` mostly points to `Dimens.kt`, which is expected.
- Test code still uses hardcoded visible text assertions, which is usually acceptable when testing localized/default display strings but can become brittle for locale-sensitive UI.
- Some dynamic test tags are built from constants and IDs, which fits existing patterns.

Research conclusion:

Compose guardrail automation can be narrow: check for forbidden star imports, `Dimens.*`, hardcoded `dp` outside `Dimens.kt`, and nested model declarations in ViewModels. Do not try to lint every UI string with regex; false positives would be noisy.

### Testing Style

Observed:

- MockK appears heavily in:
  - `SettingsViewModelTest`
  - `CategoriesViewModelTest`
  - weekly ViewModel tests
  - `CategorySeederTest`
  - `DemoDataSeederTest`
- Existing tests often verify repository/logger calls rather than exercising stateful fake behavior.

Research conclusion:

Do not remove MockK wholesale. The useful research direction is a fake-fixture map:

- `FakeWeeklyTrainingRepository` for mutable workout flows and write recording.
- `FakeSettingsRepository` for DataStore-like state.
- `FakeCategoryRepository` for category flows and mutations.
- `FakeUserActionLogger` for recorded actions.
- Keep MockK for small one-off collaborators until a fake proves useful.

### Activity Logging

Observed:

- Activity logging is one of Hermes's defining cross-cutting policies.
- Weekly, events, categories, settings, backup, and trophies interact with user actions directly or indirectly.
- Activity formatter is the bottleneck for user-visible interpretation.

Research conclusion:

Any future state-changing feature should be reviewed through `hermes-activity-logging-check`. For cleanup, the research target is not "add more logs"; it is "make existing log emission and formatting easier to audit."

## Research Backlog

These are research tasks, not implementation tasks.

### R1: Dependency Direction Matrix

Question:

Which packages are allowed to import which other packages?

Deliverable:

- A `docs/specs/dependency-direction-policy.md` proposal.
- Explicit allowed exceptions:
  - `core.database` may aggregate Room entities/DAOs.
  - `core.di` may expose DAOs/repositories.
  - `features.app` may compose feature screens.
  - `features.backup.data` may coordinate snapshot tables/settings.
- Candidate check command for later automation.

### R2: Weekly Workflow Decomposition Map

Question:

What are the exact workflows inside `WeeklyTrainingViewModel`, and which can become use cases without behavior change?

Deliverable:

- A workflow inventory with inputs, repository calls, user-action logs, undo behavior, and tests.
- Recommended extraction order.
- Risk notes for optimistic completion and copy/undo.

### R3: Planning Domain Vocabulary

Question:

Should weekly training and events share a neutral planning/schedule domain vocabulary?

Deliverable:

- Map all uses of `Workout`, `WorkoutUi`, `EventType`, `RaceEvent`, rest/busy/sick naming.
- Identify whether renaming/repackaging can be done without schema changes.
- Decide whether this is just package cleanup or a product/domain naming change that needs explicit approval.

### R4: Activity Formatter Coverage Matrix

Question:

Does every `UserActionType` have intentional title/subtitle behavior?

Deliverable:

- Table of `UserActionType` -> title handler -> subtitle handler -> fallback behavior -> test coverage.
- Proposed formatter split boundaries.

### R5: Fake-Based Test Migration Map

Question:

Which tests would become simpler and safer with fakes?

Deliverable:

- Test file by test file recommendation:
  - keep MockK
  - introduce fake
  - split test class
  - add deterministic clock
- Fake API sketches, without implementing them yet.

### R6: Demo Data Scenario Map

Question:

What product stories does `DemoDataSeeder` currently encode?

Deliverable:

- Scenario list: default week, progress history, activity history, locked trophies, completed trophies, mixed trophies, race events.
- Suggested pure builder boundaries.
- Decision on whether builders should live in debug source, test fixtures, or shared test/debug package.

### R7: Time/Locale Consistency Map

Question:

Which production paths should honor app language/configuration locale, and which can use system defaults?

Deliverable:

- File-by-file classification of direct time/locale calls.
- Recommendation on whether to introduce `DateTimeProvider`.
- Tests most likely to become flaky because of `LocalDate.now()`.

### R8: UI File Split Map

Question:

Which large Compose files can be split by stable screen sub-surface?

Deliverable:

- Proposed file boundaries for `TrophiesScreen`, `SettingsScreen`, `ProgressScreen`, `EventsScreen`, and `WeeklyTrainingScreen`.
- Preview/test preservation notes.
- Which splits are safe documentation-only moves vs. behavior-sensitive moves.

## Suggested Research Order

1. Dependency Direction Matrix
2. Weekly Workflow Decomposition Map
3. Activity Formatter Coverage Matrix
4. Fake-Based Test Migration Map
5. Planning Domain Vocabulary
6. UI File Split Map
7. Demo Data Scenario Map
8. Time/Locale Consistency Map

Reasoning:

Dependency direction and weekly workflows affect the most future work. Activity formatting and fakes make future behavior changes safer. Naming/domain vocabulary should wait until the current feature work is stable because it may become a larger product-language decision. UI splits and debug seed cleanup are valuable but less urgent.

## What Not To Do Yet

- Do not start a Gradle module split.
- Do not move weekly/event/category files during the current feature unless the feature already touches them.
- Do not replace MockK globally.
- Do not introduce a global clock abstraction as isolated cleanup.
- Do not change backup schema or import behavior as part of architecture cleanup.
- Do not adopt Navigation Compose until route/back-stack requirements are explicitly chosen.

## Research Definition Of Done

This research phase is complete when the repo has:

- A dependency direction policy proposal.
- A weekly workflow decomposition map.
- An Activity formatter coverage matrix.
- A fake-test migration map.
- A UI split map for the large Compose files.

After that, implementation can happen as small independent branches.
