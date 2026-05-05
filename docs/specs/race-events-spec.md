# Race And Events Spec

Status: implementation reference for the shipped Events feature. Treat this spec as context and boundary documentation, not as active next-release scope.

Target release: `v1.9.0`.

The first Events version is now the release baseline. Future work should be split into dedicated specs instead of expanding this document.

## Goal

Add race/events as a planner item type that can be created from the weekly screen or from a dedicated Events screen.

Do not reintroduce temporary navigation shortcuts only because earlier planning mentioned them. The active navigation direction lives in `navigation-redesign-spec.md`.

The main use case is simple:

- The user has several races/events during the year.
- They want to place each event on the weekly planner.
- They want a separate view that answers "how many days until each one?".
- They want to add a new race/event from that separate view without returning to the weekly calendar.
- They use that distance between events to organize preparation workouts.

This feature should feel close to creating a workout, but visually distinct enough that the user understands "this is an event/race, not a training session".

## Product Scope

### User Stories

- As a user, I can tap the weekly Floating Action Button and choose Race/Event.
- As a user, I can open Race & Events and create a race/event directly from that screen.
- As a user, I can reach Events through the release shell while the final navigation redesign is still pending.
- As a user, I can still reach the existing non-Events destinations supported by the release shell.
- As a user, I can create a race/event with title, description and category.
- As a user, I must select the actual event date before the race/event is saved.
- As a user, I can create an event that is several weeks away without dragging it across weeks.
- As a user, I can see race/events inside the weekly planner with category color plus a distinct event treatment.
- As a user, I can open a Race & Events screen and see all upcoming events ordered by how close they are.
- As a user, I can quickly understand "20 days left", "55 days left" and how much preparation time exists between events.

### Initial Fields

Required:

- Title.
- Event date selected through a date/calendar picker.

Optional:

- Description.
- Category.
- Time slot, when slot mode is enabled.

Distance, location, notes or target details can live in description in the first release, matching how users already write workout distance today.

Do not add structured distance fields in the first release unless there is a concrete UI that uses them.

## Product Boundaries

Events should not generate training plans in the first release.

First release should focus on:

- Easy creation from the weekly FAB.
- Easy creation from the Race & Events screen.
- Weekly planner visibility.
- Dedicated upcoming-event list with countdowns.
- Category integration.
- Activity logging.
- Backup/import compatibility.

Future specs can cover:

- Structured distance.
- Target time.
- Event location.
- Linking workouts to a specific race/event.
- Race-prep recommendations.
- Race-specific trophies.

## Creation Flow

Race/Event can be created from two entry points:

- Weekly planner FAB.
- Race & Events screen.

Both entry points should use the same create/edit form and validation rules.

### Weekly FAB Entry

Race/Event should be added to the existing weekly FAB menu alongside:

- Workout.
- Rest.
- Busy.
- Sick.

### Race & Events Screen Entry

Race & Events should include an add action.

Recommended placement:

- Floating action button if the screen uses a list-heavy layout.
- Prominent empty-state action when no events exist.
- Optional top action if the final layout has a toolbar/header.

The dialog should reuse the workout creation pattern where it fits:

- Title field.
- Description field.
- Category picker.
- Optional slot selector when slot mode is enabled.

Race/Event differs from Workout creation in one important way:

- Workout creation can start in the current week and then be dragged/reordered.
- Race/Event creation must require an explicit event date before save.

The user should not be able to save the race/event without choosing a date.

The user can still cancel/dismiss the dialog and lose the draft intentionally. Do not trap the user inside the modal.

Date picker requirements:

- Provide a calendar/date picker inside the create/edit flow.
- Default to the currently selected planner day if that day is a reasonable starting point.
- Allow choosing dates outside the current visible week.
- After save, derive `weekStartDate` and `dayOfWeek` from the selected event date.
- The event should appear automatically when the user navigates to the derived week.

Do not require the user to drag a race/event across multiple weeks to schedule a future race.

Copy should make the intent clear:

- Add race/event.
- Edit race/event.
- Delete race/event.

## Weekly Planner Behavior

Race/events appear directly in the weekly planner as their own row type.

Visual treatment:

- Keep category accent color when a category is selected.
- Add a distinct event/race signal so it does not read as a workout.
- Candidate treatments: event icon, race flag icon, stronger outline, dashed border, or small "Event" chip.
- Do not rely only on color because category color already has meaning.

Interaction:

- Race/events can be edited and deleted like other planner items.
- Reorder should work within a day/slot.
- Moving across day/slot can be allowed if implemented consistently with current drag/drop behavior.
- If moving across days is allowed, it changes the event date and must be logged as an update/move.
- Editing the date in the dialog should move the item to the derived week/day.

Cross-week behavior:

- Creating a race/event from the current week can save it into a future week.
- The current visible week does not need to change automatically after save.
- If the selected date belongs to another week, show lightweight confirmation that the event was scheduled for that date/week.

Completion:

- First release should not require a completed/cancelled status unless the UI needs it.
- If a race/event is in the past, the dedicated Race & Events screen can group it as past based on date.
- Manual completion/cancellation can be a future feature.

## Race & Events Screen

The dedicated screen answers:

- What events are coming?
- How many days until each event?
- How much time exists between the next events?

Initial list:

- Upcoming events sorted by date ascending.
- Past events grouped below or hidden behind a secondary section.

Each row/card should show:

- Title.
- Category label/color when available.
- Date.
- Countdown label, for example "20 days left".
- Description preview when available.

Useful secondary signal:

- Show the gap from the previous upcoming event when helpful, for example "14 days after previous event".

Empty state:

- Explain that events can be added from this screen or from the weekly planner FAB.
- Include a primary action to add a race/event.

## Data Model Direction

Use the existing weekly item model with a new event type:

```kotlin
enum class EventType {
    WORKOUT,
    REST,
    BUSY,
    SICK,
    RACE_EVENT,
}
```

Reasoning:

- The user creates the item from the weekly planner.
- The event belongs to a date/day/slot.
- Title, description and category already match the needed first-release fields.
- The dedicated Race & Events screen can query/filter weekly items by `eventType = RACE_EVENT` across weeks.

Do not create a separate `RaceEventEntity` in the first release unless requirements change to need fields that do not fit the weekly item model.

Important persistence note:

- Adding `RACE_EVENT` likely does not require a Room table migration if no new columns are added.
- It does change the backup/import contract because `eventType` gains a new accepted enum value.
- Follow the backup compatibility policy and decide whether this requires a new backup schema version before implementation.

## Repository And Query Direction

The Race & Events screen needs access to race/event rows across weeks, not only the currently selected week.

Potential repository support:

- Observe all race/event rows from today forward.
- Observe past race/event rows when the screen needs a history section.
- Sort by concrete event date derived from `weekStartDate + dayOfWeek`.

Avoid loading all workouts in the ViewModel and filtering there if DAO support can keep the query focused.

## Navigation

Navigation is intentionally not finalized in this spec.

Initial access must exist somewhere outside the weekly FAB so the user can review all events.

The v1 release shell is interim. Do not block Events on the broader navigation redesign, and do not treat the release shell as the final product structure.

The Race & Events screen must include its own create action regardless of where it is placed in navigation.

## Activity Logging

Race/events are state-changing and must appear in Activity.

Required actions:

- Create race/event.
- Update race/event.
- Move race/event, if drag/drop can change day or slot.
- Reschedule race/event, if date changes through the dialog.
- Reorder race/event, if ordering changes within a day/slot.
- Delete race/event.
- Undo delete race/event, if undo is supported.

Recommended entity type:

- `UserActionEntityType.RACE_EVENT`

Metadata should reuse existing workout/event metadata where possible:

- Entity id.
- Title.
- Description when existing patterns already include it.
- Category id/name/color.
- Week start date.
- Day of week.
- Time slot.
- Old/new date or slot for move/reschedule/update actions.

Activity filters should include race/event once the entity type exists.

## Backup Compatibility

Race/events require backup contract review even if Room schema does not change.

Required:

- Ensure backup export includes race/event rows.
- Ensure import accepts `RACE_EVENT`.
- Decide whether adding a new `eventType` value requires a new backup schema version.
- Keep old backups importable.
- Update `docs/backup-compatibility-policy.md` if the contract changes.

Do not gate import compatibility by app version.

## Trophies

Race-event trophy behavior belongs in release QA and follow-up checks, not new Events scope.

Implemented or release-boundary trophy ideas:

- First race/event added.
- Race week planned.
- Race prep completed.

Future trophy ideas should move to a dedicated trophy/progress spec if they become active:

- Multiple race/events planned in a year.
- Workouts planned between two race/events.
- Race/event week completed with preparation workouts.

## UI Direction

Race/events should feel distinct from workouts while still fitting the planner.

Weekly row:

- Category accent remains visible.
- Add event-specific icon or border treatment.
- Avoid making the row look like a completed workout target.

Race & Events screen:

- Countdown is the primary information.
- Date and category are secondary.
- Description preview is optional.
- Sorting should make the closest event easiest to find.

## Testing

Required tests:

- Weekly ViewModel creates race/event with selected calendar date/slot/category.
- Race & Events ViewModel creates race/event with selected calendar date/category.
- Creating a race/event for a future date saves it into the derived `weekStartDate` and `dayOfWeek`.
- Race/event created for a future week appears when observing/navigating to that week.
- Editing a race/event date moves it to the newly derived week/day.
- Race/event appears in weekly state with `EventType.RACE_EVENT`.
- Race & Events ViewModel sorts upcoming events by date.
- Countdown labels handle today, tomorrow, future dates and past dates.
- Activity logging for create/update/move/reorder/delete.
- Backup export/import round trip preserves race/event rows.
- Backup decoder handles old backups without race/event rows.
- Compose tests for weekly FAB entry, Race & Events add action, weekly row visual identity and Race & Events list ordering.

## Acceptance Criteria

- User can create a race/event from the weekly FAB.
- User can create a race/event from the Race & Events screen.
- User must select an event date before saving.
- Race/event can be scheduled directly into a future week from the create dialog.
- Race/event can include title, description and category.
- Race/event appears in the weekly planner with category color and distinct event styling.
- User can open a Race & Events screen and see upcoming events ordered by date.
- Race & Events screen shows countdowns such as days remaining.
- Activity timeline shows race/event state changes.
- Backup export/import preserves race/event rows.
- No separate race/event table is introduced unless requirements change before implementation.
