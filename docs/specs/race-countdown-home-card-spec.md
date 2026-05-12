# Race Countdown Home Card Spec

Target release: after Events and Progress are stable; likely after Browse navigation if Week becomes a richer home surface.

## Goal

Show the nearest upcoming race/event context on the Week screen: countdown, checklist progress, recent context tags, and planned sessions until race day.

The card should help users orient around the next event without creating readiness claims.

## Product Fit

Week is the user's daily planning surface. When a race is near, a small card can connect:

- event date.
- checklist progress.
- current week plan.
- recent context.

## User Stories

- As a user, I can see the next race/event from the Week screen.
- As a user, I can see days remaining.
- As a user, I can see checklist progress when available.
- As a user, I can open the event detail.
- As a user, I can dismiss or hide the card if it is distracting.

## First Version Scope

- Show nearest upcoming race/event.
- Countdown label.
- Event title/date/category.
- Open event detail action.
- Optional checklist progress.

Later:

- recent context tags.
- planned workouts before race day.

## Out Of Scope For V1

- Readiness score.
- Race prediction.
- Recommended training changes.
- Weather integration.

## Data Direction

Derived from existing events and, later, checklist/context repositories. No new persistence except optional user setting to hide card.

## Acceptance Criteria

- Week can surface nearest race context.
- Card remains compact.
- Card does not claim readiness or recommend training changes.
