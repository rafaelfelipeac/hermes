# Plan Confidence Spec

Target release: after Training Context and Weekly Report direction are stable.

## Goal

Let users manually mark how realistic a week feels: low, medium, or high confidence.

This is not readiness. It is the user's own expectation for the plan.

## Product Fit

Hermes is about adaptable planning. A confidence marker lets users preserve intent:

- "This week is ambitious."
- "This plan is realistic."
- "This week is uncertain because of travel."

## User Stories

- As a user, I can set plan confidence for a week.
- As a user, I can change or clear confidence later.
- As a user, I can see confidence in weekly reports.
- As a user, I can compare confidence with what happened later.

## First Version Scope

- Week-level confidence:
  - Low.
  - Medium.
  - High.
- Optional short note if Training Context supports weekly notes.
- Display near weekly header or context row.

## Out Of Scope For V1

- Automatic confidence calculation.
- Readiness score.
- Recommendations.
- Notifications.

## Data Model Direction

Persist week-level field or use Training Context entry.

Recommendation:

- If Training Context exists, store plan confidence there as week context.
- Otherwise use a small week metadata table.

## Backup And Activity

Requires backup schema review if persisted.

Activity logging should cover set/update/clear confidence.

## Acceptance Criteria

- Confidence is manually set by the user.
- UI copy makes clear this is plan realism, not health/readiness.
- Reports can include it as user context.
