# Trophies And Achievements Spec

## Status
- Draft for planning
- Date: 2026-03-25
- Planned release target: `1.6.0` (`appVersionCode = 11`)

## Context
Hermes already logs a wide set of local user actions through `UserActionLogger` and exposes them in `Activity`. That history is a strong base for a future trophy page, but the feature must respect the product tone:
- offline-first
- calm
- non-judgmental
- not performance analytics

The trophy page should feel closer to a personal collection cabinet than a score screen.

## Product Goal
Turn existing local history into gentle recognition for planning, adapting, and following through.

This is not about:
- ranking the user
- punishing missed weeks
- exposing failure-heavy streak loss UI

## Recommendation
- Build trophies from existing logs first.
- Separate trophies into families so the page celebrates different behaviors, not just raw completion.
- Use multi-level trophies, but avoid generic metal tiers.
- Make the labels feel like Hermes: calm, crafted, and lightly connected to training/sport language.
- Ship the trophy system, including category-aware trophies and the required logging enrichment, in one release.

## Current Logging Coverage

### Strongly supported by current logs
- workout completion events
- week completion milestones
- move and reorder actions
- copy-last-week usage
- category management
- settings changes
- backup import/export

### Weak or incomplete for trophy derivation
- category-specific workout completion from current logs alone
- "first time ever completed this workout" logic
- exact consecutive-week streak repair logic after imports
- trophies tied to deleted items' categories

### Important existing signal
`COMPLETE_WEEK_WORKOUTS` already exists and is emitted only when the week transitions from not-complete to all planned workouts complete. This is the best foundation for week-based trophies.

## Level Naming Direction

### Recommendation
Replace `Bronze / Silver / Gold / Platinum / Diamond` with names that fit a planner built around weeks, routines, and steady progress.

### Proposed tier names
- `Warm-Up`
- `Session`
- `Streak`
- `Peak`
- `Legend`

### Why this fits better
- `Warm-Up` feels like the first meaningful step.
- `Session` feels active and grounded in training.
- `Streak` connects naturally to repeated practice.
- `Peak` signals a high milestone without sounding too gamey.
- `Legend` gives the final tier some collectible status.

## Trophy Taxonomy

### 1. Follow Through
Celebrates doing the planned work.

#### Trophy: Full-Time
- Derived from distinct `COMPLETE_WEEK_WORKOUTS` records by `week_start_date`
- Levels: Warm-Up 1, Session 5, Streak 12, Peak 25, Legend 50 completed weeks

#### Trophy: Match Fitness
- Derived from `COMPLETE_WORKOUT`
- Levels: Warm-Up 10, Session 50, Streak 150, Peak 300, Legend 500 completion actions

Note:
- this counts completion actions, not guaranteed unique workouts
- acceptable for a first version, but should be documented

### 2. Consistency
Celebrates returning, not perfection.

#### Trophy: In Form
- Derived from consecutive `COMPLETE_WEEK_WORKOUTS.week_start_date`
- Levels: Warm-Up 2, Session 4, Streak 8, Peak 12, Legend 24 consecutive weeks

Reason this works:
- week-complete logs are already deduplicated by transition semantics

### 3. Adaptability
Celebrates planning resilience, which fits Hermes especially well.

#### Trophy: Comeback Week
- Derived when a week has at least one `MOVE_WORKOUT_BETWEEN_DAYS` or `REORDER_WORKOUT` before its `COMPLETE_WEEK_WORKOUTS`
- Levels: Warm-Up 1, Session 3, Streak 8, Peak 15, Legend 30 rescued weeks

This is one of the strongest theme fits in the current app.

#### Trophy: Game Plan
- Derived from total move/reorder actions
- Levels: Warm-Up 10, Session 40, Streak 100, Peak 200, Legend 400 planning adjustments

### 4. Momentum
Celebrates reusing structure without forcing sameness.

#### Trophy: Back In Formation
- Derived from `COPY_LAST_WEEK`
- Levels: Warm-Up 1, Session 5, Streak 12, Peak 24, Legend 52 copied weeks

#### Trophy: Hold The Line
- Derived when a week has `COPY_LAST_WEEK` and later `COMPLETE_WEEK_WORKOUTS`
- Levels: Warm-Up 1, Session 3, Streak 8, Peak 15, Legend 30 weeks

### 5. Builder
Celebrates setting up the system.

#### Trophy: Team Sheet
- Derived from category create/update/reorder/visibility/delete/restore actions
- Levels: Warm-Up 3, Session 10, Streak 25, Peak 50, Legend 100 category actions

#### Trophy: Kit Bag
- Derived from successful backup actions
- Levels: Warm-Up first successful export, Session first successful import, Streak 5 successful backup operations

This family can have fewer levels because it is utility behavior, not a long-run progression track.

### 6. Categories
Celebrates the categories that make the user's training style feel personal.

#### Product stance
- Category trophies should exist.
- They should make the shelf feel personal, not analytical.
- They should be generated from the categories the user actually uses.

#### Trophy: Podium Place
- One trophy set per active category
- Derived from workout completion actions filtered by category metadata
- Example copy: `You completed 25 Strength workouts`
- Levels: Warm-Up 5, Session 15, Streak 40, Peak 80, Legend 150 completed workouts in that category

#### Trophy: Home Ground
- One trophy set per active category
- Derived from completed weeks where at least one completed workout in that week belongs to the category
- The whole week does not need to be only one category
- Levels: Warm-Up 1, Session 4, Streak 10, Peak 20, Legend 40 qualifying weeks

#### Trophy: Training Block
- One trophy set per active category
- Derived from create/update/move/reorder actions associated with that category
- Celebrates shaping a routine around that category, not only finishing it
- Levels: Warm-Up 5, Session 20, Streak 50, Peak 100, Legend 200 actions

## Best Candidates For A First Release
If scope must stay tight, start with:
- Full-Time
- In Form
- Comeback Week
- Back In Formation
- Podium Place for visible categories

Why these four:
- strongest semantic fit
- best current log support
- lowest risk of inflated counts
- easiest to explain visually

Why include `Podium Place`:
- categories are already meaningful in weekly planning
- trophy shelves become more personal when they reflect the user's own training structure

## Curated Naming Set
The most cohesive naming set in this spec is:
- `Full-Time`
- `Match Fitness`
- `In Form`
- `Comeback Week`
- `Game Plan`
- `Back In Formation`
- `Podium Place`
- `Training Block`

These eight names are the recommended style reference for future trophies.

## Trophy Page Structure

### Layout
- Hero shelf with newly unlocked or nearest-progress trophies
- Sections by family: Follow Through, Consistency, Adaptability, Momentum, Builder
- Each trophy card shows an asset, name, one-sentence description, current level, next target, and unlocked date for each earned level.

### Tone
- "You completed 5 weeks"
- "You adapted your plan and still finished"
- Avoid language like "failure", "missed", "lost streak", or "only"

## Asset Direction
- Use handcrafted illustration-style assets, not generic badges
- Each trophy family should have a distinct metaphor:
- Follow Through: torch, summit, check-mark seal
- Consistency: calendar ribbon, chain links, constellations
- Adaptability: compass, folding map, wind sail
- Momentum: relay baton, echo wave, stacked pages
- Builder: toolbox, archive box, cabinet key
- Categories: herbarium cards, stamped labels, colored trail markers, woven patches

### Level treatment
- Same base illustration across levels
- Material and detail evolve by level: Warm-Up -> Session -> Streak -> Peak -> Legend
- Hermes can keep a softer palette than PSN while still feeling premium

## Derivation Rules

### Source of truth
- Trophy progress is derived from persisted `user_actions`
- No separate manual counters in v1

### Unlock model
- Recompute trophy state from history on load
- Persist only optional presentation helpers later if needed, such as "last seen unlocked"

### Imports
- Imported backups already include user actions, so trophy progress can travel with the backup
- Recompute after import instead of trying to merge counters manually

## Category Trophy Constraint
Category trophies should be part of the feature direction, but they are only trustworthy if we enrich logging in the same implementation.

Examples that are not fully reliable from existing history alone:
- complete 50 cardio workouts
- finish 10 running weeks

Reason:
- completion and movement logs do not consistently carry category metadata today

## Logging Additions Required For Category Trophies
Add `category_name` to workout lifecycle logs:
- completion and undo completion
- delete and undo delete
- move/reorder and undo variants

Also keep using `category_name`, `new_category_name`, and `old_category_name` consistently on create/update flows so trophy derivation can reason about both current and changed category context.

This logging enrichment is part of the same release as the trophy page and Activity category filters.

If unique-completion trophies become important, consider logging:
- a stable "completed_from_incomplete=true" style marker
- or a dedicated milestone event for first completion per workout instance

## Technical Shape
- Add a trophy domain module that reads `UserActionRepository`.
- Compute trophy state from action history in one place, not inside UI composables.
- Trophy definitions should be declarative so new trophies can be added without rewriting the engine.

Suggested model:
- `TrophyDefinition`
- `TrophyFamily`
- `TrophyLevel`
- `TrophyProgress`

## Testing
- Unit tests for each trophy derivation rule using fake action histories
- Edge-case tests for undo flows, same-week move plus completion, consecutive completed weeks, copied week later completed, and backup import preserving progress through recomputation.

## Decision Summary
- Trophy page makes sense now because Activity logging is already meaningful
- The strongest first version is week-based, adaptation-based, and category-aware trophies together
- Category trophies are worth doing, but only alongside the metadata enrichment that makes them reliable
- This should ship as one cohesive release, not as a split rollout
