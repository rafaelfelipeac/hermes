---
name: hermes-pr-review
description: Review Hermes pull requests for behavioral regressions, architecture violations, and policy misses. Use when evaluating code changes in Hermes, especially Kotlin/Compose UI, ViewModel, repository, Room/DataStore, Hilt wiring, localization, and activity logging coverage.
---

# Hermes PR Review

## Goal

Produce a code-review report focused on correctness, regressions, and policy compliance. Prioritize findings over summaries.

## Input Contract

Expect:
- changed file list or diff
- optional target depth: `quick`, `standard`, or `strict`

Default to `standard`.

## Strictness Modes

- `quick`: report only P0/P1 issues and highest-risk regressions.
- `standard`: report P0-P2 issues and notable policy misses.
- `strict`: report P0-P3 issues, including minor consistency drift.

## Review Workflow

1. Inspect changed files and infer user-visible behavior changes.
2. Check high-risk areas first: state transitions, persistence mapping, DI wiring, localization.
3. Validate Hermes rules from `AGENTS.md`.
4. Report findings in severity order with file/line references.
5. List residual risks and confidence blockers.

## Hermes Review Checklist

- Architecture boundary:
  - UI does not access Room/DataStore directly.
  - UI delegates to ViewModel; ViewModel delegates to repository/use case.
- ViewModel patterns:
  - Public state exposed as `StateFlow` via `stateIn` and `SharingStarted.WhileSubscribed`.
  - No new data classes/enums/sealed classes inside ViewModel files.
- Compose/UI guardrails:
  - No hardcoded `dp`; use `Dimens` named constants.
  - No `Dimens.*` usage and no star imports from `Dimens`.
  - Prefer explicit imports for `MaterialTheme.colorScheme`, `typography`, `shapes`.
  - No hardcoded string literals for UI text or test tags.
- Resources/localization:
  - New `values/strings.xml` entries mirrored in all localized `values-*/strings.xml` files.
  - Non-English files do not use English placeholders.
- User actions and Activity:
  - State-changing features emit `UserActionLogger` events.
  - New features include `UserActionType`, formatter mapping, and localized `activity_action_*` strings unless explicitly excluded.
- Persistence/DI:
  - Entity/domain mapping remains in data layer.
  - DataStore used only for settings.
  - Backup compatibility is gated by `schemaVersion`, keeps versioned decoders, and updates `docs/backup-compatibility-policy.md` when the contract changes.
  - Hilt wiring remains in expected `di` packages.
- Learning:
  - Meaningful architectural or workflow lessons are appended to `LEARNING.md`.

## Output Format

1. Findings
- `[Px] Title` with impact, root cause, and fix.
- Include file and line references.

2. Open Questions
- blockers to confidence only.

3. Residual Risks
- unverified paths (for example, tests not run).

## Dry Run Example

Input: "Review this weekly planner PR in strict mode."
Expected shape:
- Findings (severity sorted)
- Open Questions
- Residual Risks
