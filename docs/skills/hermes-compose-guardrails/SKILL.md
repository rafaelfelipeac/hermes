---
name: hermes-compose-guardrails
description: Audit Hermes Compose and UI-layer changes for design-system and code-style guardrails. Use when editing composables, previews, UI tests, theme files, or any Kotlin UI code where spacing, theming, strings, or tags might drift from Hermes conventions.
---

# Hermes Compose Guardrails

## Goal

Catch UI guardrail violations early and provide minimal corrective fixes.

## Input Contract

Expect:
- Compose/UI Kotlin diff
- optional mode: `quick`, `standard`, `strict`

Default to `standard`.

## Strictness Modes

- `quick`: flag only high-impact violations.
- `standard`: flag all policy violations in changed lines.
- `strict`: include nearby consistency drifts beyond changed lines.

## Workflow

1. Review changed UI files and classify each violation by rule.
2. Suggest the smallest fix aligned with local style.
3. Report actionable issues with file/line references.

## Guardrails

### Dimens and spacing

- Avoid hardcoded `dp` values in UI code.
- Use named constants from `Dimens`.
- Avoid star imports from `Dimens`.
- Avoid `Dimens.*`; import only required symbols.

### MaterialTheme usage

- Prefer explicit imports:
  - `MaterialTheme.colorScheme`
  - `MaterialTheme.typography`
  - `MaterialTheme.shapes`
- Use `colorScheme`, `typography`, `shapes` directly.

### Strings and tokens

- Do not hardcode user-facing strings.
- Do not hardcode magic test tags.
- Avoid hardcoded empty/newline tokens when project constants exist.

### IDs and constants

- Do not hardcode category IDs/color IDs; use `CategoryDefaults`.
- Do not hardcode theme thresholds/blend values; use `ThemeConstants`.

## Output Format

1. Violations
- `[Px] rule-name` with file/line and exact fix.

2. Patch Suggestions
- minimal snippets only.

3. Clean Areas
- optional one-line note for inspected files with no issues.

## Dry Run Example

Input: "Check this composable diff in strict mode."
Expected shape:
- Violations
- Patch Suggestions
- Clean Areas (optional)
