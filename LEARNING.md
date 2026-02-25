# LEARNING — Technical Notes & Decisions

This document exists to capture the reasoning behind the project as it evolves.
It focuses on intent, trade-offs, and lessons learned rather than code details.

--- 

Hermes is a weekly training planner for people who want a simple way to sketch a week, move things around, and keep going without feeling judged by an app. The core problem it solves is the gap between "I need a plan" and "life keeps changing." It gives you a light, editable week view so the plan can bend without breaking.

Core ideas:
- Weekly-first: the week is the main unit of thinking, like a whiteboard that shows the whole week at once. Days are slots, and there is an "unassigned" bucket for workouts that are not placed yet. This makes planning feel like arranging sticky notes rather than committing to rigid appointments.
- Offline-first: everything lives on the device. There is no account, no server, and no dependency on a network connection. The plan is always available, even if you are offline.
- Calm UX: the UI keeps states simple (planned, completed, rest) and treats rest days as first-class items. It avoids the "pushy coach" vibe; the tone is supportive and flexible.

Architectural choices and why they fit:
- The app keeps a clear flow from UI to view models to repositories, which helps keep UI concerns separate from persistence. This matters because the app is mostly about state changes (moving, editing, completing), and a clean flow keeps those transitions predictable.
- Local persistence uses a database for workouts and a lightweight preferences store for settings like theme and language. That split keeps the domain data durable while keeping small settings fast and simple.
- Reactive flows and state holders are used so the UI can react to changes without manual refresh logic. That matches the "drag it, see it" interaction style.
- User actions are logged locally and shown in an activity feed. This provides gentle feedback without turning into analytics or performance tracking.

What this project deliberately avoids:
- No accounts, no cloud sync, no server-side dependency. If that changes, it should be a deliberate decision, not a default.
- No heavy or competitive gamification. The tone is "supportive planner," not "performance tracker."
- No pressure-driven charts or streak mechanics that punish missed days. The focus is on weekly planning and easy rescheduling, not on scoring the user.

Assumptions made from the current code and README:
- The week starts on Monday, so "weekly-first" is anchored to that calendar view.
- A day can hold a rest day or workouts, but a rest day is treated as mutually exclusive with workouts for that day. This keeps the mental model simple and avoids mixed signals.

If future work adds light recognition (soft streaks, small trophies, gentle celebrations), it should stay opt-in and non-judgmental so it reinforces the calm, offline-first intent instead of undermining it.

Recent learnings:
- Single-step undo fits best as a ViewModel-scoped command snapshot, with a timeout job to clear stale actions so Snackbars do not linger across navigation.
- Restoring deleted workouts needs a repository-level insert that can accept full workout data (including completion/rest-day flags), keeping the UI layer free of Room-specific concerns.
- For undo snackbars, using an indefinite duration lets the ViewModel own dismissal timing so the UI does not cancel the prompt before the undo timeout expires.
- Undo snackbar copy needs rest-day-specific strings; adding new snackbar resources requires updating every localized `strings.xml`.
- Rest days are not completable; the ViewModel ignores completion toggles for rest-day items so undo and activity messaging stays consistent with the model.
- Undoing moves should normalize orders in the affected buckets to prevent duplicate sort indexes when new items are created during the undo window.
- Undoing deletes should also reindex affected buckets because new items can be added before undo, which otherwise leaves duplicate order values.
- When detekt flags ViewModel size, move pure helper routines (ordering normalization and action logging) to package-level functions so the ViewModel keeps orchestration responsibilities without losing behavior.
- Week-level replace flows are safer when implemented as `delete target -> copy source placements`, while undo stores a full pre-replace snapshot and restores it with explicit week deletion first; this guarantees exact rollback without residual copied items.
- One-off informational snackbars (like “nothing to copy”) work better as a dedicated `SharedFlow` event channel, while undo continues as stateful data with timeout semantics.
- Week-level features need dedicated `UserActionType` entries (including undo variants) if they should appear in Activity; relying only on week subtitles without explicit action titles makes the feed fall back to generic messaging.
- Compose UI tests that only render composables should use `createComposeRule()` so the test activity provided by `ui-test-manifest` is used; relying on `createAndroidComposeRule<ComponentActivity>()` can leave tests without a launched activity and no compose root.
- Keeping `WeeklyTrainingViewModel` under detekt’s function count is easiest by moving pure helper logic (like post-restore list assembly) to file-level functions that depend only on repositories and mappers.
- For long undo flows, extracting restore/normalization steps into shared helper functions keeps ViewModel methods short without hiding behavior behind mocks.
- Copying a week should be a single Room transaction (delete + insert) to avoid leaving a week empty if a copy fails mid-flight.
- When undo restores also adjust other rows, re-fetch the week before normalization so ordering uses the post-mutation state.
- Copy-replace confirmation should rely on a “week loaded” signal, not an empty UI list, to avoid skipping warnings during initial load or week transitions.
- Category seeding is easier to keep localized when it runs in a domain-level seeder (using `StringProvider`) after Room migrations add the schema; the seeder can also ensure `Uncategorized` exists and backfill null category IDs without hardcoding names in SQL.
- When a new settings sub-screen is needed but there is no nav graph, a lightweight route flag passed through the app shell keeps navigation simple while allowing deep links like “Manage categories” to jump directly into the settings subsection.
- We kept the workout data model field as `type`, but updated UI copy to call it “Title” so UX wording can evolve without forcing a data migration or API rename.
- Reordering the workout row to show category chip first, then title as bold body text, keeps category scanning consistent while making the title feel more like primary content.
- Day indicators now carry both the last workout (for color) and an aggregate completion flag, so the header can show a completion mark without losing the existing color logic.
- When the day indicator wraps the weekday letter, the indicator color should be nudged darker in light theme and lighter in dark theme to keep the letter + check readable without changing the category palette itself.
- Rest day visuals should reuse the same elevated surface color and `onSurfaceVariant` content color as activity/settings cards, keeping rest days muted while matching the app’s card language.
- To return users from a settings sub-screen back into an in-progress dialog, capture the dialog draft before navigation and replay it on return instead of relying on composable-local state.
- Restoring starter categories should only insert missing defaults (by their seeded IDs) so custom categories and user edits remain intact; logging a dedicated user action keeps Activity consistent.
- Moving Settings options into reusable detail screens reduces the main screen to navigable rows while keeping option cards consistent across Theme, Language, and Categories.
- Settings now uses a shared info-row pattern for feedback/rating actions, keeping card visuals consistent while routing to external intents.
- XML string resources must escape `<`, `>`, and `&` (e.g., week navigation chevrons and “A & B” labels), otherwise resource merging fails at build time.
- Email intents are more reliable when subject/body are placed in the `mailto:` URI query parameters and mirrored in extras; some mail clients ignore extras for `ACTION_SENDTO`.
- To keep system categories localized without overwriting user edits, only rename a system category on language change when its current name still matches the previous locale’s default; use `StringProvider.getForLanguage` to fetch the target locale string without switching the app language.
- Restoring default categories should also resync system category colors and localized names after inserts, so seeded defaults stay consistent while user categories remain untouched.
- Activity feed subtitles read clearer when changed values and day labels are consistently quoted, and category-name edits can rely on the subtitle rather than repeating the category label in the action title.
- Category help dialogs should live in the screen that owns the feature, with localized strings added alongside other category copy to keep UX guidance consistent across locales.
- Reselecting a bottom-nav destination can be used to reset nested UI state; for Settings, routing the tab click to `SettingsRoute.MAIN` provides a simple “return to root” behavior without adding a separate nav stack.
- The app window background should match the Compose theme background (including night overrides) to avoid a light/dark flash between the splash and first composable render.
- Skipping the initial language-apply pass avoids an extra Activity recreation on cold start; apply only after the first language value change to reduce launch flicker.
- Gating the main content behind a loading indicator avoids showing an empty state briefly before the first data load.
- Reusing `TitleChip` for Add Workout dropdown options keeps category label+color semantics aligned with Categories and weekly list surfaces, reducing visual drift between “manage” and “select” flows.
- In Add Workout, preserving `null` category selection lets the field clearly show “Uncategorized” as the default state until the user explicitly picks a category.
- For this category picker, keeping `TitleChip` in the `prefix` slot and binding a non-empty field value makes the selected/default category visible consistently; the text value itself can stay visually hidden while the chip carries the UI.
- In dropdown affordances that jump to a management flow, matching the action text color to existing primary-text actions (like “Restore defaults”) improves visual consistency across category-related entry points.
- Activity subtitles should fall back to a single category label when only one category name is available, so create and category-change actions still surface category info even with partial metadata.
- When activity rows show week + category info, put the category subtitle on its own line so the week label stays visually distinct.
- Centralizing category IDs/color IDs and shared theme thresholds in constants reduces hardcoded strings/numbers and keeps UI contrast logic consistent across screens.
- Keeping entity/domain mappers in a dedicated data-layer file keeps repositories focused on orchestration while still making mapping helpers easy to reuse.
- Pulling ad-hoc dp values into `Dimens` keeps sizing consistent and makes future layout tweaks centralized.
- When a dialog can navigate to a management flow that mutates its backing list, revalidate the selected ID on return and defensively normalize IDs again in the ViewModel to avoid persisting stale references.
- If a “restore defaults” action can mutate existing rows (names/colors), log the action whenever any of those updates happen, not only when new rows are added, to keep Activity history complete.
- Keeping locale files in sync with `values/strings.xml` includes adding non-translatable URL/intent templates so the key set matches across locales.
- Typed `menuAnchor` APIs in Material3 require a newer Compose BOM; pinning the BOM to a release that includes Material3 ≥ 1.4.0 avoids unresolved reference errors.
- For async settings actions like seeding demo data, emit a one-shot event from the ViewModel and show UI feedback only after completion so the toast reflects actual state.
- Feature specs must be anchored to the current migration chain and data model (for example, categories added in DB v2) before proposing new schema changes; otherwise implementation plans drift from real integration points and underestimate compatibility work.
- For large cross-cutting features (DB + domain + DnD + UI + settings + activity), defining explicit work-package dependencies up front reduces churn and avoids implementing UI policies before the persistence and ordering contracts stabilize.
- Introducing a new cross-cutting event model is safer when added in an additive first step (`eventType`/`timeSlot` + migration) while keeping legacy behavior (`isRestDay`) temporarily intact; this keeps the app compiling and allows UI/business-rule refactors to happen in smaller, verifiable increments.
- During a gradual migration from boolean-type flags to enums, importing enum entries with names that also exist in other enums (for example `WORKOUT`) causes subtle comparisons against the wrong type; using explicit aliases keeps behavior clear and compile-safe.
- Wiring a new settings policy is most stable when it follows the same flow as existing theme/language settings (DataStore key -> repository flow/default -> viewmodel state -> dedicated detail route); this avoids one-off state holders and keeps navigation consistent.
- [Superseded fallback] Enabling slot-aware drag-and-drop incrementally can be done without a full section-key rewrite by deriving slot from drop position within each day section (top/middle/bottom thirds) and reusing existing day-level hit testing; treat this as temporary only when per-slot bounds are unavailable.
- Reusing existing non-workout action types in Activity while adding explicit `UserActionEntityType` values (`BUSY`, `SICK`) keeps analytics/action history backward-compatible and still allows user-facing copy to be specific per event kind.
- For conversion actions, logging `entityType` from the source event when converting to workout preserves semantic context in Activity titles (for example, "busy to workout" vs generic rest-day wording).
- WeeklyTraining tests that assert update/complete behavior must seed matching workouts in `state` first; current ViewModel guards intentionally no-op when the target item is missing from the loaded week.
- Slot headers in weekly planning read better as elevated `Surface` blocks (same shape/tonal elevation used by Activity and Settings cards), because they introduce a clear visual layer for morning/afternoon/night without changing row behavior.
- For slot-mode readability, wrapping each turno as a single surface block (header + rows together) communicates grouping better than styling only the header, especially when multiple events exist in one day.
- Non-workout weekly rows can reuse the weekly divider visual language by applying a `1.dp` `outlineVariant` border; this adds separation for rest/busy/sick items without introducing a new color token.
- If non-workout rows need stronger separation, setting both border and background to `outlineVariant` creates a high-contrast “status block” style while still reusing existing theme tokens.
- Drag/ghost handling in a parent `awaitPointerEventScope` should track a stable `PointerId` for the active drag; using `event.changes.firstOrNull()` can bind ghost movement/drop to a different finger or pointer change.
- A live drop-preview badge (computed with the same helper used by final drop) reduces perceived slot mismatch because users see the exact interpreted destination before release.
- To avoid “preview says #N but drop lands at #N+1”, commit drop using the last live preview snapshot instead of recomputing on pointer-up; this keeps final placement aligned with what the user just saw.
- Drop preview affordances can reuse workout category accent as border color, giving immediate visual continuity between dragged item and landing hint.
- In slot mode, highlighting the currently targeted slot card from live drop preview state (instead of pointer math duplicated in UI) keeps drag feedback accurate and avoids desync between visual hint and drop behavior.
- Running lint’s `UnusedResources` check before cleanup is a safe way to prune legacy starter assets (old Material color swatches, unused launcher drawables, and stale strings) without guessing references.
- Grouping related planner settings under the Workouts section (Categories + Slot mode) reduces top-level fragmentation and makes weekly-planning controls easier to discover.
- Reusing a shared detail-header pattern with an optional top-right help action keeps Settings sub-screens aligned with Categories-style discoverability while avoiding per-screen header duplication.
- For help dialogs, storing paragraph breaks directly in localized `strings.xml` (`\n\n`) keeps copy layout controlled by translators and avoids hardcoded line-break logic in composables.
- Reusing identical header spacing (`start/end` padding + right-aligned help action) across Settings and Categories improves scan consistency and keeps navigation affordances in predictable positions.
- Main settings navigation labels should stay stable (feature name) rather than reflecting current selected value when the row navigates to a detail screen; it reads clearer for discovery.
- When UI tests need to assert on composable test tags, keeping tag constants `internal` (instead of private) avoids hardcoded strings while staying scoped to the module.
- Moving undo workflow helpers (reorder/delete/replace) into shared helper files keeps `WeeklyTrainingViewModel` under detekt size thresholds without changing behavior.
- Reorder vs move logging for weekly events should treat time-slot changes as moves (even on the same day), otherwise Activity history can misleadingly show a reorder while the item actually changed turno.
- For multi-locale feature rollouts, audit newly added keys by diffing localized values against base (`values/`) to catch accidental English fallbacks (for example, newly added action labels).
- For planner action labels, concise verb+noun copy (for example, "Add rest" / "Adicionar descanso") localizes better than literal event-day phrasing and remains clear in both full-day and slot contexts.
- Keeping related localized action keys grouped together in each `values-*/strings.xml` makes cross-locale review simpler and reduces misses when copy changes for the same feature.
- Event-type icon mapping should be centralized and reused across list rows and day indicators; otherwise new event types stay visually inconsistent even when action menus are already updated.
- In Compose drag rows, avoid `pointerInput(Unit)` when row identity can change; keying pointer input by stable item ID prevents stale drag callbacks from selecting a previous item after recomposition/reordering.
- Day-header indicator selection cannot rely on raw `order` when slot mode exists, because order is scoped per slot; use visual ordering `(slot rank, order)` to reflect the actual last item shown in the day.
- Calendar non-workout indicators should receive the same non-workout background token used in rows (`outlineVariant`) to avoid color drift between header and list.
- Day indicator ownership should prioritize workouts over non-workout events on mixed days, even if a non-workout is later in slot/order; users read the top indicator primarily as training status when training exists.
- For same-slot/same-order ties, day indicator ownership must include source-list index as final tiebreaker to match the last visually rendered workout, otherwise the header can pick the wrong color owner.
- Header indicator icon alignment should share the same bottom offset across completed and non-workout states; mixing bottom-only and vertical padding creates visible jitter between days.
- Even when resource keys remain legacy (`*_rest_day`), user-facing copy can and should evolve to new product terminology (for example, "rest event") across dialogs and activity logs in all locales to keep language consistent.
- For destructive confirm actions in event-specific dialogs, a generic confirm label (for example, "Delete") improves button clarity while keeping event context in title/body copy.
- Undo toast copy should derive from concrete `eventType` (not legacy booleans like `isRestDay`) to avoid collapsing busy/sick into rest messaging for move/delete actions.
- Option labels and help copy should use the same localized term for policies (for example, "Automático") to avoid mixed-language UI in settings detail screens.
- In pt-BR, slot settings copy reads more naturally as the feature concept ("Divisão do dia") than a literal technical label ("modo por turnos"); updating title, help text, option label, and activity action together keeps terminology consistent across Settings and Activity history.
- For Detekt compliance in formatter-heavy files, a targeted suppression for size (`LargeClass`) plus small refactors for rule-specific findings (`ReturnCount`, import ordering, max line length) is a low-risk cleanup path that keeps behavior unchanged while restoring CI signal.
- For activity/log enums with persisted string values, introducing a new canonical enum entry (e.g., `REST`) while keeping the legacy one (`REST_DAY`) as a compatibility fallback allows terminology cleanup without breaking old history records.
- Demo/debug seed data should mirror current feature semantics (eventType + timeSlot + slot-mode activity metadata), otherwise manual QA can pass on stale fixtures while real Activity/weekly UI paths remain underexercised.
- Slot-aware drag/drop should use measured per-slot card bounds for hit-testing; splitting the full-day section into equal thirds drifts when slot card heights differ and can place drops into the wrong turno.
- Migration note: if any path still uses thirds-based slot inference, migrate it to measured per-slot card bounds and keep thirds only as a defensive fallback.
- UI status/preview labels in Compose should be built from localized format strings (not inline separators like `|` and `#`) so punctuation/order can vary per locale without code changes.
- For icon-only affordances in Compose, prefer `Surface(onClick=...)` over nested `Box.clickable`: it provides button semantics and contained ripple, while keeping accessibility touch target (48dp) independent from glyph size (22dp).
- Distinguishing non-workout actions by event type at the `UserActionType` level (busy/sick/rest) improves analytics fidelity while preserving backward-compatible formatter behavior via shared resource mapping by entity type.
- When logging contracts add `entityId` for undo actions, unit tests that verify logger calls must assert the concrete ID (for example, `weekStartDate.toEpochDay()`) instead of `null`.
