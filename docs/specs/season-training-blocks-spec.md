# Season And Training Blocks Spec

Target release: later, after Weekly Report, Training Context, and Race Events are stable.

## Goal

Let users group weeks into a broader training block such as base, build, race prep, recovery, or custom season phases.

The feature should help users remember the purpose of a period of training without prescribing a training plan.

## Product Fit

Hermes is weekly-first, but many training decisions happen across several weeks. Training blocks give the weekly planner a larger memory:

- Why this week exists.
- What race/event it leads toward.
- Whether the week is base, build, recovery, race prep, or transition.
- What the user learned across the block.

This should remain user-authored. Hermes can summarize the block but should not generate coaching prescriptions.

## User Stories

- As a user, I can create a training block with a name and date range.
- As a user, I can assign a block type such as base, build, race prep, recovery, or custom.
- As a user, I can link a block to a race/event.
- As a user, I can see which block the current week belongs to.
- As a user, I can review block-level notes and summary counts.
- As a user, I can include a block summary in reports.

## First Version Scope

- Create/edit/delete training block.
- Block name, type, start week, end week, optional linked race/event.
- Optional block note.
- Current week shows active block label.
- Block detail shows weekly summaries inside the block.

## Out Of Scope For V1

- Auto-generated training phases.
- Training plan recommendations.
- Load/intensity scoring.
- Coach sync.
- Race prediction.

## Data Model Direction

Likely persisted:

```kotlin
data class TrainingBlock(
    val id: Long,
    val name: String,
    val type: TrainingBlockType,
    val startWeek: LocalDate,
    val endWeek: LocalDate,
    val linkedEventId: Long?,
    val note: String?,
)
```

Block types should use stable enum keys and localized labels.

## Backup And Activity

Persisted blocks require Room migration and backup schema review.

Activity logging should cover create/update/delete, but not note contents.

## Acceptance Criteria

- Weeks can belong to a user-created block.
- Block labels are visible without crowding the planner.
- Block summaries are derived from existing weekly data.
- No training prescriptions are generated.
