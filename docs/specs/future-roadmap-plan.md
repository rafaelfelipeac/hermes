# Future Roadmap Plan

Purpose: keep future Hermes ideas in one planning map after the Events release prep. This is not a commitment to build everything; it is a prioritization tool for deciding what deserves a dedicated implementation task.

## Product Direction

Hermes should remain a calm, offline-first weekly planning app.

Good future features should usually do at least one of these:

- Make the weekly plan easier to understand.
- Help the user reflect without feeling scored.
- Make the app safer, calmer or more reliable.
- Support sharing context with a coach without turning Hermes into a social network.
- Improve trust through backup, accessibility, crash reporting and release quality.

Features that require structured training assumptions should stay later until the product has enough evidence to support them gently.

## Prioritization Rules

Use this order when deciding what to build next:

1. Existing-data value: progress summaries and weekly reports.
2. Foundation and trust: accessibility, release tooling, backup safety and crash reliability.
3. Planning depth: notes, perceived effort, templates and reusable routines.
4. Timely nudges: reminders and notifications, only after the plan/report model is clear.
5. Rich polish: animations, micro-interactions, yearly reflection and advanced recognition.

Avoid adding new persisted schema unless the feature cannot be delivered from existing weekly items, user actions, categories, trophies, race events or settings.

## Recommended Roadmap

### Phase 1: Progress v1

Spec: `docs/specs/progress-screen-spec.md`.

Priority: next product feature.

Goal: give users a quiet summary of consistency, categories, trophy progress, recent activity and upcoming event context.

Why first:

- It uses existing data.
- It creates visible value from Activity and Trophies without replacing them.
- It gives the later navigation redesign a real `Progress` destination.
- It provides summary language that Weekly Report can reuse.

First version:

- Current week summary.
- Last 8 weeks completion trend.
- Category distribution for recent completed workouts.
- Trophy highlight or nearest trophy progress.
- Recent activity preview.
- Optional nearest race/event card.

Out of scope:

- Training readiness scores.
- Performance predictions.
- Automatic recommendations.
- New persisted progress tables.

### Phase 2: Weekly Report And Sharing

Spec: `docs/specs/weekly-report-spec.md`.

Priority: high after Progress.

Goal: create a coach-friendly weekly report generated from existing weekly data.

First version:

- Selected week.
- Planned, completed, rest, busy, sick and race-event counts.
- Category breakdown.
- Day-by-day item list.
- Local Android share sheet text.

Why after Progress: Progress should settle the summary model first, then the report can reuse that vocabulary and aggregation logic.

### Phase 3: Navigation Redesign And Browse Hub

Spec: `docs/specs/navigation-redesign-spec.md`.

Priority: high, but only after Progress has real value.

Target shell:

- `Week`.
- `Progress`.
- `Events`.
- `Browse`.

Browse should own:

- Activity.
- Categories.
- Trophies.
- Backup and Import.
- Settings.

Why after Progress: replacing or moving Activity only works if Progress is a useful summary destination, not a placeholder.

Navigation is public app behavior, so implementation needs explicit review before coding.

### Phase 4: Notes And Perceived Effort

Spec: `docs/specs/notes-effort-spec.md`.

Priority: medium-high after reporting direction is clearer.

Goal: capture post-session reflection without turning Hermes into a heavy training diary.

Likely schema impact:

- Nullable fields on the weekly item, or
- Separate feedback table.

Because this is persisted user data, implementation must stop for schema, migration and backup review before coding.

### Phase 5: Reminders And Notifications

Spec: `docs/specs/reminders-notifications-spec.md`.

Priority: medium.

Goal: help users remember the plan without nagging.

Start narrow if this becomes a priority:

- Weekly planning reminder in settings.

Defer per-workout or race-event reminders until the app has a clearer scheduling model and notification permission UX.

### Phase 6: Templates And Reusable Routines

Spec needed if this moves forward.

Priority: medium-later.

Goal: reduce repeated planning effort.

Possible first scope:

- Save a workout as a template.
- Create from template.
- Save current week as a reusable routine.

Likely persisted model and backup schema work, so this should not be bundled with Progress.

### Phase 7: Micro-Interactions

Spec: `docs/specs/micro-interactions-spec.md`.

Priority: opportunistic.

Only build these when tied to a specific UX problem:

- Drop target feedback.
- Completion transitions.
- Trophy celebration refinement.
- Empty-state transitions.
- Report/share confirmation.

## Continuous Quality Tracks

### Accessibility

Spec: `docs/specs/accessibility-audit-spec.md`.

Run as incremental fixes, especially around:

- Week planner rows.
- Events cards.
- Trophy cards.
- Add/edit dialogs.
- Bottom navigation labels.
- Dynamic text and TalkBack traversal.

### Build Tooling

Spec: `docs/specs/build-tooling-spec.md`.

Useful near-term work:

- `docs/release-checklist.md`.
- Short `docs/testing.md`.
- Clear CI job names/report uploads.
- Common local verification recipes.

### Analytics And Crashlytics

Spec: `docs/specs/analytics-crashlytics-spec.md`.

Treat as a separate privacy/product decision.

Crash reporting may be useful for reliability. Analytics should stay coarse and must not capture titles, descriptions, notes, category names or user free text.

## Immediate Next Steps

1. Refresh `progress-screen-spec.md` into an implementation-ready v1.
2. Decide whether Progress ships inside the existing shell first or waits for the Browse redesign.
3. Add fake-backed tests for Progress aggregation before UI composition.
4. Keep accessibility and release-tooling improvements as separate small tasks.
5. Revisit Weekly Report after Progress has stable summary state.
