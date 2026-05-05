# Reminders And Notifications Spec

Target release: later; only after the product decides reminders are worth the added permission/scheduling complexity.

## Goal

Help users remember their plan without making Hermes nag.

Reminders should support planning and follow-through, not create pressure.

## Product Fit

Hermes is offline-first and weekly-focused. Reminders should be local device reminders tied to planning intent:

- remember today's workout.
- remember to plan the week.
- remember an upcoming race/event.

Do not build a remote notification system unless Hermes later adds sync/accounts.

## User Stories

- As a user, I can enable or disable reminders globally.
- As a user, I can set a reminder for a workout.
- As a user, I can set a weekly planning reminder.
- As a user, I can get a local reminder before an upcoming race/event.
- As a user, I can turn reminders off easily.

## First Version Options

Choose one narrow v1:

### Option A: Weekly Planning Reminder

- One weekly reminder setting.
- Day and time.
- Local notification opens Hermes to Week.

Lowest data complexity.

### Option B: Workout Reminders

- Per-workout reminder toggle/time.
- Notification opens Hermes to the relevant week/workout.

More useful, but requires item-level scheduling.

### Option C: Race Event Countdown Reminders

- Reminder X days before a race event.
- Notification opens Race Events.

Fits the Events surface, but should not be squeezed into release prep or Progress v1.

Recommendation: start with Option A unless user feedback specifically asks for workout/event reminders first.

## Android Constraints

Implementation must account for:

- notification runtime permission on modern Android.
- exact alarm restrictions if exact delivery is required.
- WorkManager vs AlarmManager trade-offs.
- device reboot rescheduling.
- timezone/date changes.
- notification channel setup.

Default to inexact/local scheduling unless exact reminders are essential.

## Settings Direction

Settings should include:

- master reminder toggle.
- reminder type controls.
- permission state explanation.
- disabled state when permission is denied.

Avoid burying reminders in unrelated training settings once Browse/navigation redesign exists.

## Notification Copy

Tone should be calm:

- "Plan your week"
- "Workout on your plan today"
- "Race event coming up"

Avoid:

- guilt.
- streak pressure.
- urgent language unless user explicitly asked for it.

All copy must be localized.

## Activity Logging

Log configuration changes:

- enable/disable reminders.
- change reminder schedule.
- set/clear item reminder.

Do not log every notification delivery by default.

Do not log notification content containing user-entered titles/descriptions.

## Data Model Direction

Weekly planning reminder can live in DataStore settings.

Per-item reminders likely require persisted fields or a reminder table:

- item id.
- reminder date/time.
- enabled flag.

Any persisted item reminder model needs backup schema review.

## Testing

Recommended tests:

- Settings state maps permission/reminder state correctly.
- Reminder changes emit Activity logs.
- Scheduler receives expected local schedule requests.
- Disabled reminders cancel scheduled work.
- Reboot/timezone reschedule logic has unit coverage where possible.
- Compose tests cover permission denied/granted states.

## Acceptance Criteria

- User can opt in and opt out.
- Reminders are local-only.
- Notification permission is handled explicitly.
- Reminder copy is localized and calm.
- Configuration changes are logged without user free text.
