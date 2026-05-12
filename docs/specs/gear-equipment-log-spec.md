# Gear And Equipment Log Spec

Target release: later, after categories and reports are stable.

## Goal

Let users track equipment usage such as running shoes, bike, or strength gear across planned/completed sessions.

The feature should be useful and lightweight, not a full inventory system.

## Product Fit

Gear tracking fits Hermes when it helps answer practical questions:

- Which shoes did I use?
- How many kilometers are on this pair?
- Which bike did I ride?

It should avoid turning the app into a maintenance platform.

## User Stories

- As a user, I can add gear.
- As a user, I can assign gear to a workout.
- As a user, I can see usage totals by gear.
- As a user, I can retire gear.
- As a user, I can include gear notes in reports when useful.

## First Version Scope

- Gear list under Browse.
- Gear fields:
  - name.
  - category/sport.
  - optional start date.
  - optional notes.
  - retired flag.
- Assign one gear item to a workout.
- Show total completed sessions per gear.

Optional running-specific later addition:

- Distance totals if structured workout distance exists.

## Out Of Scope For V1

- Maintenance reminders.
- Multiple gear items per workout.
- Purchase tracking.
- Cost tracking.
- External device integration.

## Data Model Direction

Likely persisted:

- `GearItem`
- optional workout `gearId`, or join table if multiple gear items are needed later.

Recommendation:

- Use a join table if the team expects shoes + watch + bike-like combinations later.
- Use one gear id on workout only if v1 must stay minimal.

## Backup And Activity

Requires backup schema review.

Activity logging should cover gear create/update/retire and gear assignment, but not free-text notes.

## Acceptance Criteria

- User can track gear and assign it to completed/planned workouts.
- Retired gear remains visible in history.
- Gear tracking remains optional.
