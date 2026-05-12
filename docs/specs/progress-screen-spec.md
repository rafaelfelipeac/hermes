# Progress Screen Spec

Target release: after the base Race & Events work or alongside the navigation redesign.

Branch hygiene note: the exploratory future specs and architecture research docs created on `feat/progress` are working notes only. Remove them from the Progress PR before publishing unless they are explicitly approved for public review.

## Goal

Create a real `Progress` destination that summarizes the user's training history instead of only listing raw activity logs.

Progress should bridge the gap between:

- Activity: detailed timeline of what happened.
- Trophies: collectible milestones derived from history.

The screen should answer: "how am I doing over time?"

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

- Summary cards.
- Weekly completion trend.
- Category distribution.
- Trophy highlight.
- Recent activity preview.

Recommended additional graph surfaces for later in v1 if the screen still reads clearly:

- A second trend chart for weekly volume or planned item count.
- A compact split for completed items vs. rest/busy/sick weeks.
- A small category trend over time if category balance becomes more useful than a single snapshot.

The screen should support adding or removing these sections without hard-coding the order into the UI. The layout should stay data-driven so sections can be hidden, shown again or reordered later, similar to how categories can be organized.

### Summary Cards

Small top cards should summarize recent progress.

Candidate metrics:

- Workouts completed this week.
- Completed weeks in the last 4 or 8 weeks.
- Current consistency streak if derivable from existing trophy/history logic.
- Next upcoming race/event once Race & Events exists.

Do not show metrics that require new tracking unless the implementation already has reliable data.

### Weekly Completion Trend

A compact chart should show completion over recent weeks.

Preferred first chart:

- Last 8 weeks.
- One bar per week.
- Bar height or fill based on completed workouts / planned workouts.
- Empty weeks should be visually distinct from planned-but-incomplete weeks.

This can be implemented with Compose primitives first. Avoid adding a chart dependency unless native Compose drawing becomes too costly.

If space and readability allow, Progress can include one more chart in the first release rather than stopping at a single trend card. The key rule is that additional graphs must help the user scan the data quickly, not turn the screen into a report dump.

### Category Distribution

Show how recent completed or planned workouts are distributed by category.

Preferred first version:

- Last 4 or 8 weeks.
- Horizontal stacked bar or small ranked list.
- Use category color and name.
- Keep Uncategorized last.

Race/events should not count as completed workouts in this chart, but future versions may show race/event category presence separately.

### Trophy Highlight

Show one or two trophy signals:

- Recently unlocked trophy, if any.
- Closest locked trophy, if no recent unlock.

Use existing trophy presentation models where possible.

Do not duplicate the full trophy grid.

### Recent Activity Preview

Show the latest few Activity entries as a preview.

Rules:

- Keep it short, for example 3 to 5 entries.
- Include a "View all" action into Activity if Activity is no longer top-level.
- Do not bring all Activity filters into Progress.

## Race & Events Integration

Race/events should be optional in the first Progress version.

Once Race & Events exists, Progress can include:

- Nearest upcoming race/event.
- Days remaining.
- Number of planned workouts before that event.
- Gap between next two events.

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

- Strong summary cards at the top.
- Compact charts with category color accents.
- Calm, readable, non-competitive tone.
- Avoid dense analytics language.

Charts should be simple:

- Bars.
- Stacked bars.
- Small trend cards.

Avoid line charts unless the data actually benefits from precise time-series comparison.

The screen should keep its sections flexible enough that the user can eventually decide what appears first, what is hidden, and what gets promoted or demoted over time. v1 does not need drag-and-drop yet, but it should not hard-code a forever-fixed order.

## Navigation Direction

If Progress is implemented, it becomes the preferred middle bottom-nav item:

- Week.
- Progress.
- Race Events.
- Browse.

Activity can move into Browse or become a detail route from Progress.

Trophies can remain in Browse, with Progress linking to trophy highlights.

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
- The section order and visibility model is flexible enough to support future hide/show and reorder controls without reworking the screen architecture.
