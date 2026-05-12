# Training Library Spec

Target release: later, before or alongside week templates if repeated workout entry becomes a clear friction.

## Goal

Let users save favorite workout definitions and reuse them when planning.

This should make planning faster without requiring a formal training-plan system.

## Product Fit

Hermes users likely repeat sessions:

- Easy run.
- Long run.
- Gym A.
- Mobility.
- Bike intervals.

A library lets users create from known patterns while still editing each planned item.

## User Stories

- As a user, I can save a workout as a library item.
- As a user, I can create a weekly item from the library.
- As a user, I can edit or delete library items.
- As a user, I can categorize library items.
- As a user, I can keep descriptions reusable but editable.

## First Version Scope

- Library destination under Browse or inside Add Workout.
- Save workout definition:
  - title.
  - description.
  - category.
  - optional event type limited to workout in v1.
- Insert from library into selected day/slot.

## Out Of Scope For V1

- Week templates.
- Structured intervals.
- Auto-scheduling.
- Shared libraries.
- External imports.

## Data Model Direction

Likely persisted:

```kotlin
data class TrainingLibraryItem(
    val id: Long,
    val title: String,
    val description: String,
    val categoryId: Long?,
    val sortOrder: Int,
)
```

## Backup And Activity

Requires backup schema review.

Activity logging should cover library item create/update/delete and insertion from library.

## Acceptance Criteria

- Reusable workouts can be saved and inserted.
- Inserted workouts remain normal editable weekly items.
- Library use does not create automatic plans.
