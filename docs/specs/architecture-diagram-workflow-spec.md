# Architecture Diagram Workflow Spec

## Goal

Create a lightweight system-design workflow for Hermes so the app has a current architecture diagram that can be reviewed and updated as releases change the product.

The diagram should help answer:

- What are the current app boundaries?
- Which features own which screens and ViewModels?
- Where do state changes flow?
- Which parts of the app are persisted in Room or DataStore?
- Which cross-cutting pipelines depend on user actions, backup, trophies, reports or platform integrations?

## Current Context

Hermes already has enough feature depth that architecture can become hard to keep in one person's head:

- Weekly planning, Events, Activity, Trophies, Backup and Settings already exist.
- Progress, Browse, reports, notes, calendar sync, exports and privacy controls are planned.
- Activity logging is a cross-feature contract, not an isolated screen detail.
- Backup compatibility is schema-versioned and must stay visible when persisted models change.

The diagram should therefore be release-oriented, not implementation-oriented.

## Source Files

- Editable source: `docs/architecture/hermes-system-design.excalidraw`
- Workflow notes: `docs/architecture/README.md`
- Optional release snapshots: `docs/architecture/releases/vX.Y.Z-system-design.png`

The `.excalidraw` file is the source of truth. Exported images are review artifacts only.

## Diagram Boundaries

Include:

- App shell and navigation destinations.
- Major feature screens.
- ViewModel/state boundaries.
- Domain/use-case boundaries.
- Repository boundaries.
- Room and DataStore persistence.
- Backup/import.
- User-action logging and consumers such as Activity, trophies, Progress and reports.
- External Android/platform integrations once they are real.

Exclude:

- Individual composables.
- Individual entity fields unless a release specifically changes a schema boundary.
- DAO method lists.
- Test fakes and fixtures.
- Future features that are only ideas and not committed to a release.

## Update Triggers

Update the diagram when a release includes:

- Top-level navigation changes.
- New or moved feature destinations.
- New persisted data or Room schema changes.
- Backup schema changes.
- New cross-feature pipelines.
- New platform integrations such as notifications, calendar, share/export, app lock or crash reporting.

Skip updates for:

- Copy changes.
- Small UI polish.
- Isolated bug fixes.
- Internal refactors that do not change ownership boundaries.

## Release Process

1. During release prep, compare the release diff against the update triggers.
2. If any trigger applies, update `hermes-system-design.excalidraw`.
3. Export a PNG only when needed for visual review, release notes or a historical snapshot.
4. Store snapshots under `docs/architecture/releases/` using the release version in the filename.
5. Mention the diagram update in the PR or release-prep notes.

## Acceptance Criteria

- The project has a committed Excalidraw source diagram.
- The diagram uses stable layer colors documented in `docs/architecture/README.md`.
- The workflow explains when a diagram update is required and when it is unnecessary.
- Future release work can update the diagram without reverse-engineering the convention.
- The diagram remains high-level enough to stay maintainable.
