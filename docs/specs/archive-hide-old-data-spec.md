# Archive And Hide Old Data Spec

Target release: later, after Events, Personal Records, Gear, and Templates create more long-lived data.

## Goal

Let users hide or archive old items without deleting their history.

The feature should keep Hermes calm for long-term users.

## Product Fit

As the app accumulates races, gear, records, templates and notes, not everything should stay visible forever. Archive gives users control without data loss.

## User Stories

- As a user, I can archive old race/events.
- As a user, I can archive old gear.
- As a user, I can archive templates or library items I no longer use.
- As a user, I can view archived items later.
- As a user, I can restore archived items.

## First Version Scope

- Archive support for race/events and gear or templates, depending on which exists first.
- Archived items hidden from default lists.
- Archive filter or separate archived section.
- Restore action.

## Out Of Scope For V1

- Auto-archive rules.
- Permanent deletion redesign.
- Storage cleanup.
- Cloud retention settings.

## Data Direction

Prefer `archivedAt` nullable timestamp over boolean `isArchived`.

Backup should preserve archive state.

## Activity Logging

Log archive/restore. Avoid logging free-text names where sensitive.

## Acceptance Criteria

- Archive hides old data from default surfaces.
- Archived data remains restorable.
- Backup/import preserves archive state.
