# Event Types Expansion Spec

Target release: after Race Events usage clarifies whether non-race events need first-class labels.

## Goal

Expand event labels beyond Race/Event so users can distinguish race, trip, training camp, test day, rest weekend, or custom event types.

The feature should reuse the existing event model where possible.

## Product Fit

Hermes already has `RACE_EVENT`, but future planning may need event context that is not a race:

- travel.
- training camp.
- benchmark/test day.
- rest weekend.
- appointment.

## User Stories

- As a user, I can choose an event type.
- As a user, I can filter events by type.
- As a user, I can see event type labels in Week and Events.
- As a user, I can use event types in reports and context.

## First Version Scope

- Event subtype field for `RACE_EVENT` items or a broader `EVENT` model.
- Built-in subtypes:
  - Race.
  - Trip.
  - Training camp.
  - Test day.
  - Rest weekend.
  - Other.
- Filter Events by subtype.

## Out Of Scope For V1

- Separate tables per event type.
- External calendar categories.
- Automatic behavior per subtype.

## Data Direction

Likely schema change: add nullable `eventSubtype` to event/workout rows or introduce a related event metadata table.

Requires backup schema review.

## Activity Logging

Log subtype changes without custom free text.

## Acceptance Criteria

- Event subtypes are visible and filterable.
- Race behavior remains compatible.
- Backup/import preserves subtype.
