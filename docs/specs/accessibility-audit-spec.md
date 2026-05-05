# Accessibility Audit Spec

Target release: continuous; can run alongside feature work without changing product scope.

## Goal

Make Hermes easier to use with larger text, screen readers, touch accessibility and color/contrast needs.

This spec is an audit and remediation plan, not a new product feature.

## Priority

Accessibility should be treated as release quality work. It is especially important for:

- Week planner.
- Race Events.
- Trophies.
- Settings.
- Add/edit dialogs.
- Backup/import flows.

## Audit Areas

### Screen Reader Semantics

Check:

- Icon-only buttons have meaningful content descriptions.
- Decorative icons do not create noise.
- Rows expose useful labels and state.
- Completion controls announce checked/unchecked state.
- Drag/drop alternatives are understandable where possible.

### Touch Targets

Check:

- Interactive controls meet minimum touch target size.
- Visual size and touch target can differ where needed.
- Dense row actions remain tappable.

### Dynamic Type

Check:

- Text scales without clipping.
- Bottom navigation labels do not overflow badly.
- Dialog fields remain usable with larger fonts.
- Cards do not hide critical actions at larger text sizes.

### Color And Contrast

Check:

- Category colors preserve readable text.
- Completed/planned/rest/busy/sick/race-event states are distinguishable without color alone.
- Dark and light themes both pass practical contrast review.

### Navigation And Focus Order

Check:

- Screen reader traversal follows visual order.
- Dialog focus starts in a useful place.
- Snackbar actions are reachable.
- Date picker and category picker interactions are understandable.

## Current High-Risk Surfaces

- Weekly planner rows with multiple actions and drag behavior.
- Race Events grid cards.
- Trophy cards and celebration snackbar action.
- Category color chips.
- Backup/import result messages.
- Bottom navigation labels in localized languages.

## Remediation Rules

- Use localized strings for content descriptions.
- Do not hardcode test tags or strings.
- Preserve Hermes `Dimens` usage for touch sizes.
- Prefer semantic Compose controls when available.
- Keep fixes small and targeted.

## Testing Direction

Recommended checks:

- Compose tests for important content descriptions.
- Font-scale manual pass.
- Light/dark contrast manual pass.
- TalkBack manual smoke pass for top-level screens.
- Locale smoke pass for long bottom-nav labels.

## Acceptance Criteria

- Critical icon buttons have meaningful labels.
- Top-level screens remain usable at larger font scales.
- Race events and workouts are distinguishable without color alone.
- Important actions are reachable by screen reader.
- Audit findings are tracked and fixed incrementally.
