# Weekly Report Spec

Target release: after Progress v1.

## Goal

Create a coach-friendly weekly report that summarizes one selected week in a calm, shareable format.

The report should help the user communicate the plan and what happened without turning Hermes into a social feed or performance platform.

## Product Fit

Hermes already thinks in weeks. A weekly report is a natural extension of that model:

- It reuses the weekly planner as the source of truth.
- It complements Progress without requiring a full analytics system.
- It makes notes and perceived effort more valuable once those exist.
- It supports coach communication while keeping the app offline-first.

## User Stories

- As a user, I can open a report for the current or selected week.
- As a user, I can see planned, completed, rest, busy, sick and race-event counts.
- As a user, I can see which categories got attention that week.
- As a user, I can share a readable text report through Android sharing.
- As a user, I can review the report before choosing where to send it.
- As a user, I do not need an account or server to generate/share the report.

## First Version Scope

Recommended v1:

- Selected week header.
- Summary counts:
  - planned workouts.
  - completed workouts.
  - remaining planned workouts.
  - rest days.
  - busy blocks.
  - sick blocks.
  - race events.
- Category breakdown for workout and race-event rows.
- Simple item list grouped by day.
- Share action that sends text to Android share sheet.
- Empty-state copy when the selected week has no reportable items.

Use existing weekly item data first. Do not add new persisted report tables.

## Optional Later Additions

- Include post-session notes and perceived effort once those exist.
- Export as image or PDF.
- Coach-specific formatting presets.
- Compare report with previous week.
- Include Progress summary cards.
- Include upcoming race context.

## Out Of Scope For V1

- Server upload.
- Public profile or social sharing.
- Automatic coach sync.
- PDF/image generation unless text sharing proves insufficient.
- Performance scoring or readiness claims.
- New persisted report history.

## Data Direction

Primary source:

- `WeeklyTrainingRepository`.
- Category data from `CategoryRepository`.

Possible later inputs:

- Post-session note/effort model.
- Progress summary model.
- Race-event queries.

Report state should live in dedicated presentation state files, not inside a ViewModel.

## UI Direction

The report view should feel like a readable summary, not a dashboard wall.

Recommended sections:

- Week title.
- Summary strip or compact cards.
- Category breakdown.
- Day-by-day report.
- Share action.

The share preview should use plain text that reads well in messaging apps.

## Share Text Direction

The first text format should be intentionally simple:

```text
Hermes weekly report
Apr 27 - May 3

Summary
Completed: 3/5 workouts
Rest: 1
Busy: 1
Race events: 1

By category
Run: 3
Strength: 2

Week
Mon: Easy run - completed
Tue: Strength - planned
Sun: 10K race
```

Final copy must be localized and generated through string resources/string providers.

## Activity Logging

Sharing is user intent and should be logged once the Android chooser is opened.

Do not log external delivery success because Android does not provide a reliable signal that the user actually sent the report.

Recommended action:

- `SHARE_WEEKLY_REPORT`.

Recommended entity type:

- `WEEKLY_REPORT` or existing week/app entity if a new entity type is not worth it.

Metadata should avoid free text:

- week start date.
- planned count.
- completed count.
- report format.

## Backup Compatibility

No backup schema change for v1 if reports are generated from existing data and not persisted.

If report settings or saved report history are added later, follow `docs/backup-compatibility-policy.md`.

## Localization

All visible report labels and share text fragments must be localized.

Watch for:

- plural forms.
- date formatting.
- right-to-left layout.
- category names inserted into localized templates.

## Testing

Recommended tests:

- Report state maps weekly items into counts.
- Category breakdown ignores non-category rows correctly.
- Race events appear in report without being counted as completed workouts.
- Empty week produces an empty report state.
- Share text is stable for representative mixed weeks.
- Activity log is emitted when share is launched.
- Compose test covers populated and empty report surfaces.

## Acceptance Criteria

- User can open a report for the selected week.
- Report summarizes the week without requiring new persisted data.
- Report can be shared through Android share sheet.
- Share action logs intent without claiming external success.
- Report copy is localized.
- Race events appear as events, not workouts.
