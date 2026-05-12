# Race Retrospective Spec

Target release: after Race Events, Personal Records, and Training Context exist.

## Goal

Let users capture what happened after a race/event: result, notes, what worked, what to change next time, and optional Personal Record link.

The feature should preserve race-day learning without becoming a performance analytics system.

## Product Fit

Hermes can already plan a race/event. A retrospective closes the loop:

- planned event.
- prep context.
- checklist completion.
- result.
- lessons learned.
- optional personal record.

## User Stories

- As a user, I can add a result to a past race/event.
- As a user, I can write what worked and what I would change.
- As a user, I can link the result to a Personal Record.
- As a user, I can review race retrospectives from the event detail.
- As a user, I can include the retrospective in a report if I choose.

## First Version Scope

- Add/edit race result after event date.
- Fields:
  - finish time.
  - distance, if not already structured.
  - optional note.
  - what worked.
  - what to change next time.
  - optional Personal Record link.
- Race detail shows result and retrospective summary.

## Out Of Scope For V1

- Race prediction.
- Automatic readiness conclusions.
- Public sharing.
- GPS import.
- Ranking against other users.

## Data Model Direction

Likely persisted:

```kotlin
data class RaceRetrospective(
    val id: Long,
    val raceEventId: Long,
    val finishTimeSeconds: Long?,
    val distanceMeters: Double?,
    val note: String?,
    val workedWell: String?,
    val changeNextTime: String?,
    val personalRecordId: Long?,
)
```

## Backup And Activity

Requires backup schema review.

Activity logging should capture create/update/delete and linked PR id, but not free-text notes.

## Acceptance Criteria

- User can record a race result and lessons learned.
- Free-text race notes are not logged in Activity.
- Personal Records can link to race results without requiring the PR feature in v1 if deferred.
