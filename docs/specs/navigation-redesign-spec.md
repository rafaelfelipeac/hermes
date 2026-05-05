# Navigation Redesign Spec

Target release: after Progress proves enough value to become a top-level destination.

## Goal

Rethink the app shell so Hermes can add more destinations without crowding the bottom navigation.

Earlier top-level destinations:

- Week
- Activity
- Trophies
- Settings

The proposed direction is to keep the bottom navigation focused on frequent navigation and move secondary destinations into a Browse hub.

## Current Context

Current navigation is defined by:

- `core/navigation/AppDestinations.kt`
- `features/app/HermesAppContent.kt`

`HermesAppContent` iterates over every `AppDestinations` entry and renders them directly in `NavigationSuiteScaffold`. There is no nested nav graph. Settings currently uses local route state for sub-screens.

## Proposed Information Architecture

### Top-Level Destinations

Replace the current flat set with a more intentional shell.

Stable base:

- Week
- Browse

Avoid shipping a two-item bottom nav. It feels sparse and underuses a navigation pattern designed for multiple peer destinations.

Target direction:

- Four items: `Week`, `Progress`, `Race Events`, `Browse`.

Recommendation: keep `Week` as the first tab while the destination remains the weekly planner. Move to `Home` only if the first destination becomes broader than the current weekly planner.

### Week

Week is the default destination.

It should keep opening directly into the weekly planner.

Do not rename Week to Home unless the first destination earns the broader name by including more than the week, such as:

- Current week.
- Near upcoming race event.
- High-level planning summary.

### Progress

Progress is the preferred replacement for Activity as a bottom-nav concept.

Reasoning:

- Activity by itself feels like an audit log, not a primary destination.
- Progress can contain Activity while also making room for Trophies, weekly completion, streaks and future race-prep signals.
- The label reads more user-facing than Activity and fits the app's planning/training tone.

Initial Progress scope can stay small:

- Activity timeline as the primary content or first section.
- A compact trophy/progress highlight.
- A link into the full Trophies shelf.

This avoids making Activity a lonely middle tab while preserving one-tap access to history.

### Intermediate Three-Item Option

If Race Events is not ready when Progress ships, the temporary shell can be:

- `Week`
- `Progress`
- `Browse`

This gives the app a balanced bottom nav without inventing a weak destination just to fill space.

### Target Four-Item Option

Target shell once Race Events exists:

- `Week`
- `Progress`
- `Race Events`
- `Browse`

This makes Race Events a first-class planning surface while keeping Browse as the hub for supporting destinations.

### Browse

Browse is a hub for lower-frequency and exploratory destinations.

Initial Browse entries:

- Activity.
- Categories.
- Trophies.
- Backup and Import.
- Settings.

Future Browse entries:

- None required for the current plan.

This is similar to apps that keep primary workflows in the tab bar and place the rest of the product map behind Browse.

Activity should live in Browse once Progress exists. Progress may preview recent activity and link into the full Activity timeline.

## Why Browse Instead Of More

`More` is accurate but generic. `Browse` suggests that the destination contains useful app areas, not only overflow settings.

`Browse` also scales better for Race Events because Race Events is product content, not configuration. If the hub contains Trophies and future Race Events, `Browse` reads better than `More`.

Potential labels to evaluate:

- `Browse`
- `Explore`
- `Menu`

Recommendation: start with `Browse`.

## Events Placement

Events should remain a direct bottom-nav item in the target four-item shell.

Rationale:

- Race Events has its own aggregate screen, create action and countdown use case.
- Users may open it to compare upcoming races/events throughout the year.
- It is closer to planning than to Settings-like Browse content.

If implementation needs an intermediate step, Events can temporarily stay in the release shell before the final Browse redesign.

## Browse Hub Structure

Browse should not look like Settings.

Recommended structure:

- Header: simple title and optional short description.
- Primary cards for product destinations:
  - Activity.
  - Categories.
  - Trophies.
  - Backup and Import.
- Utility rows/cards:
  - Settings.

Trophies should feel like a destination card, not a settings row.

Activity should remain accessible as the full event timeline.

Categories should become a direct Browse destination because they shape how the planner is organized, not only an app preference.

Backup and Import should become a direct Browse destination because it is a data-management workflow, not a general preference.

Settings should remain visually distinct as app preferences and should no longer need to own Categories or Backup as first-level sections after Browse exists.

## Routing Model

Recommended refactor:

- Replace direct `AppDestinations.entries` rendering with a stable top-level route model.
- Add a Browse route with internal destinations.
- Keep nested route state outside individual feature screens when cross-destination navigation is needed.
- Preserve pending workflows:
  - Manage categories from workout dialog.
  - Trophy snackbar action opens the specific trophy, even though Trophies is no longer top-level.

Potential route shape:

```kotlin
enum class TopLevelDestination {
    WEEK,
    PROGRESS,
    EVENTS,
    BROWSE,
}

enum class BrowseDestination {
    ROOT,
    ACTIVITY,
    CATEGORIES,
    TROPHIES,
    BACKUP,
    SETTINGS,
    EVENTS,
}
```

Do not define these inside a ViewModel.

## Product Copy Direction

Top-level labels should be short:

- `Week`
- `Progress`
- `Race Events`
- `Browse`

Do not use `Browser`; it implies a web browser. Use `Browse`.

Potential localized labels must be reviewed specifically for bottom navigation width. Do not reuse long screen titles as nav labels.

## Migration Plan

1. Introduce the new top-level route model.
2. Keep Week as the default direct destination.
3. Add Browse root screen.
4. Move Activity, Categories, Trophies, Backup and Settings under Browse.
5. Remove Categories and Data/Backup from the first level of Settings once their Browse entries exist.
6. Update trophy snackbar action to navigate to Browse/Trophies and target the requested trophy.
7. Keep Manage Categories flow working through Browse/Categories and returning to the pending workout flow.
8. Prototype `Progress` as the Activity replacement.
9. Keep Events as a direct bottom-nav item once the final shell exists.

## Testing

Recommended tests:

- App shell starts on Week.
- Browse opens the hub.
- Progress opens Activity-derived history or progress content when implemented.
- Events opens the aggregate events list.
- Activity opens from Browse.
- Categories open from Browse.
- Trophies open from Browse.
- Backup and Import open from Browse.
- Settings opens from Browse.
- Trophy snackbar action opens Trophies through Browse and targets the requested trophy.
- Manage categories from workout dialog routes through Browse/Categories and returns correctly.
- Locale-sensitive nav labels render without obvious overflow in Compose tests where practical.

## Acceptance Criteria

- Bottom navigation target is `Week`, `Progress`, `Race Events`, `Browse`.
- Week remains one tap from app launch.
- Activity is replaced by a broader Progress concept as a top-level tab and remains reachable from Browse.
- Activity, Categories, Trophies, Backup and Import, and Settings are reachable from Browse.
- Categories and Backup are no longer first-level Settings rows after Browse owns them.
- Events remains direct in the target shell.
- Existing cross-destination flows still work.
- Navigation change is reviewed explicitly before implementation.
