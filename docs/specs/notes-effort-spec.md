# Notes And Perceived Effort Spec

Target release: after Progress and likely after the first Weekly Report direction is clearer.

## Goal

Let users capture how a session felt without making Hermes feel like a heavy training diary.

The feature should support reflection and coach-friendly reporting, while keeping the weekly planner fast and calm.

## Product Fit

Hermes already has a planning field through workout descriptions. Notes and perceived effort should add post-session context, not replace planning details.

The core distinction:

- Description: what the user planned or wants to remember before the session.
- Note: what happened or how it felt after/during the session.
- Perceived effort: optional lightweight intensity signal.

## User Stories

- As a user, I can add a note to a workout.
- As a user, I can add perceived effort when marking a workout completed.
- As a user, I can skip effort without being nagged.
- As a user, I can edit or clear a note/effort later.
- As a user, I can see note/effort in weekly details and reports when present.

## First Version Scope

Recommended v1:

- Optional note for workout rows.
- Optional perceived effort for workout rows.
- Edit note/effort from the workout edit flow or a lightweight completion follow-up.
- Show note/effort in row detail or expanded state when present.
- Include note/effort in Weekly Report only when the report feature exists.

Keep race events, rest, busy and sick rows out of effort tracking unless a later use case makes it valuable.

## Effort Scale Direction

Use a simple perceived effort scale:

- 1 to 5, or
- 1 to 10.

Open decision: choose before implementation.

Recommendation: start with 1 to 5 because Hermes is not a performance analytics app. A smaller scale is easier to understand and less pseudo-scientific.

Potential labels:

- Very easy.
- Easy.
- Moderate.
- Hard.
- Very hard.

Use localized strings and avoid numeric-only UI if labels clarify the tone.

## Data Model Decision

This likely requires persisted schema work.

Options:

1. Add nullable fields to `WorkoutEntity`:
   - `note`.
   - `perceivedEffort`.

2. Add a separate session feedback table:
   - `workoutId`.
   - `note`.
   - `perceivedEffort`.
   - timestamps if needed.

Recommendation for v1: add nullable fields to the weekly item if notes/effort are strictly one-per-workout and do not need history.

Choose a separate table only if the product needs multiple notes, timestamps, or feedback history.

Any persisted model change must include:

- Room migration.
- Backup schema version review.
- Import/export tests.
- Activity logging.

## UI Direction

Avoid turning completion into a blocking form.

Possible UX:

- User marks workout completed.
- Snackbar or inline affordance offers "Add effort" or opens a small bottom sheet.
- User can ignore it.
- Edit dialog has optional Note and Effort sections.

The row should stay compact. Show a small note/effort indicator only when present.

## Activity Logging

State-changing actions should be logged:

- Add/update note.
- Clear note.
- Add/update perceived effort.
- Clear perceived effort.

Do not log free-text note contents.

Metadata can include:

- workout id.
- week start date.
- day of week.
- old/new effort value.
- boolean flags for note present/cleared.

## Backup Compatibility

Likely backup schema change.

Requirements:

- Add a new schema version if fields are exported.
- Keep older backups importable with null note/effort.
- Decide how forward-compatible unknown fields should behave.
- Update `docs/backup-compatibility-policy.md`.

## Localization

All note/effort labels, actions and validation messages must be localized.

Effort labels need special review because tone matters.

## Testing

Recommended tests:

- Migration preserves existing workout rows with null note/effort.
- Backup round trip preserves note/effort.
- Old backups import with empty feedback.
- Completion does not require effort.
- Updating effort logs metadata without note text.
- Clearing effort/note updates state and logs correctly.
- Compose test covers completion flow with and without effort entry.

## Acceptance Criteria

- Notes and perceived effort are optional.
- Completion remains fast when the user skips effort.
- Free-text note contents are not logged in Activity metadata.
- Backup/import stays compatible.
- Weekly report can include note/effort later without reworking the model.
