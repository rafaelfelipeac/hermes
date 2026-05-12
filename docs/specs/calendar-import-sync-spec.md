# Calendar Import And Sync Spec

Target release: later, after Events, Privacy Controls, and Backup are stable.

## Goal

Let users bring real-life calendar context into Hermes, and possibly sync selected Hermes events back to a device calendar, while preserving offline-first control and privacy.

The feature should help users account for constraints such as travel, appointments, races, or busy periods without turning Hermes into a full calendar app.

## Product Fit

Hermes planning is affected by life outside the app. Calendar context can explain why a week changes:

- travel.
- busy work periods.
- race dates.
- appointments that affect training.

This feature must be opt-in, transparent, and easy to disconnect.

## User Stories

- As a user, I can import selected calendar events as Hermes busy blocks or race/events.
- As a user, I can review events before importing.
- As a user, I can choose which calendar sources to use.
- As a user, I can sync selected Hermes race/events to my device calendar.
- As a user, I can disable calendar access later.
- As a user, I can keep Hermes usable without granting calendar permission.

## Product Modes

### Import Only

Hermes reads selected calendar events and lets the user create:

- Busy blocks.
- Race/events.
- Training context tags such as travel or busy week.

Recommendation: first implementation should start here.

### Export Only

Hermes writes selected race/events or reminders to the calendar.

Useful for:

- race date visibility.
- checklist deadlines if reminders exist later.

### Two-Way Sync

Hermes imports and exports with ongoing matching.

This is higher-risk because conflict handling and deletion semantics become complex.

Recommendation: do not start with full two-way sync.

## First Version Scope

- Opt-in permission flow.
- Calendar source picker if available.
- Import preview list.
- User chooses destination type per imported item:
  - Busy.
  - Race/event.
  - Ignore.
- Manual one-time import.
- No background sync in v1.

## Out Of Scope For V1

- Continuous background sync.
- Two-way conflict resolution.
- Automatic training plan changes.
- Server calendar providers.
- Importing private event details without explicit user review.
- Calendar event body/attendee import.

## Privacy Rules

- Calendar permission is optional.
- Show what will be imported before writing Hermes data.
- Do not log calendar event titles/descriptions in Activity.
- Prefer importing only date/time/category-like metadata unless user explicitly confirms title use.
- Provide "Disconnect calendar" and "Clear imported calendar links" actions.

## Data Direction

Imported items should become normal Hermes rows after confirmation.

Optional metadata:

- source calendar id.
- source event id.
- imported at.

Store external IDs only if needed for future duplicate detection. Do not require them for v1 if import is one-time/manual.

## Backup Compatibility

If external calendar metadata is persisted, backup schema review is required.

Recommendation:

- Do not export external calendar IDs in user backup unless they are needed to explain imported items.
- Export only the resulting Hermes item data.

## Activity Logging

Log user-created Hermes data through existing action types.

Recommended new actions only if needed:

- `IMPORT_CALENDAR_ITEMS`
- `EXPORT_EVENT_TO_CALENDAR`
- `DISCONNECT_CALENDAR`

Do not log imported event titles or descriptions.

## Testing

Recommended tests:

- Permission denied keeps app usable.
- Import preview maps selected event to busy block.
- Import preview maps selected event to race/event.
- Ignored event creates no Hermes data.
- Activity logs do not include calendar free text.
- Disconnect clears stored calendar settings/links.

## Acceptance Criteria

- Calendar access is opt-in.
- User reviews items before import.
- Hermes remains usable without calendar permission.
- No full two-way sync is implemented in v1.
- Private calendar text is not logged.
