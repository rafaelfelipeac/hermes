# Privacy Controls Spec

Target release: before Coach Pack Export or any sharing-heavy feature ships.

## Goal

Give users clear control over what private training data appears in reports, exports, Activity metadata, and shared text.

The feature should make Hermes safer as it stores more notes, context tags, PRs, race retrospectives, and references.

## Product Fit

Hermes is offline-first and personal. As it becomes a training memory system, privacy defaults should be explicit:

- Notes are private by default.
- Sharing should be previewed.
- Logs should avoid free text.
- User decides what leaves the device.

## User Stories

- As a user, I can choose default share sections.
- As a user, I can exclude notes from reports.
- As a user, I can exclude Personal Records from coach exports.
- As a user, I can preview before sharing.
- As a user, I can reset privacy defaults.

## First Version Scope

- Settings screen for share/export defaults.
- Toggles:
  - include notes.
  - include context tags.
  - include Personal Records.
  - include checklist progress.
  - include gear.
- Preview before share remains required for share flows.

## Out Of Scope For V1

- Cloud privacy policy UI.
- Per-contact sharing rules.
- Encryption settings.
- App lock, covered by `app-privacy-lock-spec.md`.

## Data Direction

Persist settings in DataStore.

No backup schema change required unless settings backups should preserve these preferences.

## Activity Logging

Changing privacy controls is state-changing and should be logged without including private values beyond setting keys.

## Acceptance Criteria

- Share/export defaults are user-controlled.
- Free-text notes are excluded by default.
- Share flows still show a preview before leaving the app.
