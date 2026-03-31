# Trophy Page UX Spec

## Status
- Draft for planning
- Date: 2026-03-25
- Planned release target: `1.6.0` (`appVersionCode = 11`)

## Purpose
Define how the trophy feature should look and behave in Hermes so the experience feels collectible, calm, and premium instead of noisy or arcade-like.

This spec complements:
- [trophies-achievements-spec.md](/Users/rafaelcordeiro/AndroidStudioProjects/hermes/docs/specs/trophies-achievements-spec.md)

## Product Intent
The trophy page should feel like a personal cabinet of training moments.

It should communicate:
- progress
- identity
- momentum
- recovery and adaptability

It should avoid:
- aggressive competition
- failure-heavy streak loss messaging
- analytics dashboard aesthetics
- gamification that pressures the user

## Placement

### Navigation
- Trophy page should be its own top-level destination, not a sub-screen hidden under Activity.
- Activity remains the event timeline.
- Trophies become the interpreted, collectible layer built from that history.

### Relationship To Activity
- Activity answers: "What happened?"
- Trophies answer: "What patterns and milestones have emerged?"

## Page Structure

### Top area
- Page title
- Short supportive subtitle
- Optional hero card featuring:
- most recently unlocked trophy
- or closest next unlock if nothing was unlocked recently

### Main content
- Sectioned shelf by trophy family:
- Follow Through
- Consistency
- Adaptability
- Momentum
- Builder
- Categories

### Bottom behavior
- No sticky CTA required
- This page is primarily reflective and browseable

## Hero Card

### Purpose
Give the page a focal point without turning it into a reward pop-up screen.

### Behavior
- If there is a recent unlock, show it first.
- Otherwise show the trophy with the nearest next milestone.
- Only one hero item at a time.

### Hero card content
- trophy artwork
- trophy name
- current tier label
- short line of supportive copy
- progress summary

### Example copy
- `You reached Peak in Comeback Week.`
- `3 more completed weeks to reach Session in Full-Time.`

## Trophy Sections

### Section headers
- Family title
- One short descriptor line if needed

### Example
- `Adaptability`
- `For the weeks that changed and still came together.`

## Trophy Card Anatomy

Each card should include:
- artwork
- trophy name
- one-line description
- current tier
- progress toward next tier
- optional unlocked date for the current tier

### Recommended visual order
1. Artwork
2. Name
3. Description
4. Tier badge
5. Progress bar or segmented progress marker
6. Small metadata line

## Card States

### Locked
- Artwork visible but slightly muted
- Name visible
- Description visible
- Progress visible
- Next tier target emphasized

### In progress
- Full artwork
- Current tier highlighted
- Progress treatment visible and readable at a glance

### Newly unlocked
- Slight emphasis treatment
- Optional glow/ring/accent stroke
- Avoid loud confetti or reward-burst visuals

### Fully completed
- Preserve the card in a finished premium state
- Do not hide maxed trophies

## Progress Presentation

### Recommendation
Use calm progress indicators, not game HUD meters.

### Good options
- thin horizontal progress bar
- segmented milestone markers
- compact `current / next` numeric copy

### Avoid
- giant circular meters
- percentage-first presentation
- red warning states when progress stalls

### Copy examples
- `12 of 25 completed weeks`
- `40 of 80 category completions`
- `Current tier: Streak`

## Tier Display

### Tier labels
Use:
- `Warm-Up`
- `Session`
- `Streak`
- `Peak`
- `Legend`

### Display pattern
- Current tier badge is always visible.
- Next tier label appears in secondary text when relevant.
- Maxed trophies show `Legend` without additional pressure text.

## Unlock Moments

### In-page behavior
- Newly unlocked cards should feel noticeable but restrained.
- A subtle accent animation on first render is enough.

### Cross-page behavior
- If a trophy unlock happens elsewhere in the app, Hermes can later show:
- a small banner
- a Snackbar-like celebration
- or a soft modal sheet

This should be designed later, but the trophy page must support a "recently unlocked" source of truth.

## Category Trophy UX

### Why it matters
Category trophies make the page feel personal because they reflect the user’s actual training language.

### Presentation
- Group category trophies under the `Categories` family.
- Show the category name prominently in the card.
- Category color may tint the card accent, but do not overpower the global visual system.

### Example card titles
- `Podium Place`
- `Strength`

or

- `Strength`
- `Podium Place`

### Recommendation
Prefer:
- primary: trophy name
- secondary: category name

This keeps the trophy system coherent across cards.

## Empty And Early-State UX

### No trophies yet
- Show a calm empty state with 2-3 example cards in muted preview form.
- Explain that trophies appear as the user plans, completes, and reshapes weeks.

### Very early progress
- Show visible progress even before first unlock.
- The page should not feel blank during the first days of use.

## Motion

### Recommendation
Use a few meaningful motions only:
- hero card fade/slide on load
- staggered card reveal
- subtle highlight when a new tier is unlocked

### Avoid
- bouncing cards
- slot-machine reward motion
- constant shimmer effects

## Visual Direction

### Overall mood
- premium
- tactile
- athletic without aggression
- collectible without feeling childish

### Surfaces
- Cards should feel like crafted objects, not plain list rows.
- Slight texture, framing, or layered illustration treatment is encouraged.

### Color
- Use family accents, not random rainbow colors.
- Category accents should be controlled and secondary.

### Typography
- Keep names clear and bold.
- Supporting text should remain compact and readable.
- Tier badges should feel like labels, not stickers.

## Interaction Model

### Tap behavior
- Tapping a card opens trophy detail.

### Detail view should show
- full artwork
- full description
- tier ladder
- current progress
- unlocked dates per reached tier
- derivation-friendly explanation such as:
- `Complete weeks after planning changes, then finish the week.`

## Sorting

### Recommended order within each family
1. Newly unlocked
2. In-progress
3. Locked
4. Fully completed

Alternative:
- fixed trophy order if visual consistency matters more than dynamic sorting

### Recommendation
Use fixed order in v1.

Reason:
- more predictable
- easier to learn
- better for premium shelf composition

## Accessibility

### Required
- text labels must carry the meaning, not color alone
- tier badges must be readable in both themes
- progress bars need textual equivalents
- artwork should have accessible labels

## Technical Notes

### Suggested UI model
- `TrophyPageUiState`
- `FeaturedTrophyUi`
- `TrophyFamilySectionUi`
- `TrophyCardUi`
- `TrophyTierUi`

### Recommended derivation boundary
- Trophy computation happens outside composables.
- UI receives already-shaped card data plus progress metadata.

## Testing
- UI tests for empty, in-progress, newly unlocked, and completed trophy states
- UI tests for category trophy rendering with category accents
- unit tests for hero-card selection rules
- snapshot or screenshot coverage if the team adopts visual regression later

## Decision Summary
- Trophy page should be a top-level destination
- The page should feel like a curated shelf, not an analytics dashboard
- Category trophies should be visually integrated, not treated as a special case
- Calm premium motion and collectible card design are more important than flashy reward effects
