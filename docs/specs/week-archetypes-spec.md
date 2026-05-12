# Reusable Week Archetypes Spec

Target release: later, after templates/reusable routines are evaluated.

## Goal

Let users save and apply common week shapes such as busy work week, travel week, base week, race week, or recovery week.

The feature should reduce repetitive planning while keeping the final week editable.

## Product Fit

Hermes already supports copy-last-week. Week archetypes are a more intentional version of that idea:

- They capture a pattern, not a date-specific plan.
- They help users start a week quickly.
- They can pair with context tags like travel or busy week.

## User Stories

- As a user, I can save the current week as an archetype.
- As a user, I can name an archetype.
- As a user, I can apply an archetype to an empty or existing week.
- As a user, I can edit or delete an archetype.
- As a user, I can keep category, title, day, slot, and order information.

## First Version Scope

- Save current week as archetype.
- Apply archetype to selected week.
- Confirm replace/merge behavior before applying.
- Edit archetype name.
- Delete archetype.

Recommended default archetypes can be considered later, but v1 should start user-created.

## Out Of Scope For V1

- Marketplace/shared templates.
- AI-generated weeks.
- Automatic application by calendar.
- Complex periodized plans.

## Data Model Direction

Likely persisted:

- `WeekArchetype`
- `WeekArchetypeItem`

Items should not preserve completion state or race-event date. Applying an archetype should create fresh weekly items.

## Backup And Activity

Requires backup schema review if persisted.

Activity logging should cover create/update/delete/apply archetype. Do not log custom notes/free text beyond safe generated labels.

## Acceptance Criteria

- User can create and apply reusable week patterns.
- Applying an archetype does not force a permanent plan.
- Existing week data is protected by confirmation when replace behavior is possible.
