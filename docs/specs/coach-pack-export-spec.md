# Coach Pack Export Spec

Target release: after Weekly Report, Training Context, Personal Records, and Race Retrospective have enough data to export.

## Goal

Let users export a clean coach-friendly bundle that includes selected training context, weekly report data, race prep status, notes, and Personal Records.

The feature should help communication without requiring accounts, cloud sync, or a coach portal.

## Product Fit

Hermes should stay offline-first and user-controlled. A Coach Pack is a local export/share action:

- User chooses what to include.
- Hermes formats it clearly.
- Android share sheet sends it.

## User Stories

- As a user, I can select a date range.
- As a user, I can choose what sections to include.
- As a user, I can preview the export before sharing.
- As a user, I can exclude private notes.
- As a user, I can share via Android share sheet.

## First Version Scope

- Date range selection.
- Sections:
  - weekly summaries.
  - selected context tags.
  - optional notes.
  - race events.
  - checklist progress.
  - personal records.
- Plain text export first.

## Out Of Scope For V1

- Coach account.
- Automatic email delivery.
- PDF/image generation.
- Cloud sync.
- External calendar or training-platform export.

## Privacy Defaults

Default include:

- summary counts.
- categories.
- race/event dates.
- checklist progress.
- context tags.

Default exclude:

- free-text notes.
- custom checklist titles.
- sensitive custom text.

## Data Direction

Generated from existing repositories. No persisted export history in v1.

## Activity Logging

Log the intent to open the share sheet. Do not log the exported text.

## Acceptance Criteria

- User can preview and share a local coach pack.
- User controls private note inclusion.
- No account or network is required.
