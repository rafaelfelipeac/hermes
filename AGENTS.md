# AGENTS.md

Project-level conventions for Codex and similar agents.

## Kotlin/Compose style
- Avoid star imports from `Dimens`; import only the items in use (e.g., `import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd`).
- Do not use `Dimens.*` directly in code; reference imported names.
- Prefer explicit imports for `MaterialTheme` properties:
  - `import androidx.compose.material3.MaterialTheme.colorScheme`
  - `import androidx.compose.material3.MaterialTheme.typography`
  - `import androidx.compose.material3.MaterialTheme.shapes`
  Then reference `colorScheme`, `typography`, and `shapes` directly (no `MaterialTheme.`).
- Avoid hardcoded `dp` values in UI; add new sizes to `Dimens` and use named constants.
- Avoid hardcoded string tokens (including `""`, `"\n"`, and magic `testTag` strings). Use `AppConstants` or file-level constants.
- Avoid hardcoded category IDs or color IDs; rely on `CategoryDefaults` constants.
- Avoid hardcoded theme thresholds/blend values; use constants from `core/ui/theme/ThemeConstants.kt`.

## Architecture & data flow
- Follow UI → ViewModel → Repository boundaries; UI should not access Room or DataStore directly.
- ViewModels expose `StateFlow` via `stateIn` and `SharingStarted.WhileSubscribed`.

## Dependency injection
- Use Hilt for wiring; ViewModels use `@HiltViewModel` + `@Inject` constructor.
- Keep bindings in `core/di` or feature `di` packages.

## Persistence
- Room is the source of truth for entities and DAOs; map `*Entity` ↔ domain models in the data layer.
- DataStore (Preferences) is only for settings (theme/language).

## UI & resources
- Compose + Material 3 only; no XML layouts.
- No hardcoded strings; use `StringProvider` for non-UI formatting/strings.
- When adding a string to `app/src/main/res/values/strings.xml`, add equivalent entries to all localized `values-*/strings.xml` files.
- Do not add English placeholder text in localized `values-*/strings.xml` files; provide proper translations for each locale.

## User actions
- Log user actions via `UserActionLogger` when state changes, using existing metadata keys.
- For any new user-facing state-changing feature, default to full Activity support in the same task:
  add/update `UserActionType`, emit logs in the ViewModel/use case, map titles/subtitles in Activity formatter, and add localized `activity_action_*` strings.
- If a new feature intentionally should not appear in Activity, state that explicitly in the PR/task notes instead of leaving it implicit.

## Testing
- Prefer fakes over mocks in unit tests.

## General
- Keep changes minimal and consistent with existing patterns in the touched files.
- Avoid mass formatting changes unless explicitly requested.
- Do not define new data classes/enums/sealed classes inside ViewModels; place them in dedicated files in the feature package.

## Change safety
- If a change affects public APIs, navigation, or persisted data schemas, stop and ask before proceeding.
- If unsure about an architectural decision, present options instead of choosing.

## Assumptions
- Do not assume missing requirements; ask or state assumptions explicitly.
- Prefer TODO comments over speculative implementations.

## Testing
- Do not introduce new mocking frameworks.
- Follow existing test patterns in the module.

## Learning & existing codebase
- Maintain `LEARNING.md` as a living document.
  Update it incrementally as part of completing tasks, not as a separate or optional step.

- When working on existing code or architecture, append short learning notes to `LEARNING.md`
  explaining relevant decisions, trade-offs, or lessons uncovered during the task.

- Treat entries as post-hoc learning notes:
    - explain intent, trade-offs, and consequences
    - capture what was learned while changing or reading the code
    - avoid restating what the code does line by line

- If intent is not explicit in code, state assumptions clearly.
- Prefer teaching-oriented explanations over reference-style documentation.

- Do not wait for explicit requests to update `LEARNING.md` when meaningful learning occurs.
