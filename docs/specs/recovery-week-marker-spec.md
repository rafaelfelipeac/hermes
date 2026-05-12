# Recovery Week Marker Spec

Target release: after Training Context or Training Blocks exists.

## Goal

Let users mark a week as recovery/deload/transition so the plan's intent is clear later.

The marker should support reflection without medical claims or training prescriptions.

## Product Fit

Some weeks are intentionally lighter. Without a marker, lower completion or lower volume can look like a failed week. A recovery marker preserves intent.

## User Stories

- As a user, I can mark a week as recovery.
- As a user, I can clear the marker later.
- As a user, I can see recovery weeks in Progress and reports.
- As a user, I can distinguish intentional recovery from disrupted weeks.

## First Version Scope

- Week-level marker:
  - Recovery.
  - Deload.
  - Transition.
- Optional note if Training Context exists.
- Show marker in weekly header/context row.
- Include marker in Weekly Report.

## Out Of Scope For V1

- Automatic deload detection.
- Medical recovery advice.
- Training recommendations.
- Performance scoring.

## Data Model Direction

Prefer Training Context or Training Block model if available.

If implemented standalone, use a week metadata table with a stable marker enum.

## Backup And Activity

Requires backup schema review if persisted.

Activity logging should cover set/clear marker.

## Acceptance Criteria

- User can label a week as intentionally lighter.
- Progress/report copy treats this as context, not failure.
- No medical or readiness claims are made.
