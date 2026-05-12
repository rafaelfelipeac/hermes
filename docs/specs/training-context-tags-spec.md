# Training Context Tags And Notes Spec

Target release: after the current Progress direction is stable; likely before Race Prep Checklist becomes context-aware.

## Goal

Let users record lightweight training notes and recovery/constraint tags such as travel, soreness, low sleep, busy week, stress, or schedule disruption, so they can review context before a race or when reflecting on a week.

The feature should help users remember what affected training without making medical claims, readiness scores, or automatic training decisions.

## Product Fit

Hermes is a flexible weekly planner. Training does not happen in a vacuum: travel, poor sleep, soreness, work, family, and schedule disruptions affect what the plan should look like.

This feature supports the user's own decision-making:

- capture what happened.
- review context before a race.
- include context in reports.
- understand why a week changed.

It should not tell the user what to do. It should make context visible so the user can decide.

## Relationship To Existing Notes Spec

`docs/specs/notes-effort-spec.md` focuses on workout-level notes and perceived effort.

This spec broadens that idea into training context:

- Workout note: what happened in a specific session.
- Weekly note: context for the week.
- Race/event note: context around a target event.
- Context tag: structured non-medical label that can be filtered, summarized, or included in reports.

Recommendation:

- Treat this as the broader umbrella spec.
- Keep workout note/effort as the first narrow implementation if that is easier.
- Add weekly/race context tags after the data model direction is settled.

## User Stories

- As a user, I can add a note to a workout.
- As a user, I can add a note to a week.
- As a user, I can tag a week with context like travel, low sleep, busy week, or soreness.
- As a user, I can tag a workout with context like soreness or low energy.
- As a user, I can review recent context before a race event.
- As a user, I can include context notes/tags in a weekly report if I choose.
- As a user, I can edit or clear notes and tags later.

## First Version Scope

Recommended v1:

- Optional workout note.
- Optional weekly note.
- Optional context tags on the week.
- Small predefined tag set.
- Show note/tag indicators in the week view.
- Include weekly note and tags in Weekly Report when that feature exists.

Recommended initial tags:

- Travel.
- Low sleep.
- Busy week.
- Soreness.
- Stress.
- Weather.
- Reduced time.
- Other.

Tone rules:

- Use neutral labels.
- Avoid medical diagnosis language.
- Avoid "injury" in v1 unless the app adds clear disclaimers and user copy.
- Do not generate readiness scores from tags.

## Later Scope

- Workout-level context tags.
- Race/event-level context notes.
- Race detail "Recent context" summary.
- Filter Activity/Progress by tags.
- Compare planned vs completed weeks with context.
- Coach-share controls for which notes/tags appear in reports.
- Custom tags.

## Out Of Scope For V1

- Medical advice.
- Injury diagnosis.
- Automatic training changes.
- Readiness score.
- AI recommendations.
- Cloud sync or coach portal.
- Required note entry after completion.

## Data Model Direction

This feature needs persisted user data.

There are two reasonable paths:

### Option 1: Add Nullable Fields To Existing Items

Add to workout rows:

- `note`.
- `perceivedEffort`.

Add week-level table:

- `weekStartDate`.
- `note`.
- tag IDs as a separate relation.

### Option 2: General Training Context Model

Create a context-note model that can attach to multiple entity types.

```kotlin
data class TrainingContextEntry(
    val id: Long,
    val entityType: TrainingContextEntityType,
    val entityId: Long?,
    val weekStartDate: LocalDate?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String,
)
```

Tags:

```kotlin
data class TrainingContextTag(
    val id: Long,
    val key: String,
    val labelRes: Int,
)

data class TrainingContextTagAssignment(
    val entryId: Long,
    val tagKey: String,
)
```

Entity types:

- `WORKOUT`
- `WEEK`
- `RACE_EVENT`

Recommendation:

- If v1 only adds workout note/effort, use nullable workout fields.
- If v1 includes weekly notes/tags, use a dedicated training-context table so the model can later attach to race events without adding scattered columns.

## Backup Compatibility

Any persisted notes/tags require backup schema review.

Requirements:

- Add a new backup schema version.
- Export/import context entries and tag assignments, or exported workout fields if using nullable columns.
- Older backups import with empty notes/tags.
- Do not reject older backups that lack context.
- Update `docs/backup-compatibility-policy.md`.

Privacy requirement:

- Notes are user free text. Exporting them is expected because backup is user-owned, but Activity logs must not include note contents.

## Activity Logging

State changes should be logged, but without free-text note content.

Recommended action types:

- `ADD_TRAINING_NOTE`
- `UPDATE_TRAINING_NOTE`
- `CLEAR_TRAINING_NOTE`
- `ADD_CONTEXT_TAG`
- `REMOVE_CONTEXT_TAG`
- `UPDATE_PERCEIVED_EFFORT`
- `CLEAR_PERCEIVED_EFFORT`

Metadata:

- entity type.
- entity id when available.
- week start date.
- tag key.
- old/new effort value.
- note present boolean.

Do not log:

- note text.
- custom tag text if custom tags are added later.

## UI Direction

### Week View

Keep the weekly planner fast.

Recommended v1 UI:

- A small weekly context row or chip group near the weekly header.
- Indicator on workout rows when a note exists.
- Edit note/tags from a lightweight sheet or existing edit flow.

Weekly context examples:

- `Travel`
- `Low sleep`
- `Busy week`

Avoid large note cards inside the main planner unless the note is expanded intentionally.

### Workout Edit Flow

Add optional sections:

- Note.
- Perceived effort.
- Context tags if workout-level tags are in scope.

Do not require any of these when completing a workout.

### Race/Event Detail

Future context section:

- Recent weekly tags leading into the race.
- Race/event note.
- Checklist progress from Race Prep Checklist.

Copy direction:

- "Context before this event"
- "Recent notes and constraints"

Avoid:

- "Readiness"
- "Risk"
- "Injury status"

## Decision Support Before A Race

The app can help users review context before a race by showing:

- recent weekly tags.
- recent notes.
- missed/reduced training count.
- race prep checklist progress.
- upcoming event date.

The app should not conclude:

- whether the user should race.
- whether the user is healthy.
- whether the user should change training intensity.

Recommended language:

- "Review recent context before race day."
- "You marked travel and low sleep this week."
- "Checklist: 6 of 8 done."

Avoid language:

- "You are ready."
- "You should rest."
- "High injury risk."
- "Race prediction."

## Weekly Report Integration

Weekly Report can include:

- weekly note.
- selected tags.
- workout notes only if the user enables them.
- perceived effort summary if implemented.

Because notes can be private, provide an inclusion toggle before sharing:

- Include notes.
- Include context tags.
- Include perceived effort.

Default recommendation:

- Include context tags by default.
- Exclude free-text notes by default until the user opts in.

## Progress Integration

Progress can later show:

- number of constrained weeks in recent history.
- context tags attached to weeks with lower completion.
- recent notes preview.

Avoid scoring.

Progress copy should frame this as explanation, not evaluation:

- "Context marked this week"
- "Weeks with travel"
- "Notes captured"

## Localization

All tag labels and UI copy must be localized.

Tag keys should be stable non-localized identifiers:

- `travel`
- `low_sleep`
- `busy_week`
- `soreness`
- `stress`
- `weather`
- `reduced_time`
- `other`

Display labels come from resources.

## Testing

Recommended tests:

- Add weekly note.
- Edit weekly note.
- Clear weekly note.
- Add/remove context tag.
- Add workout note without requiring effort.
- Updating note logs only note-present metadata, not content.
- Updating tag logs tag key.
- Backup round trip preserves notes/tags.
- Old backup imports with empty notes/tags.
- Weekly Report excludes free-text notes by default.
- Race detail context summary uses recent notes/tags without generating recommendations.
- Compose test covers weekly tags and note indicator.

## Acceptance Criteria

- User can capture at least weekly training context without disrupting planning.
- User can mark common constraints/recovery context with localized tags.
- Notes and tags can be edited or cleared.
- Activity logs never include free-text note contents.
- Backup/import includes persisted context.
- Weekly/Race review surfaces can show context without medical or readiness claims.
