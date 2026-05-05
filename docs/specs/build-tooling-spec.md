# Build Tooling Spec

Target release: continuous; should be done in small infrastructure-only changes.

## Goal

Make local and CI verification faster, clearer and less error-prone.

This spec is about developer/release quality, not app behavior.

## Current Tooling Context

Hermes already uses:

- Gradle.
- Detekt.
- Ktlint.
- Unit tests.
- Android instrumentation tests.
- GitHub Actions.
- Release notes and changelog conventions.

## Product Rationale

As the feature set grows, tooling should reduce release risk:

- Events adds schema/activity/UI surface.
- Progress will add derived state and charts.
- Future notes/reminders may add migrations and platform APIs.

Fast, reliable checks matter more as the app gets broader.

## Candidate Work

### Local Verification Commands

Document and standardize common commands:

- compile debug Kotlin.
- unit tests.
- detekt.
- ktlint.
- lint.
- targeted instrumentation tests.

Add a release-check recipe that runs the minimum expected pre-release suite.

### CI Clarity

Improve CI readability:

- clear job names.
- separate lint/static analysis/test failures.
- upload useful reports when available.
- keep release workflow distinct from PR verification.

### Test Ergonomics

Reduce friction for:

- running a single ViewModel test.
- running a single Compose test.
- running backup codec tests.
- running migration tests if more schemas are added.

### Release Checklist

Create a release checklist covering:

- version bump.
- changelog.
- README shipped/future feature wording.
- release notes registry.
- backup policy update if schema changed.
- localized strings.
- screenshots if UI changed.

## Out Of Scope

- Replacing Gradle.
- Adding new static analysis tools without a clear payoff.
- Mass formatting.
- Rewriting CI from scratch.

## Documentation Direction

Possible destinations:

- `docs/release-checklist.md`.
- `docs/testing.md`.
- `README.md` developer section, if public-facing.

Keep command docs short and current.

## Acceptance Criteria

- Common verification paths are documented.
- Release checklist captures schema/localization/README/release-notes duties.
- CI failure categories are easy to understand.
- Changes do not alter app behavior.
