# Workout References Spec

Target release: later, after Training Library and Notes direction are stable.

## Goal

Let users attach lightweight references to workouts, race/events, or library items, such as route links, gym program links, race pages, or local notes.

This should support useful context without turning Hermes into file storage.

## Product Fit

Users may already keep training details elsewhere:

- race page.
- route map.
- strength program.
- coach document.
- video reference.

Hermes can store a simple reference so the plan points to the right place.

## User Stories

- As a user, I can add a link/reference to a workout.
- As a user, I can add a link/reference to a race/event.
- As a user, I can open the reference from the item detail.
- As a user, I can edit or remove a reference.
- As a user, I can keep notes separate from references.

## First Version Scope

- One or more text URL references per item.
- Optional label per reference.
- Support workouts and race/events first.
- Open reference through Android intent.

## Out Of Scope For V1

- File attachments.
- Image/video upload.
- Cloud storage.
- Downloading remote content.
- Preview thumbnails.
- Permission-heavy document picker.

## Data Direction

Likely persisted table:

```kotlin
data class ItemReference(
    val id: Long,
    val entityType: ReferenceEntityType,
    val entityId: Long,
    val label: String?,
    val uri: String,
    val sortOrder: Int,
)
```

## Backup And Activity

Backup schema review required if references are persisted.

Activity logging should capture add/update/delete reference, but avoid logging full URL if considered sensitive.

## Acceptance Criteria

- User can add, open, edit and remove item references.
- References are optional.
- Hermes does not store external files in v1.
