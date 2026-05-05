# Events Release Scope

Status: release-prep archive for `v1.9.0`.

This document records the Events release boundary. It is not active future scope.

## Goal

Ship the existing Race & Events work as the only user-facing feature in `v1.9.0`.

This release should not add Progress, reminders, reports, analytics, Crashlytics, templates, notes, perceived effort, navigation redesign or other roadmap features. Those remain post-release planning candidates.

## Release Principle

The release should feel like a focused planning upgrade:

- Users can add upcoming events/races.
- Events appear in the weekly planner.
- The dedicated Events screen gives a countdown-style view.
- Activity and backup understand the new event type.

Do not expand the release scope just because adjacent ideas are already documented.

## Included Scope

The `v1.9.0` release includes the current Race & Events feature surface:

- `RACE_EVENT` as a weekly item type.
- Add/edit/delete race events from the weekly planner flow.
- Add/edit/delete race events from the Events screen.
- Required event date selection.
- Category integration.
- Weekly planner visual distinction for race events.
- Events screen with upcoming/past grouping and countdown labels.
- Race-event Activity logging and filtering support.
- Backup schema support for race-event rows.
- Existing tests for ViewModel, backup and weekly behavior.
- Release notes/changelog/README updates for the shipped Events feature.

## Explicitly Out Of Scope

Do not add these to the Events release:

- Progress screen.
- Browse/navigation redesign.
- Weekly report sharing.
- Notes or perceived effort.
- Reminders or notifications.
- Analytics or Crashlytics.
- New trophy families.
- Structured race distance, target time or location fields.
- Linking workouts to a race event.
- Race-prep recommendations.
- New persisted tables beyond the already planned backup/schema support.

## Pre-Release Checklist

- Confirm the Events tab appears in the intended temporary shell.
- Confirm Activity remains reachable through the current app shell.
- Confirm race events can be created for future weeks without navigating week by week.
- Confirm race events render distinctly from workouts in the weekly planner.
- Confirm past events and upcoming events sort as expected.
- Confirm backup export/import preserves race events under the supported schema.
- Confirm localized strings exist for all Events copy.
- Confirm release notes mention Events and do not advertise post-release roadmap items.
- Confirm README moves race/events from future/planned language into shipped feature language.

## Post-Release Direction

After the Events release, the recommended next product exploration is Progress.

Progress should be planned as a separate release, using the existing `docs/specs/progress-screen-spec.md` as the starting point. Its first version should use existing data and avoid new write-side state.
