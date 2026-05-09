# Progress Coaching Summary Design

Date: 2026-05-09

## Goal

Redesign the Progress screen so it reads like a weekly coaching summary instead of a collection of loosely related dashboard cards.

The screen should answer:

- How is this week going?
- What does the recent trend mean?
- Is my training mix balanced?
- What should I pay attention to next?
- What changed recently?

The first implementation remains read-only. It should use existing workout, category, trophy, event and activity data.

## Design Direction

Progress should lead with interpretation, then show evidence.

The current screen already has useful data, but the sections do not explain themselves well enough. `At a glance` is especially weak because it presents compact metrics without context. `Weekly completion` has a useful chart, but the user has to infer what the bars and percentages mean. `Category mix`, trophy, upcoming and recent activity are useful only when they support the broader progress story.

The new direction is:

1. One strong top coaching note.
2. Clear visual sections with labels and explanatory captions.
3. Section insights only when the data supports a useful interpretation.
4. More visual information, but neutral top-level styling.
5. Activity as a compact preview, not a second Activity screen.

## Screen Order

### 1. Weekly Readout

Replace `At a glance` with a neutral coaching summary card.

The card should include:

- Status headline, for example `You are on track`.
- Current-week completion, for example `6 of 7 planned workouts complete`.
- Next action, for example `Strength is next`.
- A horizontal progress indicator.
- Compact supporting metrics when useful, such as `1 planned workout left`.

Use a calm neutral surface. Avoid colorful gradients at the top of the screen. Color should be used as an accent, not as the dominant visual style.

This card owns the main coaching note. Do not repeat a coaching paragraph in every section.

### 2. Weekly Completion

Keep the weekly bar chart, but make it self-explanatory.

Required chart information:

- Last 8 weeks.
- One vertical bar per week.
- Bar height is completion percentage.
- Current week is highlighted.
- Vertical scale labels: `100%`, `50%`, `0%`.
- A caption explaining that `100%` means every planned workout for that week was completed.
- Completed/planned labels such as `6/7`, at least for the current week.

Preferred behavior:

- Show completed/planned counts under every bar when space allows.
- If narrow screens become crowded, show completed/planned counts for the current week and the immediately previous week, then use week labels only for older bars.
- Add one insight only when useful, for example `Current week has the largest planned workload shown`.

This avoids the misleading impression that every `100%` bar means the same workload. A perfect week can be `3/3`, `5/5` or `6/6`.

### 3. Training Mix

Rename or frame `Category mix` as `Training mix`.

The section should show:

- Completed workouts over the last 8 weeks.
- A horizontal stacked bar where color shows category and width shows share.
- Category rows with color dot, name, completed count and share percent.
- A short caption explaining the window and meaning.
- Optional insight only if meaningful, for example `Your mix is balanced` or `Run is dominating this window`.

Race events should not count as completed workouts in this chart.

### 4. Next Focus And Trophy

Keep upcoming event/workout focus and trophy highlight as compact supporting cards.

They should not compete with the coaching readout or charts.

Recommended presentation:

- `Next focus`: title and timing, for example `Strength · 1 day away`.
- `Trophy`: latest unlocked trophy or nearest trophy progress.

`Next focus` should prefer the next scheduled incomplete workout in the current week. If the current week has no remaining workout, it should fall back to the nearest upcoming race/event. If neither exists, hide the card.

Only add coaching text if there is a real connection to the progress story.

### 5. Recent Activity Preview

Show Recent activity as a compact timeline preview.

Rules:

- Show 2 to 3 items.
- Include `View all`.
- Do not duplicate the full Activity screen.
- Use compact titles, for example `Workout completed`, not full sentence titles.
- Put detail in subtitles, for example `Mobility · Week of May 4`.
- Use small icons or color dots by activity type.

Example structure:

```text
Recent activity · View all

20:32  Theme changed
       Dark -> Light

20:31  Workout completed
       Mobility · Week of May 4

20:31  Event completed
       6 of 7 planned workouts now complete
```

The section should answer `what changed recently?`, not become the main visual endpoint of Progress.

## Insight Rules

Use smart information sparingly and only when it is supported by data.

Recommended rule:

- Always show the top weekly coaching note.
- Add a section-level insight only when it says something new.
- Do not force every section to have an insight.
- Prefer clear captions over fake-smart interpretation.

Good insights:

- `This is your heaviest planned week in the last 8 weeks.`
- `Your current week is stronger than the first 3 weeks shown.`
- `Your mix is balanced across Run, Cycling and Mobility.`
- `Other is your top category, but it is not dominating the window.`

Avoid:

- Fitness scoring.
- Readiness claims.
- Advice that requires data the app does not track.
- Repeating the same completion number in multiple places without adding meaning.

## Visual Guidelines

- Top card: neutral surface, restrained accent.
- Charts: use color to encode meaning.
- Category colors should come from the existing category color system.
- Progress accent should use the app theme color, not hardcoded color values.
- Avoid dense analytics language.
- Avoid making the screen feel like a report dump.
- Prefer captions close to charts so users understand what is being measured.

## Data And State

The redesigned screen can reuse the existing Progress data sources:

- Weekly workouts for completion and trend.
- Categories for training mix.
- Trophy progress for trophy highlight.
- Race/event rows for upcoming or next focus where available.
- Activity presentation items for recent activity preview.

New presentation state is likely needed for:

- Top coaching readout.
- Weekly chart labels and optional insight.
- Training mix stacked-bar data and optional insight.
- Compact recent activity preview rows.

Progress remains read-only. No new Activity logging is required unless the redesign introduces state-changing controls, which is out of scope for this design.

## Testing

Recommended coverage:

- Weekly readout maps current-week completion and next focus correctly.
- Weekly chart exposes completion percent and completed/planned labels.
- Weekly chart handles `0 planned`, partial weeks and perfect weeks with different planned counts.
- Training mix excludes race events and counts only completed workouts in the selected window.
- Training mix handles hidden categories and uncategorized rows consistently with current rules.
- Recent activity preview limits items to 2 or 3 and preserves navigation to the full Activity screen.
- Optional insights appear only when their conditions are true.

## Implementation Notes

- The existing broader spec at `docs/specs/progress-screen-spec.md` should be reconciled with this coaching-summary design before implementation if that file remains the active product spec.
- Visual companion mockups are exploratory artifacts and should stay outside version control.
