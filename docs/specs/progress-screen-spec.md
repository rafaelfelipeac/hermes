# Progress Screen Spec

Target release: next product feature candidate after `v1.9.0` Events release prep.

## Goal

Create a real `Progress` destination that summarizes the user's training history instead of only listing raw activity logs.

First implementation should be read-only and should use existing data only.

## Real Data From Backup

A user backup snapshot gives a useful starting shape for the first version:

- 14 weeks of weekly history.
- 113 weekly items total, with 91 completed and 22 pending.
- Item mix: 96 workouts, 12 rest days, 4 busy blocks and 1 sick block.
- Weekly volume is fairly stable, usually 7 to 9 items per week.
- Completion is usually in the 75% to 89% range, with one partial week dropping to 38%.
- Categories are all system categories in this backup snapshot, so the first Progress version can work with the existing category model instead of needing category migration logic.
- This backup predates the Events release, so there are no race-event rows to fold into the baseline here.

What that means for implementation:

- A first Progress screen can focus on weekly completion, category distribution and recent activity without needing new persistence.
- Empty or partial event-based sections should be optional, not required for v1.
- The screen should read as a summary of the existing weekly plan, not as a scorecard.

Progress should bridge the gap between:

- Activity: detailed timeline of what happened.
- Trophies: collectible milestones derived from history.

The screen should answer: "how am I doing over time?".

## Current Context

Existing data sources already support a first Progress version:

- Activity uses `UserActionRepository` to show raw logs.
- Trophies use `UserActionRepository` plus `TrophyEngine` to derive milestones.
- Weekly planner already derives current-week completion summary from weekly items.

There is currently no screen that summarizes this data into trends, charts or high-level insights.

## Product Scope

### User Stories

- As a user, I can see a quick summary of recent training consistency.
- As a user, I can understand recent activity without scanning the full Activity timeline.
- As a user, I can see trophy progress/highlights without opening the full trophy shelf.
- As a user, I can spot whether categories are balanced over recent weeks.
- As a user, I can use upcoming race/event context later to understand preparation time.

## Relationship To Existing Screens

### Activity

Activity remains the source-of-truth timeline.

Progress should show a small recent activity preview, not duplicate the full Activity screen.

Activity answers:

- What exactly happened?
- When did it happen?
- What changed?

Progress answers:

- What pattern is emerging?
- Am I consistent?
- Which areas/categories are getting attention?

### Trophies

Trophies remain the collectible shelf.

Progress can show trophy highlights:

- Recently unlocked trophy.
- Closest next trophy.
- Overall unlocked count.

Progress should link to the full Trophies screen instead of replacing it.

## Initial Content

Recommended first sections:

- At a Glance summary cards.
- This week snapshot.
- Weekly completion trend.
- Category distribution.
- Trophy highlight.
- Recent activity preview.
- Optional upcoming event preview.

The first version should feel like a calm dashboard, not a report dump. It can include several useful signals, but they should be chosen for clarity and scanability.

### Summary Cards

Small top cards should summarize recent progress.

Candidate metrics:

- Workouts completed this week.
- Completed weeks in the last 4 or 8 weeks.
- Current consistency streak if derivable from existing trophy/history logic.
- Next upcoming race/event.
- Total planned items this week.
- Completion percentage for the current week.
- Most-used category in the recent window.

Recommended card set for v1:

- `This week` completion.
- `Consistency` or streak if derivable from existing history.
- `Top category` from recent weeks.
- `Upcoming` race/event when available.

Do not show metrics that require new tracking unless the implementation already has reliable data.

If a metric is unavailable, the card should gracefully degrade instead of showing fake numbers.

### Weekly Completion Trend

A compact chart should show completion over recent weeks.

Preferred first chart:

- Last 8 weeks.
- One bar per week.
- Bar height or fill based on completed workouts / planned workouts.
- Empty weeks should be visually distinct from planned-but-incomplete weeks.
- Use the current week as the visual anchor.

For this backup shape, the trend should be expected to show a mostly steady band of 7 to 9 planned items per week with completion usually around three-quarters or better. That makes a simple bar chart a better fit than a dense analytic graph.

This can be implemented with Compose primitives first. Avoid adding a chart dependency unless native Compose drawing becomes too costly.

### Category Distribution

Show how recent completed or planned workouts are distributed by category.

Preferred first version:

- Last 4 or 8 weeks.
- Horizontal stacked bar or small ranked list.
- Use category color and name.
- Keep Uncategorized last.
- Show either count or share, but not both if that makes the card noisy.

Race/events should not count as completed workouts in this chart, but future versions may show race/event category presence separately.

For this backup snapshot, the dominant categories are Run, Cycling and Strength, with a smaller uncategorized bucket mostly used by rest/busy/sick rows. That suggests the first category view can be a short ranked list or stacked bar with three main colors and one neutral fallback.

### Trophy Highlight

Show one or two trophy signals:

- Recently unlocked trophy, if any.
- Closest locked trophy, if no recent unlock.
- Small unlocked-count summary if it adds a real signal.

Use existing trophy presentation models where possible.

Do not duplicate the full trophy grid.

If the current backup has no trophy-derived highlight data available, show the section only when the app can derive a real signal from existing history.

### Recent Activity Preview

Show the latest few Activity entries as a preview.

Rules:

- Keep it short, for example 3 to 5 entries.
- Include a "View all" action into Activity if Activity is no longer top-level.
- Do not bring all Activity filters into Progress.
- Prefer a compact list or timeline that is easier to scan than the full Activity screen.

This backup has a long activity history, so Progress can safely preview just the most recent changes instead of trying to summarize the entire timeline.

## Race & Events Integration

Race/events should be optional in the first Progress version.

Progress can include:

- Nearest upcoming race/event.
- Days remaining.
- Number of planned workouts before that event.
- Gap between next two events.
- A compact event-context card once at least one race/event exists.

Do not attempt race-readiness scoring in the first version. That would require clearer training-plan assumptions.

## Data Direction

Progress should have its own presentation state and ViewModel.

Possible inputs:

- `UserActionRepository` for activity history and trophy derivation.
- `WeeklyTrainingRepository` for recent weekly items when trend/category metrics need planned and completed item state.
- Trophy derivation helpers for unlocked/nearest trophy signals.
- Race/event query once `EventType.RACE_EVENT` exists.

Avoid placing Progress state classes inside a ViewModel file. Follow the existing rule: put state/data classes in dedicated files.

## UI Direction

Progress should feel like a dashboard, not another list.

Visual direction:

- Borrow the structure of Garmin and Strava progress screens, not their domain-specific metrics.
- Start with a small set of summary cards, then move into one or two charts and a compact history section.
- Make the first screen useful at a glance, with deeper detail available by scrolling.
- Strong summary cards at the top.
- Compact charts with category color accents.
- Calm, readable, non-competitive tone.
- Avoid dense analytics language.
- If the user has no data for a section, hide it or collapse it instead of showing placeholder filler.

Useful layout cues from the examples:

- Garmin-style "At a Glance" cards map well to Hermes summary cards for completion, consistency, category mix and upcoming event context.
- Strava-style month recap and sport breakdowns map well to weekly trend, category distribution and activity history sections.
- A short "Top category" or "This week" block can sit above the longer history sections, just like those apps keep their monthly summary ahead of the details.

Nice extra ideas for v1, if they fit the data cleanly:

- A small current-week total card alongside the completion card.
- A month-to-date or last-30-days activity total, if the existing data supports it.
- A category share card that highlights the most-used activity type without pretending to be a performance metric.
- A simple streak card derived from actual weekly consistency, not a new notion of fitness scoring.

Charts should be simple:

- Bars.
- Stacked bars.
- Small trend cards.

Avoid line charts unless the data actually benefits from precise time-series comparison.

## Navigation Direction

If Progress proves useful, it becomes the preferred middle bottom-nav item:

- Week.
- Progress.
- Race Events.
- Browse.

Activity can move into Browse or become a detail route from Progress.

Because navigation is public app behavior, implementation must explicitly review whether Progress ships in the existing shell first or together with the Browse redesign.

Trophies can remain in Browse, with Progress linking to trophy highlights.

If the first version lands well, Weekly Report is the natural next follow-on because it can reuse the same weekly summary, trend and category logic.

## Activity Logging

Progress is read-only in the first version.

No new Activity logs are required for viewing Progress or opening charts.

If Progress later adds state-changing controls, those controls need normal `UserActionLogger` coverage.

## Testing

Recommended tests:

- Progress state maps recent weeks into completion trend buckets.
- Category distribution ignores non-workout events and handles Uncategorized last.
- Trophy highlight chooses recent unlock before nearest locked trophy.
- Recent activity preview limits entries and links to Activity.
- Race/event summary is absent when no race events exist.
- Compose tests cover empty, partial and populated dashboard states.

## Acceptance Criteria

- Progress is a real summary screen, not just Activity renamed.
- Activity timeline remains accessible.
- Trophies remain accessible.
- Progress shows at least one summarized trend or chart.
- Progress shows at least one trophy/progress highlight.
- Progress uses existing persisted data and does not introduce new write-side state in the first release.
