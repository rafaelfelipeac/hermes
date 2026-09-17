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

## Wave 7 Status
- Refined Personal Records shelf cards toward the quiet row grammar.
- Replaced decorative category icon bubbles with simple category-tinted icons and a slim accent rail.
- Preserved family grouping, metric chips, current-best summaries, entry counts, and add-first-result actions.

## Wave 8 Status
- Refined the Pace Calculator result card as the outcome surface.
- Added a restrained result rail and primary result emphasis while keeping empty-result copy quiet.
- Preserved mode selection, presets, inputs, calculation behavior, and logging.

## Wave 9 Status
- Refined Activity Log rows toward a clearer timeline surface.
- Added a slim primary rail to each activity item while keeping row bodies neutral in light and dark themes.
- Preserved filters, section grouping, requested-item focus, timestamps, and subtitles.
- Added before and after light/dark captures to the screenshot archive.

## Wave 10 Status
- Refined Challenges list cards with the quiet rail grammar.
- Moved challenge category color into a slim leading accent while preserving neutral card bodies, chips, progress bars, and status labels.
- Preserved active/archived tabs, sorting, list clicks, and create challenge action.
- Added before and after light/dark captures to the screenshot archive.

## Wave 11 Status
- Refined Progress support blocks away from filled `surfaceVariant` panels.
- Moved Next focus and reusable support cards to neutral bordered surfaces while preserving click targets and content hierarchy.
- Preserved weekly readout, trend chart, category mix, recent activity, and support navigation behavior.
- Added before and after light/dark captures to the screenshot archive.

## Wave 12 Status
- Refined Home weekly workout rows away from fully filled category cards.
- Moved workout and race-event rows to neutral card bodies with category-colored borders and existing chips.
- Preserved completion controls, delete action, drag/focus behavior, filters, section grouping, and non-workout event treatment.
- Added before and after light/dark captures to the screenshot archive.

## Wave 13 Status
- Refined Challenge detail completion and metric surfaces.
- Replaced filled celebration and metric tiles with neutral bordered cards while keeping the trophy icon, labels, values, and status hierarchy.
- Preserved summary, Today metrics, quick add buttons, history list, overflow actions, and progress behavior.
- Added before and after light/dark captures to the screenshot archive.

## Wave 14 Status
- Refined Personal Record detail cards with the same category rail grammar used by shelves and rows.
- Added slim category accents to the current-best card and history rows while keeping card bodies neutral.
- Preserved metric/comparison chips, edit/delete actions, manual current selection, history ordering, and add-result FAB behavior.
- Added before and after light/dark captures to the screenshot archive.

## Wave 15 Status
- Refined the Home To be defined help affordance.
- Replaced the filled help bubble with a neutral bordered circular surface while preserving tap target and help dialog behavior.
- Preserved weekly navigation, filters, summary, section grouping, workout rows, and add action.
- Added before and after light/dark captures to the screenshot archive.

## Wave 16 Status
- Refined Home workout editor dialog fields.
- Moved the dialog's title, description, date, and category fields to quiet container fills with outline-variant borders, while preserving existing labels, category chip, date picker trigger, dropdown behavior, and save/cancel actions.
- Preserved workout editing, category management handoff, and date selection behavior.
- Added before and after light/dark captures to the screenshot archive.

## Wave 17 Status
- Completed the final visual regression pass for the UI refactor branch.
- Captured Home, Progress, Events, and Browse in light and dark themes after the wave 0-16 changes.
- Verified the screenshot archive has a folder and README entry for every wave from 0 through 17.
