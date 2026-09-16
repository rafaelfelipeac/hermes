# Hermes UI Refactor Plan

## Branch
- Work happens on `feat/ui-refactor`.
- Keep one branch for the full refactor, with visual review after each wave before starting the next wave.

## Visual Direction
- Keep Hermes quiet, compact, and training-led.
- Use the existing blue brand as the default identity.
- Avoid decorative gradients, glass effects, oversized pills, and generic dashboard cards.
- Prefer neutral surfaces, readable rows, restrained category markers, and clear hierarchy.

## Wave 0 Status
- Added a branded-default Material You preference in Settings.
- Kept Material You opt-in and platform-aware: Android 12+ can enable it, older versions retain the imported value but render branded colors.
- Added Activity timeline support for Material You preference changes.
- Updated backup schema to v7 with `settings.useDynamicColor`.
- Older backup schemas 1-6 import with `useDynamicColor = false`.
- Updated backup compatibility notes and focused tests for v7 round trip, legacy defaults, and malformed v7 settings.

## Wave 1 Status
- Finished Categories list refactor first.
- Added individual quiet row cards, drag handles, overflow actions, and atomic move-to-position support.
- Added accessible up/down menu actions while keeping existing edit, visibility, and delete flows.
- Verified light and dark Categories screens, including protected and regular category overflow menus.

## Wave 2 Status
- Applied the same quiet list grammar to the Browse home cards.
- Replaced broad filled cards with neutral elevated destination rows.
- Kept icons small and brand-tinted, with a trailing chevron for navigation affordance.
- Verified light and dark Browse captures before continuing.

## Wave 3 Status
- Applied the quiet row grammar to the main Settings launcher.
- Converted preference and About actions into standalone elevated rows.
- Kept detail option screens card-based to preserve compact form density.
- Verified light and dark Settings captures before continuing.

## Wave 4 Status
- Applied the quiet row grammar to the Backup utility screen.
- Split export, import, and folder actions into standalone elevated rows.
- Preserved per-action disabled semantics during backup operations.
- Added before and after light/dark captures to the screenshot archive.

## Wave 5 Status
- Refined the Trophies shelf cards toward neutral surfaces.
- Removed the filled unlocked-card treatment in favor of restrained borders and a small top accent.
- Made locked trophy cards quieter so overview shelves feel less decorative and more consistent with the refactor direction.
- Added before and after light/dark captures to the screenshot archive, with after captures using mixed trophy demo data to show unlocked and locked states together.

## Wave 6 Status
- Refined Events cards away from fully colored tiles.
- Kept category color as a slim frame, footer icon tint, and chip accent while moving the card body back to neutral surfaces.
- Preserved completion checkbox, edit tap, delete action, section grouping, and countdown behavior.
