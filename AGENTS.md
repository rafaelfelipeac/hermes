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

## User actions
- Log user actions via `UserActionLogger` when state changes, using existing metadata keys.

## Testing
- Prefer fakes over mocks in unit tests.

## General
- Keep changes minimal and consistent with existing patterns in the touched files.
- Avoid mass formatting changes unless explicitly requested.

## Change safety
- If a change affects public APIs, navigation, or persisted data schemas, stop and ask before proceeding.
- If unsure about an architectural decision, present options instead of choosing.

## Assumptions
- Do not assume missing requirements; ask or state assumptions explicitly.
- Prefer TODO comments over speculative implementations.

## Testing
- Do not introduce new mocking frameworks.
- Follow existing test patterns in the module.
