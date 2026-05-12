# Today Home View Spec

Target release: later, if Week becomes too dense or users need a daily orientation surface.

## Goal

Show today's planned items, nearest race/event, and relevant context without replacing the weekly planner.

This should be a quick orientation view, not a full daily planner.

## Product Fit

Hermes is weekly-first, but users may open the app asking "what is today?" A Today view can answer that while preserving Week as the main planning model.

## User Stories

- As a user, I can see today's planned items.
- As a user, I can see the next race/event.
- As a user, I can see today's context tags or notes.
- As a user, I can mark workouts complete from Today.
- As a user, I can jump back to the full Week.

## First Version Scope

- Today card or Today surface inside Week.
- Today's items grouped by slot when available.
- Next race/event summary.
- Link to Week.

## Out Of Scope For V1

- Hourly calendar timeline.
- Separate daily planner model.
- Notification center.
- Automatic recommendations.

## Data Direction

Derived from weekly items and events. No new persistence required.

## Activity Logging

Only log state changes like completion using existing actions.

## Acceptance Criteria

- User can orient around today quickly.
- Week remains the primary planning surface.
- Today view does not introduce a separate schedule model.
