# Analytics And Crashlytics Spec

Target release: separate infrastructure/privacy decision; not part of Progress v1.

## Goal

Improve app reliability and product decision-making while respecting Hermes' offline-first, low-noise identity.

Crash reporting and analytics must be treated differently:

- Crashlytics: reliability and crash diagnosis.
- Analytics: coarse product usage signals only.

## Product Principles

- Do not collect workout titles, descriptions, notes or other free text.
- Do not collect category names.
- Do not collect precise training content unless explicitly justified.
- Prefer aggregate/coarse events.
- Keep user trust more important than metric coverage.
- Update privacy-facing copy if telemetry ships.

## Open Decision

Before implementation, decide:

- Is Firebase acceptable for this personal/offline-first app?
- Should Crashlytics ship without Analytics?
- Is opt-in required for the app's intended distribution/privacy posture?
- What should happen in debug builds?

Do not implement until these decisions are made.

## Crashlytics Scope

Potential first scope:

- Crash reporting in release builds.
- Non-fatal logging only for important failures:
  - backup import/export failures.
  - migration failures.
  - unexpected decode states.

Avoid logging user-entered content.

## Analytics Scope

Potential event categories:

- screen opened:
  - week.
  - trophies.
  - race events.
  - settings.
- feature actions:
  - create workout.
  - create race event.
  - export backup result.
  - import backup result.
  - share trophy intent.
  - future share report intent.
- settings toggles:
  - theme changed.
  - language changed.
  - week start changed.

Do not log:

- workout title.
- workout description.
- notes.
- category name.
- exact report contents.

## Event Naming Direction

Use stable product-level names:

- `screen_view_week`.
- `race_event_create`.
- `backup_export_result`.
- `backup_import_result`.
- `trophy_share_intent`.

Metadata should be coarse:

- success/failure.
- schema version.
- event type.
- count bucket, if needed.

## Architecture Direction

Introduce an abstraction before binding to Firebase:

- `TelemetryLogger`.
- no-op debug implementation.
- Firebase release implementation if approved.

Keep telemetry out of UI composables where possible. ViewModels/use cases should call domain-friendly logging methods, not Firebase directly.

## Testing

Recommended tests:

- Telemetry logger receives expected coarse events.
- Sensitive fields are not passed into telemetry metadata.
- Debug/no-op implementation does not crash.
- Backup telemetry records result without backup content.

## Acceptance Criteria

- Privacy/product decision is documented before implementation.
- Crash reporting can be enabled without broad analytics if desired.
- No free text is logged.
- Telemetry is behind an app abstraction.
- Progress v1 does not depend on telemetry.
