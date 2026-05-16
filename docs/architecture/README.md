# Hermes Architecture Diagrams

This folder owns living system-design diagrams for Hermes.

## Source Of Truth

- `hermes-system-design.excalidraw` is the editable source.
- Exported images are optional review artifacts.
- Release snapshots should live under `releases/` when a release changes the app shape enough to preserve a visual record.

## Diagram Scope

The main diagram should stay at system-design level:

- App shell and navigation destinations.
- Feature screens and their ViewModels.
- Domain/use-case boundaries.
- Repository and persistence boundaries.
- Room, DataStore and backup/export flows.
- User-action logging, Activity, trophies, progress and reporting consumers.
- Platform integrations such as Android share, notification, calendar or biometric APIs when they become real.

Avoid class-level detail, function names, individual composables and test-only helpers. Those belong in code, specs or review notes.

## Update Triggers

Update the diagram when a release changes one of these:

- Top-level navigation or destination ownership.
- Persisted data model, Room schema or backup contract.
- Major feature boundary, such as a new Browse destination.
- Cross-cutting pipelines, such as Activity logging, trophies, reporting, notifications or calendar sync.
- External integration boundary, such as Android share, calendar, crash reporting, app lock or export formats.

Small UI polish, copy changes and isolated bug fixes usually do not need a diagram update.

## Release Workflow

1. Open `hermes-system-design.excalidraw` in Excalidraw.
2. Update the current-state diagram only for behavior that is actually shipping.
3. Export a PNG if visual review or release notes need one.
4. If preserving a release snapshot, save it as `releases/vX.Y.Z-system-design.png`.
5. Link the diagram update from the release prep or PR notes.

## Visual Conventions

- Blue: UI shell and screens.
- Purple: ViewModels and state.
- Green: domain/use-case logic.
- Orange: repositories, Room, DataStore and backup.
- Gray: cross-cutting product pipelines.
- Red: external platform or system boundaries.

Keep arrows directional and label them only when the relationship would otherwise be ambiguous.
