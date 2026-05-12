# Soft Planning Assistant Spec

Target release: later, after Hermes has enough user-authored history and trust.

## Goal

Offer optional planning suggestions based on the user's own history, without claiming to coach, optimize, or prescribe training.

Example: "You often add strength twice a week. Add one this week?"

## Product Fit

Hermes should remain user-led. A soft assistant can reduce repetitive planning only if it stays:

- explainable.
- dismissible.
- history-based.
- non-authoritative.

## User Stories

- As a user, I can see optional planning suggestions.
- As a user, I can accept a suggestion.
- As a user, I can dismiss a suggestion.
- As a user, I can turn suggestions off.
- As a user, I can understand why a suggestion appeared.

## First Version Scope

- Local, rule-based suggestions only.
- Candidate suggestions:
  - repeat common category frequency.
  - add a saved library workout.
  - apply a week archetype.
  - remind about an upcoming race checklist.
- Suggestion cards on Week or Browse.

## Out Of Scope For V1

- AI training plans.
- Performance optimization.
- Medical/recovery advice.
- Cloud model inference.
- Automatic schedule changes.

## Data Direction

Derived from existing local data:

- categories.
- previous weeks.
- training library.
- week archetypes.
- race events/checklists.

No persistence required except settings for enabling/disabling and dismissed suggestion state.

## Activity Logging

Log accepted suggestions only if they create state changes. Dismissals probably do not need Activity unless persisted.

## Acceptance Criteria

- Suggestions are optional and explainable.
- User remains in control.
- Suggestions never claim what the user should do.
