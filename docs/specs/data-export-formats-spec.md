# Data Export Formats Spec

Target release: after Backup and Weekly Report are stable.

## Goal

Provide user-friendly export formats beyond JSON backup, such as CSV for workouts, Personal Records, events, and reports.

This should support personal analysis and portability without replacing backup.

## Product Fit

JSON backup is for restoring Hermes. CSV/export formats are for user inspection and external use.

## User Stories

- As a user, I can export workouts as CSV.
- As a user, I can export Personal Records as CSV.
- As a user, I can export race/events as CSV.
- As a user, I can choose a date range.
- As a user, I can share or save the exported file.

## First Version Scope

- CSV export for workouts and race/events.
- Date range picker.
- Save/share through Android document flow.

## Out Of Scope For V1

- CSV import.
- Spreadsheet formatting.
- Cloud upload.
- Automated scheduled exports.

## Privacy Direction

Warn when export includes notes or custom text.

Default exclude free-text notes unless user opts in.

## Data Direction

Generated from repositories. No new persistence except optional last export timestamp.

## Activity Logging

Log export intent and format, not file contents.

## Acceptance Criteria

- User can export selected local data as CSV.
- Backup JSON remains separate from user-facing exports.
- User controls private text inclusion.
