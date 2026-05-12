# Onboarding And First Week Setup Spec

Target release: when first-run experience becomes a priority, likely after the main Week/Progress/Browse structure is stable.

## Goal

Help new users create a useful first week quickly: choose basic settings, keep or customize starter categories, add first workouts, and optionally add a first race/event.

The feature should reduce empty-state friction without turning onboarding into a long account-style setup.

## Product Fit

Hermes is offline-first and personal. The first run should feel like setting up a notebook, not registering for a platform.

Good onboarding should:

- keep the app usable if skipped.
- avoid accounts.
- avoid asking for too much upfront.
- create a useful week in under a minute.

## User Stories

- As a new user, I can choose my week start day.
- As a new user, I can review starter categories.
- As a new user, I can add a few workouts to my first week.
- As a new user, I can optionally add an upcoming race/event.
- As a new user, I can skip onboarding and use the app normally.
- As a returning user, I do not see onboarding again unless I reset it.

## First Version Scope

- First-run onboarding shown only when there is no meaningful user data.
- Steps:
  - welcome/value statement.
  - week start day.
  - category starter review.
  - add first workout or skip.
  - optional race/event.
- Final action opens Week.

## Out Of Scope For V1

- Account creation.
- Cloud sync.
- Training goal questionnaire.
- AI-generated plan.
- Required fitness profile.
- Notifications permission prompt unless reminders are being introduced.

## Data Direction

Use existing settings, categories, workouts, and race/event storage.

Persist only:

- onboarding completed flag.

If the user skips onboarding, mark it completed but keep a settings entry to restart it later.

## Backup And Activity

Backup should include onboarding flag only if settings backup already includes similar preferences. It is not critical user training data.

Activity logging:

- Do not log onboarding navigation.
- Log actual state changes through existing category/workout/race-event paths.

## Testing

Recommended tests:

- Onboarding appears for an empty new install.
- Onboarding does not appear when user data exists.
- Skip opens Week and marks onboarding complete.
- Week start selection persists.
- Added first workout appears in Week.
- Optional race/event appears in Events.

## Acceptance Criteria

- User can complete or skip onboarding.
- No account or network is required.
- Onboarding creates normal app data through existing flows.
- Returning users are not blocked by onboarding.
