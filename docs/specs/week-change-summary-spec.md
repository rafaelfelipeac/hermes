# Week Change Summary Spec

Target release: after Activity and Weekly Report language are stable.

## Goal

Show a calm summary of what changed in a week: moved sessions, added/deleted items, completions, context tags, and missed/reduced plans.

The feature should answer: "what changed this week?"

## Product Fit

Hermes already logs user actions. Week Change Summary turns those logs into a readable week-level reflection without making the user scan the full Activity feed.

## User Stories

- As a user, I can see a summary of changes for the selected week.
- As a user, I can understand how the plan evolved.
- As a user, I can include the summary in Weekly Report.
- As a user, I can tap into Activity for details.

## First Version Scope

- Derived summary for selected week:
  - added items.
  - moved items.
  - deleted items.
  - completed/incompleted items.
  - copied week.
  - context tags added.
- Link to filtered Activity for the week.

## Out Of Scope For V1

- Judgmental language.
- Readiness scoring.
- Automatic recommendations.
- New persisted summary table.

## Data Direction

Use `UserActionRepository` plus weekly item data. No new persistence for v1.

## UI Direction

Small card on Progress, Weekly Report, or Week detail:

- "This week changed 6 times"
- "Moved 3 sessions"
- "Completed 4 of 5 workouts"
- "Context: travel"

## Acceptance Criteria

- Summary is generated from existing logs.
- User can reach detailed Activity.
- Copy explains changes without judging them.
