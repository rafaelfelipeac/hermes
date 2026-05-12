# Race Prep Checklist Spec

Target release: after Race Events and Browse are stable; likely after Notes/Training Context exists if the checklist should use recent notes to guide race decisions.

## Goal

Give users a lightweight checklist tied to a race/event so they can prepare calmly without turning Hermes into a coaching or race-prediction app.

The checklist should answer: "what do I need to remember before this event?"

## Product Fit

Hermes already supports race events as dated planning items. A race prep checklist is a natural extension:

- The race/event owns the target date.
- Weekly training owns the plan leading into the event.
- Progress and reports can summarize preparation.
- Notes and recovery/constraint tags can provide context before race day.

The feature should stay practical: packing, logistics, simple reminders, and user-authored prep items. It should not claim whether the user is ready or prescribe medical/training decisions.

## User Stories

- As a user, I can add checklist items to a race/event.
- As a user, I can mark checklist items done.
- As a user, I can reorder checklist items.
- As a user, I can use a starter checklist for common race prep.
- As a user, I can see checklist progress on the race/event detail.
- As a user, I can review unresolved items before race day.
- As a user, I can optionally include checklist status in a weekly/race report.

## First Version Scope

Recommended v1:

- Race/event detail section: `Checklist`.
- Add/edit/delete checklist item.
- Mark item done/undone.
- Reorder items.
- Starter checklist action for a race event.
- Progress summary: `3 of 8 done`.

Starter checklist examples:

- Confirm race start time.
- Prepare race outfit.
- Prepare shoes.
- Check route or location.
- Plan travel time.
- Prepare nutrition/hydration.
- Charge watch/phone.
- Set alarm.

Keep starter items editable after insertion.

## Out Of Scope For V1

- Automatic checklist generation from distance/category.
- Training readiness or taper advice.
- Medical guidance.
- Weather integration.
- External calendar sync.
- Push notifications for each checklist item.
- Shared/public checklist templates.

## Placement

Primary placement:

- Race/event detail or edit flow.

Secondary placement:

- Events list can show a small checklist progress indicator for upcoming events.
- Browse does not need a standalone checklist destination.

Future integration:

- Progress can show nearest race/event with checklist progress.
- Weekly Report or a future Race Report can include open checklist items.

## Data Model Direction

This feature likely needs persisted data.

Suggested model:

```kotlin
data class RacePrepChecklistItem(
    val id: Long,
    val raceEventId: Long,
    val title: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val completedAt: String?,
)
```

Storage options:

1. New `race_prep_checklist_items` table.
2. JSON blob field attached to a race event.

Recommendation:

- Use a dedicated table. Checklist items are ordered, individually mutable, and likely need backup/import support.

Important:

- Race events currently live as weekly items with `EventType.RACE_EVENT`. `raceEventId` can reference that row id.
- If a race event is deleted, checklist items should be deleted in the same operation or through a repository transaction.

## Backup Compatibility

Persisted checklist items require backup schema review.

Requirements:

- Add a new backup schema version.
- Export/import checklist items.
- Older backups import with empty checklists.
- Missing race-event references should be handled explicitly during import.
- Update `docs/backup-compatibility-policy.md`.

Recommended import rule:

- If a checklist item references a missing race event, reject the backup as an invalid reference.

## Activity Logging

Checklist state changes should appear in Activity only if they are user-visible enough to matter.

Recommended v1 logging:

- `CREATE_RACE_CHECKLIST_ITEM`
- `UPDATE_RACE_CHECKLIST_ITEM`
- `DELETE_RACE_CHECKLIST_ITEM`
- `COMPLETE_RACE_CHECKLIST_ITEM`
- `INCOMPLETE_RACE_CHECKLIST_ITEM`
- `RESTORE_RACE_CHECKLIST_TEMPLATE`

Recommended entity type:

- `RACE_CHECKLIST_ITEM`

Metadata should avoid free-text checklist titles unless the item came from a fixed starter template.

Safe metadata:

- race event id.
- race event date.
- checklist item id.
- checklist item template key, if template-based.
- old/new completion state.
- old/new order.

## UI Direction

### Race/Event Detail

Checklist section:

- Compact progress line: `3 of 8 done`.
- List of items with checkboxes.
- Add item action.
- Starter checklist action when the list is empty, or secondary menu when not empty.

Item row:

- Checkbox.
- Title.
- Drag/reorder handle or move actions.
- Overflow actions: edit, delete.

### Empty State

Copy direction:

- "Add a few reminders for race day."
- Primary action: "Use starter checklist".
- Secondary action: "Add item".

### Events List

Optional small signal:

- `Checklist 3/8`
- Only show for events that have checklist items.

## Relationship To Training Notes And Constraints

Race Prep Checklist is about concrete tasks.

Training notes and recovery/constraint tags are about context:

- "Low sleep this week."
- "Travel day before the race."
- "Soreness after long run."
- "Busy week, reduced training."

Do not merge these into the checklist. Instead, future race-event detail can show a small "Recent context" section sourced from training notes/tags.

## Localization

All checklist labels, template items, empty states and Activity titles must be localized.

Starter checklist templates should use resource keys, not raw strings stored as canonical data.

Recommended persistence:

- Store a `templateKey` for starter checklist items when applicable.
- Store custom title only for custom items.

## Testing

Recommended tests:

- Add checklist item to race event.
- Mark item complete/incomplete.
- Reorder checklist items.
- Delete race event deletes or rejects orphaned checklist items according to chosen repository rule.
- Starter checklist inserts localized/template-backed items in stable order.
- Backup round trip preserves checklist items.
- Old backup imports with empty checklist list.
- Import rejects checklist item referencing missing race event.
- Activity logging omits custom checklist title text.
- Compose test covers empty and populated checklist states.

## Acceptance Criteria

- Checklist is tied to a race/event.
- User can add, edit, delete, complete and reorder checklist items.
- Starter checklist can be inserted and edited.
- Checklist progress is visible from the race/event surface.
- Persisted checklist data participates in backup/import.
- Activity logging avoids custom free-text leakage.
- No readiness, medical, or race-performance claims are made.
