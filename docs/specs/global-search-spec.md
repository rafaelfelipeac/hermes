# Global Search Spec

Target release: after Hermes has enough history that retrieval becomes a real need.

## Goal

Let users search across workouts, race/events, notes, categories, Personal Records, gear, templates, and reports from one place.

The feature should help users find their training memory quickly.

## Product Fit

As Hermes gains notes, records, reports and history, capture becomes less valuable without retrieval. Search is the natural counterpart to the planning-memory direction.

## User Stories

- As a user, I can search by workout title.
- As a user, I can search race/events.
- As a user, I can search notes if I choose to include them.
- As a user, I can filter by result type.
- As a user, I can open a result in its owning screen.

## First Version Scope

- Search destination under Browse.
- Result types:
  - workouts.
  - race/events.
  - categories.
  - notes/context entries if implemented.
  - Personal Records if implemented.
- Local search only.

## Out Of Scope For V1

- Full-text ranking engine.
- Cloud search.
- Fuzzy matching beyond simple contains.
- Search suggestions from private text.

## Privacy Direction

Notes may be sensitive.

Recommendation:

- Include notes in search only if the user enables it, or make the search screen clearly local/private.
- Never log search queries.

## Data Direction

Start with repository-level local filtering. If performance becomes an issue later, evaluate Room FTS.

## Activity Logging

Do not log search queries.

## Acceptance Criteria

- User can search core local data.
- Results open the relevant screen/detail.
- Search remains local and private.
