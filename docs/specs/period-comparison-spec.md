# Period Comparison Spec

Target release: after Progress and Training Blocks exist.

## Goal

Let users compare two selected periods, such as this block vs previous block or this month vs last month, descriptively and without scoring.

## Product Fit

Progress shows trends. Period comparison helps answer:

- What changed between two blocks?
- Which categories increased or decreased?
- Did context tags explain a lower-completion period?

## User Stories

- As a user, I can choose two date ranges.
- As a user, I can compare completed workouts, planned items, categories, race/events, PRs and context tags.
- As a user, I can see differences without judgmental language.
- As a user, I can include comparison in a report later.

## First Version Scope

- Compare two week-aligned periods.
- Summary:
  - planned workouts.
  - completed workouts.
  - category distribution.
  - race/event count.
  - context tags.
- Simple deltas only.

## Out Of Scope For V1

- Performance predictions.
- Statistical analysis.
- Readiness claims.
- Automatic conclusions.

## Data Direction

Derived from existing repositories. No new persistence.

## Acceptance Criteria

- User can compare two periods locally.
- Copy remains descriptive.
- No new persisted data is required.
