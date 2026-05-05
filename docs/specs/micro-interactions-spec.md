# Micro-Interactions Spec

Target release: after core product work; only when tied to specific UX problems.

## Goal

Add subtle motion and feedback that makes Hermes feel clearer and more responsive without slowing planning down.

Micro-interactions should help users understand state changes. They should not be decorative noise.

## Product Fit

Hermes should feel calm and lightweight. Good micro-interactions reinforce:

- drag/drop placement.
- completion state changes.
- event creation/update confirmation.
- trophy celebration.
- empty to populated transitions.

## Candidate Areas

### Weekly Planner

- Stronger drop target feedback.
- Smooth row placement after move/reorder.
- Completion check transition.
- Rest/busy/sick/race-event creation feedback.

### Race Events

- Countdown card update feedback.
- Add/edit confirmation.
- Past/upcoming section transition.

### Trophies

- Refine celebration snackbar/banner timing.
- Gentle unlock emphasis.
- Family/detail transition polish.

### Reports And Progress

- Bar chart entrance.
- Share confirmation.
- Empty-state to populated-state transition.

## Constraints

- Respect system animation/accessibility settings where practical.
- Keep animations short.
- Do not block input.
- Do not animate every list item in long lists.
- Avoid making state harder to track in tests.

## Implementation Direction

Prefer Compose-native animation primitives:

- `animate*AsState`.
- `AnimatedVisibility`.
- `animateItem` where appropriate.
- transition APIs for small state groups.

Avoid adding animation libraries unless Compose primitives are insufficient.

## Testing

Recommended checks:

- Compose tests still find stable semantics.
- Manual dark/light pass.
- Manual font-scale pass for animated surfaces.
- Verify reduced-motion behavior if implemented.

## Acceptance Criteria

- Each animation has a clear UX purpose.
- Planning interactions remain fast.
- Accessibility is not worse.
- Tests remain stable.
