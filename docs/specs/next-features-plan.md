# Next Features Plan

Status: superseded by `docs/specs/roadmap-convergence-audit-2026-05-12.md`. Keep this file as historical context only; do not use it as the current ordering.

Target baseline: `v1.9.0` release prep (`appVersionCode = 14`).

This plan is the active planning index after the Events work. Older specs may still contain useful implementation detail, but this file owns the current order and release posture.

## Current Product Baseline

Hermes now has:

- Weekly planning with drag/drop, copy last week, undo, slot mode and week-start settings.
- Workout, rest, busy, sick and race-event item types.
- Categories with colors, ordering and visibility.
- Events screen for race/event planning and countdown context.
- Activity timeline with filters.
- Trophy shelf and trophy progress.
- Backup/import with schema-versioned compatibility.
- Settings, release notes, demo data and debug seeders.

## Recommendation

The next product feature should be **Progress v1**.

If you want the one spec that explains that idea, read `docs/specs/progress-screen-spec.md`.

Why:

- It can be built from existing Room rows, user actions, categories, trophies and race events.
- It gives the app a useful reflection surface without adding performance scoring or coaching claims.
- It creates a better home for Activity previews and trophy highlights.
- It makes a later navigation redesign easier to justify because `Progress` can become a real top-level destination instead of a renamed Activity log.
- It sets up Weekly Report without requiring report-specific persistence.

The next non-product work can happen in parallel or as small chores: accessibility audit fixes, build/release checklist cleanup and CI clarity.

## Candidate Ranking

| Rank | Candidate | Recommendation | Reason |
| --- | --- | --- | --- |
| 1 | Progress v1 | Next product feature | High user value, uses existing data, low schema risk. |
| 2 | Accessibility audit | Continuous quality work | Important before broader UI growth, but not a feature release by itself. |
| 3 | Build tooling/release checklist | Continuous quality work | Reduces release risk, especially after schema/activity-heavy Events work. |
| 4 | Weekly report | After Progress | Strong fit, but benefits from Progress summary language and reusable aggregation. |
| 5 | Navigation redesign/Browse | After Progress has value | Public navigation change; should not happen until Progress earns the tab. |
| 6 | Notes/perceived effort | Later | Valuable, but likely needs persisted schema and backup policy changes. |
| 7 | Reminders/notifications | Later | Adds permissions, scheduling and platform complexity. |
| 8 | Analytics/Crashlytics | Separate decision | Useful for reliability/product learning only if privacy posture is accepted. |
| 9 | Micro-interactions | Opportunistic | Best tied to concrete UX friction, not a standalone feature. |

## Progress v1 Release Shape

Keep the first version read-only and existing-data only.

Minimum useful scope:

- Current week summary.
- Last 8 weeks completion trend.
- Recent category distribution for completed workouts.
- Trophy highlight or nearest trophy progress.
- Recent Activity preview.
- Optional nearest upcoming event card.

Explicitly avoid:

- Readiness scores.
- Training recommendations.
- Performance predictions.
- New persisted progress tables.
- Automatic coaching language.

## Spec Files

Active specs:

- [Progress screen](progress-screen-spec.md)
- [Future roadmap plan](future-roadmap-plan.md)
- [Weekly report](weekly-report-spec.md)
- [Navigation redesign](navigation-redesign-spec.md)
- [Notes and perceived effort](notes-effort-spec.md)
- [Training context tags and notes](training-context-tags-spec.md)
- [Race prep checklist](race-prep-checklist-spec.md)
- [Reminders and notifications](reminders-notifications-spec.md)
- [Accessibility audit](accessibility-audit-spec.md)
- [Build tooling](build-tooling-spec.md)
- [Analytics and Crashlytics](analytics-crashlytics-spec.md)
- [Micro-interactions](micro-interactions-spec.md)
- [Personal records and pace calculator](personal-records-pace-calculator-spec.md)
- [Season and training blocks](season-training-blocks-spec.md)
- [Reusable week archetypes](week-archetypes-spec.md)
- [Race retrospective](race-retrospective-spec.md)
- [Training library](training-library-spec.md)
- [Coach pack export](coach-pack-export-spec.md)
- [Gear and equipment log](gear-equipment-log-spec.md)
- [Soft planning assistant](soft-planning-assistant-spec.md)
- [Week change summary](week-change-summary-spec.md)
- [Year in review](year-in-review-spec.md)
- [Race countdown home card](race-countdown-home-card-spec.md)
- [Plan confidence](plan-confidence-spec.md)
- [Recovery week marker](recovery-week-marker-spec.md)
- [Onboarding and first week setup](onboarding-first-week-setup-spec.md)
- [Calendar import and sync](calendar-import-sync-spec.md)
- [Workout references](workout-references-spec.md)
- [Global search](global-search-spec.md)
- [Archive and hide old data](archive-hide-old-data-spec.md)
- [Privacy controls](privacy-controls-spec.md)
- [Today home view](today-home-view-spec.md)
- [Period comparison](period-comparison-spec.md)
- [Event types expansion](event-types-expansion-spec.md)
- [Custom units](custom-units-spec.md)
- [Data export formats](data-export-formats-spec.md)
- [App privacy lock](app-privacy-lock-spec.md)

Reference/archive specs:

- [Events release scope](events-release-scope.md) - release-prep record for `v1.9.0`, not active future scope.
- [Race events](race-events-spec.md) - implementation reference for the shipped Events feature and follow-up boundaries.

## Cross-Cutting Rules

- Navigation changes need explicit review before implementation because `AGENTS.md` requires stopping for public navigation changes.
- New persisted data, backup schema changes or public API changes require a stop-and-review before coding.
- New state-changing features need Activity logging, formatter support and localized Activity strings.
- New user-facing copy must be added to every localized `values-*` resource file.
- UI work must follow Hermes Compose guardrails: `Dimens`, localized strings, explicit `colorScheme`/`typography`/`shapes` imports and no hardcoded test tags.
