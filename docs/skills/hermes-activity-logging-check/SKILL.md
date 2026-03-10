---
name: hermes-activity-logging-check
description: Verify Hermes user-action and Activity timeline coverage for new state-changing features. Use when a feature changes persisted or user-visible state and must be represented end-to-end in action logging, formatting, and localized activity strings.
---

# Hermes Activity Logging Check

## Goal

Confirm that state-changing features are fully wired into Hermes Activity logging with consistent metadata and localization.

## Input Contract

Expect:
- feature diff touching state-changing behavior
- optional mode: `quick`, `standard`, `strict`

Default to `standard`.

## Strictness Modes

- `quick`: only missing mandatory logging links.
- `standard`: full required-path coverage checks.
- `strict`: standard checks + naming/metadata consistency warnings.

## End-to-End Checklist

For each state-changing behavior, verify:

1. `UserActionType`
- suitable action exists or new one added.

2. Emission point
- log emitted in ViewModel/use case where state changes.

3. Metadata
- existing metadata keys reused consistently.

4. Activity formatter mapping
- title/subtitle mapping exists.

5. Localization
- required `activity_action_*` strings in base and localized files.

6. Intentional exclusions
- explicit rationale recorded if excluded from Activity.

## Output Format

1. Missing Pieces (ordered)
- `[Px]` with file refs and expected addition.

2. Consistency Gaps
- metadata mismatches, naming drift, partial localization.

3. Completion Matrix
- `Action type | emitted | formatted | localized | status`.

## Dry Run Example

Input: "Audit activity logging for this new rest-event feature in strict mode."
Expected shape:
- Missing Pieces
- Consistency Gaps
- Completion Matrix
