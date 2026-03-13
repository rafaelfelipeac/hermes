# Hermes Skills

Hermes keeps its project-specific skills in this repo so the automation guidance evolves with the codebase instead of living in a separate machine-local install.

## When To Use Each Skill

### `hermes-activity-logging-check`
Use this when a feature changes app state and you want to confirm the Activity timeline is complete.

It checks the full path:
- `UserActionType`
- logger emission
- metadata keys
- Activity formatter mapping
- localized `activity_action_*` strings

Good fit:
- new setting that should appear in Activity
- new workout/category mutation
- import/export actions with user-visible history

### `hermes-compose-guardrails`
Use this for Compose or UI-only changes when the main risk is drifting from Hermes design-system rules.

It focuses on:
- `Dimens` usage instead of hardcoded `dp`
- `colorScheme` / `typography` / `shapes` import style
- no hardcoded strings or test tags
- avoiding hardcoded theme/category constants

Good fit:
- composable refactors
- previews
- UI tests
- theme updates

### `hermes-localization-check`
Use this whenever strings change and you need to make sure every locale stays correct.

It checks:
- key propagation across `values*/strings.xml`
- placeholder parity
- accidental English fallback text in localized files
- naming/consistency issues

Good fit:
- any new user-facing string
- renaming labels
- Activity string additions

### `hermes-pr-review`
Use this for a broad Hermes review when you want findings by severity instead of a narrow checklist.

It looks at:
- regressions
- architecture boundary violations
- DI and persistence mistakes
- backup policy mismatches
- localization misses
- logging/policy drift

Good fit:
- reviewing a feature branch
- checking a multi-file refactor
- auditing a risky change before merge

### `hermes-test-gap-check`
Use this after a feature or refactor when you want to know what behavior is still under-tested.

It maps changed behavior to:
- `covered`
- `partial`
- `uncovered`

And then proposes fake-based tests that match Hermes conventions.

Good fit:
- ViewModel changes
- repository changes
- state transition logic
- logging side effects
