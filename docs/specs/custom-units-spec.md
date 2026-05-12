# Custom Units Spec

Target release: before Personal Records, Gear, or structured distance fields become broad.

## Goal

Let users choose preferred units for distance, pace, speed, weight, and strength records.

The feature should make Hermes usable across metric and imperial contexts without complicating the core planner.

## User Stories

- As a user, I can choose kilometers or miles.
- As a user, I can choose kg or lb for strength records.
- As a user, I can see pace in min/km or min/mile.
- As a user, I can store values consistently while displaying preferred units.

## First Version Scope

- Settings:
  - distance unit.
  - weight unit.
- Display conversions for Personal Records and Pace Calculator.

## Out Of Scope For V1

- Per-category unit preferences.
- Power/cadence/heart-rate zones.
- Complex localization beyond unit display.

## Data Direction

Persist preferences in DataStore.

Store canonical values internally:

- meters for distance.
- seconds for duration.
- kilograms or a chosen canonical weight unit.

## Backup And Activity

Backup settings if settings backup includes other preferences.

Log unit preference changes.

## Acceptance Criteria

- User can choose core units.
- Calculations remain correct across display units.
- Stored values are canonical enough to avoid migration churn.
