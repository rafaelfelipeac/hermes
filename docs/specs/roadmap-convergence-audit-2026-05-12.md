# Roadmap Convergence Audit

Date: 2026-05-12

Status: current roadmap decision layer. This document supersedes the ordering in `future-roadmap-plan.md` and `next-features-plan.md` until a newer convergence doc replaces it.

Input note: `project-research-map-2026-05-11.md` and `project-architecture-audit-2026-05-11.md` were named as inputs, but they are not present in this checkout. The architecture findings from Rafael's prompt are treated as source inputs here.

## Executive Summary

Hermes should narrow its future roadmap around one product identity: a calm, offline, weekly planning app that helps users understand and adjust a week without becoming a coach, performance platform, social product or analytics dashboard.

The strategic lens is broader than normal feature delivery. Hermes should also become a long-term exploration ground for AI-native product thinking, context-aware UX, local-first intelligence, AI-assisted engineering workflows and structured reasoning over the product/code/spec ecosystem. That does not mean shipping an AI feature now. It means making roadmap and architecture choices that keep future intelligent behavior possible without violating calmness, privacy or predictability.

The current spec library has too many future directions with overlapping value claims. The highest-leverage path is:

1. Finish and stabilize Progress as the current read-only reflection surface.
2. Fix the app shell and navigation structure before adding more destinations.
3. Build Weekly Report only after its place in the product is explicit, and keep v1 generated from existing data.

Weekly Report is still a strong candidate, but it should not automatically follow Progress. The stronger default after Progress is Browse/navigation foundation because the current shell is already crowded and app-level navigation state is manual. Under the AI-native framing, this becomes even stronger: future contextual or reasoning surfaces need clean app structure, explicit route ownership and coherent information architecture before they need new model calls or new screens.

Schema-heavy features should wait. Notes, perceived effort, templates and per-item reminders are plausible, but each adds backup, migration, Activity and localization cost. They should not be pulled forward until a concrete user workflow proves that existing descriptions, Progress, Events and a generated report are insufficient.

Several ideas should leave the active roadmap: readiness scoring, race-prep recommendations, PR/pace calculators, social/coach sync, saved report history, PDF/image report export, broad analytics and standalone micro-interaction releases. AI does not rescue these ideas; it makes the drift risk higher unless they are reframed as restrained clarity aids.

## Product Identity Constraints

Future work should pass these constraints before getting an implementation plan:

- Weekly-first: the week remains the main planning unit.
- Offline-first: no account, server, sync or remote dependency by default.
- Calm tone: no guilt, pressure, pseudo-coaching or competitive scoring.
- Existing-data bias: prefer features generated from workouts, events, categories, Activity, trophies and settings.
- Schema restraint: new persisted fields/tables require clear user value, migration, backup and import/export review.
- Local sharing only: sharing can use Android intents, but Hermes should not become a social feed or coach portal.
- Privacy by default: no free text, category names or detailed training content should leave the device.
- Read-only insights first: reflection surfaces should interpret existing data before asking users to track more.
- Explicit review gates: public navigation, persisted schema and backup contract changes must stop for decision before implementation.
- AI as optional context, not identity: intelligent behavior should assist organization and clarity; it should not become a chatbot shell, engagement loop or coaching authority.
- Predictability over cleverness: AI-assisted output must be inspectable, dismissible and easy to override.
- Local-first intelligence: prefer on-device or backup-portable context models before remote reasoning dependencies.
- Minimal memory: store only context that clearly improves planning and can be exported, imported, cleared and understood by the user.

## AI-Native Strategic Lens

Hermes should explore AI-native thinking as systems design, not as a gimmick feature category.

Good AI-adjacent directions:

- Contextual planning assistance that helps organize the week without prescribing training.
- Lightweight adaptation, such as surfacing "this week looks crowded" from existing local state.
- Semantic organization, such as grouping recurring patterns or detecting planning friction without creating scores.
- Reasoning support for reports, summaries, release planning and spec convergence.
- Local-first memory that is explicit, user-owned and backup-aware.
- Hybrid offline/online experiments that degrade gracefully when offline and do not make cloud reasoning core to the app.

Bad AI-adjacent directions:

- Chatbot-first UX.
- Readiness, fatigue, injury, race-time or medical claims.
- Constant nudges or automated pressure.
- Hidden surveillance over user behavior.
- Engagement analytics disguised as intelligence.
- Social comparison or coach-sync infrastructure.
- Automations that change the plan without clear user intent.

AI should increase clarity, not complexity. The product test is: can the user still understand why Hermes is showing something, ignore it, change it and trust that their data remains theirs?

## AI-Aware Decision Changes

| Decision area | Becomes stronger | Becomes weaker |
| --- | --- | --- |
| Progress closeout | Stronger. It is the first structured reflection surface and can teach the app how to summarize without coaching. | More charts for their own sake. |
| Browse/navigation foundation | Stronger. Context-aware systems need clear information architecture and route boundaries before new intelligence surfaces. | Adding more top-level destinations as experiments. |
| Weekly Report v1 | Slightly stronger, but only as generated local text from existing data. It becomes a proving ground for transparent summaries. | Report history, coach presets, PDF/image exports and automatic advice. |
| Notes/context | Stronger as a future context model, weaker as immediate schema. Note-only context may become the first user-authored memory primitive. | Perceived effort as analytics/intensity tracking before the product framing is explicit. |
| Reminders | Stronger only as restrained, context-aware assistance later. | Per-workout notifications and aggressive follow-up loops. |
| Templates | Stronger as pattern recognition/reuse once architecture is cleaner. | Template CRUD as another heavy planner subsystem too early. |
| Analytics | Weaker. AI-native product thinking does not require broad behavioral telemetry; local reasoning and explicit research notes fit the identity better. | Broad Firebase Analytics or detailed event instrumentation. |
| Architecture cleanup | Much stronger. Future intelligence depends on clean boundaries, context models and testable derivation layers. | Shipping isolated AI demos before the system can explain and preserve context safely. |

## Roadmap Dependency Graph

```text
Progress closeout
  -> shared aggregation language
  -> Progress can justify a top-level destination
  -> Browse/navigation redesign becomes viable
  -> Weekly Report can reuse summary vocabulary
  -> future contextual summaries learn from deterministic aggregation first

Browse/navigation foundation
  -> Activity/Categories/Trophies/Backup/Settings can move out of crowded bottom nav
  -> future Report/Reminders/Backup/settings surfaces have a cleaner home
  -> app shell route state can stop growing as manual flags
  -> future intelligent surfaces have explicit entry points instead of ad hoc screens

Weekly Report v1
  -> generated from existing weekly data
  -> validates whether shareable summaries matter
  -> may create real demand for notes/context
  -> creates a transparent summary contract before any AI-generated wording

Notes / training context
  -> requires schema, backup, Activity and localization decisions
  -> should follow proven report/context demand
  -> perceived effort should lag note-only unless the product accepts an intensity signal
  -> possible first user-authored memory primitive if designed as clear local context

Reminders
  -> weekly reminder can live in settings after navigation/settings structure is clearer
  -> per-item reminders require scheduler, permissions and persisted reminder model
  -> intelligent reminders require restraint rules before scheduling logic

Templates / reusable routines
  -> depends on repository workflow cleanup and backup schema decisions
  -> should follow evidence that copy-last-week is not enough
  -> pattern recognition should come after deterministic template/reuse boundaries

Telemetry
  -> requires privacy decision
  -> crash-only can be considered separately
  -> analytics remains outside the active roadmap

Context model / reasoning layer
  -> requires explicit schema and backup/export policy
  -> starts as deterministic derived state, not model output
  -> may later support on-device or hybrid AI experiments
```

## Staged Sequence

| Stage | Product work | Architecture work | Why this order |
| --- | --- | --- | --- |
| 0 | Roadmap convergence and doc cleanup | None beyond docs | Stops stale specs from driving new work. |
| 1 | Progress closeout | Split/trim Progress UI only as needed | Progress is already in motion and uses existing data. Finish the current surface before opening a new product front. |
| 2 | Browse/navigation foundation | App shell route model, manual route-state cleanup | Current bottom nav is crowded. This is public behavior, but it unlocks future surfaces without adding schema. |
| 3 | Weekly Report v1 | Reuse/extract Progress aggregation helpers; Activity formatter split if sharing logs land | Strong existing-data value, but should not be forced before navigation debt is handled. |
| 4 | Context model decision | Repository/API cleanup, Room/backup migration plan, backup/export policy | Decide what "memory" means before adding notes, effort, templates or intelligent reminders. |
| 5 | Notes/training context, if still justified | Context model primitives, Activity formatter split | Only after Report proves that user-authored context would materially improve the app. |
| 6 | Weekly planning reminder, if demanded | Settings route clarity, notification permission/scheduler abstraction | Keep reminders opt-in, narrow and local; intelligent reminders require restraint rules first. |

## Risk Map

| Area | Specs affected | Risk |
| --- | --- | --- |
| Public navigation | Navigation redesign, Weekly Report entry points, Reminders settings | High. Requires explicit review before implementation. Current `HermesAppContent` manually owns destination state, pending drafts and snackbar routing. |
| Persisted schema | Notes/effort, templates, per-item reminders, saved reports, structured race fields | High. Requires Room migration, backup schema decision, import/export tests and Activity logging review. |
| Backup contract | Notes/effort, templates, reminders, structured event fields, telemetry settings | High when new data is persisted. Generated Progress/Report v1 can avoid this. |
| Activity logging | Weekly Report sharing, notes/effort, reminders, templates, telemetry settings | Medium-high. `ActivityUiFormatter` is already monolithic; adding more action types increases formatter pressure. |
| Localization | All visible feature work | High operational cost. Share text, reminder copy, effort labels and nav labels need careful locale handling. |
| Privacy/product trust | Analytics, Crashlytics, notes, sharing, reminders | High. Free text and training detail must stay local. |
| Product identity drift | Weekly Report, notes/effort, race prep, PR calculator, analytics, year review | Medium-high. Useful ideas can quickly become coaching, medical, performance or social-product claims. |
| AI opacity | Future contextual assistance, summaries, reminders, memory | High. Any model-assisted output must be explainable, dismissible and non-authoritative. |
| Context persistence | Notes, memory, templates, intelligent reminders | High. User-owned context needs export/import/delete semantics and should not silently become telemetry. |

## Spec Classification

| Spec | Classification | Decision | Prerequisite / risk | Action |
| --- | --- | --- | --- | --- |
| `progress-screen-spec.md` | Build soon | Treat as Progress closeout/stabilization, not open-ended future scope. | UI density, empty/partial states, localized copy, no new schema. | Keep active until Progress ships, then archive/update as shipped behavior. |
| `weekly-report-spec.md` | Needs prerequisite | Strong candidate, but not automatically next after Progress. Default order: after Browse/nav foundation. | Share logging, localized share text, possible route/entry-point decision. | Keep, but narrow v1 to generated local text report from existing data. |
| `navigation-redesign-spec.md` | Needs prerequisite, then build soon | Revalidated as the next major change after Progress closeout. Its dependency is not "Progress exists" only; it also needs explicit public-nav review and app-shell cleanup. | Public navigation change, pending draft flows, trophy snackbar targeting, settings subroutes. | Keep active. Split implementation into route model + Browse hub + moved destinations. |
| `notes-effort-spec.md` | Needs prerequisite / split | Reframe as "local context and memory" before implementation. Note-only may be useful; perceived effort is more product-risky. | Room migration, backup schema, Activity logging without free text, effort tone, memory export/delete semantics. | Do not build before Weekly Report proves context demand. Split note and effort decisions before implementation. |
| `reminders-notifications-spec.md` | Needs prerequisite / defer | Keep only weekly planning reminder as plausible first scope. Future intelligent reminders need restraint rules before scheduling. | Android permission, scheduler, reboot/timezone handling, settings UX, possible schema, notification pressure risk. | Defer until navigation/settings structure is clearer and user demand exists. |
| `micro-interactions-spec.md` | Merge into another feature | Not a standalone roadmap item. Animations should attach to concrete UX problems. | Accessibility/reduced motion, test stability. | Convert to a feature QA checklist; remove from ranked roadmap. |
| `accessibility-audit-spec.md` | Build soon | Continuous quality track, not a product release. | Dynamic type, TalkBack, touch targets, localized nav label width. | Keep active and run alongside Progress/nav work. |
| `build-tooling-spec.md` | Build soon | Continuous quality track. | Low product risk. | Keep active. Prioritize release checklist and verification docs before more schema work. |
| `analytics-crashlytics-spec.md` | Needs prerequisite / defer | Split Crashlytics from Analytics. Crash-only may be acceptable; broad analytics is weaker under the AI-native/local-first framing. | Privacy decision, Firebase acceptance, telemetry abstraction, no free text, no training-content capture. | Create a separate privacy decision before any implementation. |
| `race-events-spec.md` | Delete or archive from active roadmap | Shipped implementation reference, not future scope. Future race ideas should not keep expanding this doc. | Existing backup/activity coverage already done for Events. | Keep as archive/reference only. Extract no active product work except deferred race-context ideas. |
| `events-release-scope.md` | Delete or archive from active roadmap | Release-prep archive for Events. | None. | Keep as historical archive or move to an archive folder later. |
| `future-roadmap-plan.md` | Delete or archive | Superseded by this convergence doc. | Outdated ordering bakes in Weekly Report after Progress. | Mark superseded; do not use as current ordering. |
| `next-features-plan.md` | Delete or archive | Superseded by this convergence doc. | Outdated ranking bakes in Progress -> Weekly Report -> Navigation. | Mark superseded; do not use as current ordering. |

## Ideas Without Dedicated Active Specs

| Idea | Classification | Decision |
| --- | --- | --- |
| Templates / reusable routines | Needs prerequisite | Defer until copy-last-week and current planner reuse are proven insufficient. Requires repository workflow cleanup and likely backup schema work. |
| Context model / memory layer | Needs prerequisite | Worth exploring as architecture, not as a feature. Start with deterministic derived state and explicit user-owned persisted context only when needed. |
| Contextual planning assistance | Needs prerequisite | Promising long-term direction if it stays inspectable and non-prescriptive. Should begin as deterministic suggestions or organization aids, not AI-generated plans. |
| Adaptive weekly organization | Needs prerequisite | Potentially high leverage after navigation and repository cleanup. Must never reorder or change the plan without clear user intent. |
| On-device / hybrid AI experiments | Defer indefinitely | Strategic research direction only. Requires privacy, fallback, model boundary and data-export decisions. |
| Race prep checklist | Defer indefinitely | Emotionally appealing but high coaching drift. It implies prescribed preparation logic Hermes does not currently own. |
| Race-prep recommendations | Delete from active roadmap | Violates the calm planner boundary unless the product explicitly becomes coaching software. |
| Structured distance, target time, location | Defer indefinitely | Could fit Events later, but only after there is a UI that uses the fields. Otherwise it is schema inventory. |
| PR / pace calculator | Delete from active roadmap | Performance-tool drift. Low leverage for a weekly planning app and likely to pull Hermes toward analytics claims. |
| Year in review | Defer indefinitely | Pleasant reflection, but low execution leverage compared with Progress/Report. Build only after reporting is mature. |
| Advanced recognition / race-specific trophies | Defer indefinitely | Trophy expansion should follow real user behavior, not invent new loops. |
| Saved report history | Delete from v1 | Adds schema and backup cost while generated reports already serve the core use case. |
| PDF/image report export | Defer indefinitely | More implementation and QA cost than text sharing; consider only if plain text sharing fails. |
| Coach sync / social sharing | Delete from active roadmap | Conflicts with offline-first and no-account identity. |
| Per-workout/event reminders | Defer indefinitely | Too much scheduling/schema complexity before a simple weekly reminder proves useful. |
| Broad analytics | Delete from active roadmap | Product-learning value does not outweigh privacy and identity cost right now. |

## Consolidation Recommendations

1. Use this document as the roadmap decision layer. Older roadmap/index docs should link here and stop asserting order.
2. Reconcile Progress docs after the current Progress release. `progress-screen-spec.md`, the coaching-summary design and old implementation plans should become either shipped notes or archives.
3. Treat Weekly Report as one local generated-report feature. Keep PDF/image export, saved history, coach presets, comparisons and notes integration out of v1.
4. Split "notes and perceived effort" before it becomes active. A workout note is lightweight context; perceived effort is a stronger training signal and should be a separate decision.
5. Split telemetry into "crash reporting" and "analytics". They have different privacy and product implications.
6. Fold micro-interactions into per-feature acceptance criteria instead of tracking them as a release.
7. Keep Events specs as shipped references. Do not use them as a container for race-prep, structured fields or recommendations.
8. Create a future Templates spec only if planning repetition becomes a clear problem after Progress/nav/report work.
9. Treat AI-native ideas as architecture and interaction principles until a concrete experiment passes the calm/offline/trust gates.
10. Before any model-assisted feature, define a context boundary: what data is read, what is stored, what is exportable, what is deletable and what happens offline.

## Delete, Defer Or Archive List

Archive from active roadmap:

- `events-release-scope.md`
- `race-events-spec.md`
- `future-roadmap-plan.md`
- `next-features-plan.md`
- old Progress implementation plans once Progress ships

Defer indefinitely:

- templates/reusable routines
- on-device or hybrid AI experiments
- structured race distance, target time and location
- race-specific trophies and advanced recognition
- year in review
- PDF/image report export
- per-workout or race-event reminders

Delete from active product strategy:

- race-prep recommendations
- race prep checklist as a prescribed training workflow
- PR or pace calculator
- social feed, coach sync or account-based sharing
- readiness scores, performance predictions or medical/training claims
- broad analytics as a product-learning dependency
- saved report history for Weekly Report v1
- AI chatbot shell as a primary product surface
- automatic plan optimization without explicit user control

## Recommended Next Three Releases

### Release 1: Progress Closeout

Goal: ship Progress as a stable reflection surface.

Scope:

- Finish Progress spacing, empty/partial states and copy.
- Keep Recent Activity as a preview and Activity as the full history.
- Keep all Progress insights read-only and generated from existing data.
- Add small accessibility fixes found while testing Progress.
- Do not add schema, recommendations, readiness scoring or saved settings.

Architecture alongside:

- Split oversized Progress UI blocks only where it reduces review cost.
- Keep Progress aggregation test coverage focused on existing-data behavior.

### Release 2: Browse And Navigation Foundation

Goal: reduce top-level navigation crowding and stop app-shell state from growing around manual destination flags.

Scope:

- Introduce a stable top-level route model.
- Add Browse root.
- Move Activity, Categories, Trophies, Backup and Settings into Browse.
- Keep Week, Progress and Events as direct destinations unless explicit product review changes that.
- Preserve trophy snackbar routing and manage-categories return flows.

Architecture alongside:

- Extract app shell route state away from ad hoc flags where practical.
- Start untangling `core.ui` dependencies on feature types if Browse/shared components expose the problem.
- Preserve explicit route/context boundaries so future contextual assistance has clear surfaces to read from and navigate to.
- Keep this as a public navigation change with explicit review before coding.

### Release 3: Weekly Report v1

Goal: generate a local, shareable weekly summary from existing data.

Scope:

- Selected week report.
- Counts for workouts, completed workouts, rest, busy, sick and race events.
- Category breakdown.
- Day-by-day list.
- Android share-sheet text.
- Activity log for share intent only.
- No report persistence, saved history, PDF/image export or coach sync.

Architecture alongside:

- Reuse or extract Progress aggregation helpers instead of duplicating weekly summary logic.
- Split `ActivityUiFormatter` before adding many new report/share action mappings if the file remains monolithic.
- Keep share text generated through localized string resources/providers.
- Keep the report generation contract deterministic and testable before considering AI-assisted wording.

### Post-Release Research Track: Context Model Spike

Goal: define what "context" means in Hermes before building AI-assisted behavior.

Scope:

- Inventory existing context sources: weekly items, events, categories, Activity, trophies, settings and backups.
- Decide which context is derived, user-authored, persisted, exportable and deletable.
- Define restraint rules for any future suggestions or reminders.
- Identify on-device-first and hybrid fallback constraints.
- Produce architecture notes only; do not ship user-facing AI behavior from this spike.

## Architecture Work To Schedule

These are not separate product features, but they decide how expensive future features become.

| Architecture issue | Why it matters | Do before / alongside |
| --- | --- | --- |
| App shell navigation state is too manual | Browse, report entry points, settings subroutes and trophy targeting will otherwise add more flags to `HermesAppContent`. | Release 2 navigation work. |
| `WeeklyTrainingViewModel` is too large | Notes, templates, reminders and new planner behaviors will keep increasing state-change complexity. | Before schema-heavy planner features. |
| `WeeklyTrainingRepository` mixes workflows and deprecated APIs | Templates and copy/reuse features need clean repository operations, not more UI orchestration. | Before templates or notes/effort. |
| `ActivityUiFormatter` is monolithic | Notes, reminders, report sharing and templates all need Activity copy. | Before adding several new action types. |
| `DemoDataSeeder` is oversized | Manual QA for Progress/report/navigation depends on rich and maintainable fixtures. | Incrementally during QA-heavy releases. |
| Large Compose screen files need gradual splits | Progress, Events and future Browse/Report screens are already large enough to slow review. | Opportunistically when touching each screen. |
| `core.ui` depends on feature types | Shared UI should not drag feature models upward; Browse/shared cards may expose this coupling. | During Browse/shared component cleanup. |
| Build/release docs are thin | Future schema/localization/navigation work needs consistent verification. | Continuous; before the next schema migration. |
| No explicit context model boundary | Future AI/context-aware behavior needs a stable definition of readable context, persisted memory and export/delete behavior. | Before notes, intelligent reminders, templates or model-assisted summaries. |
| Deterministic aggregation and AI output are not separated | Model-assisted wording should not own product truth; deterministic state should remain source of truth. | Before any AI-assisted report, summary or planning aid. |
| Privacy/consent architecture is undefined for AI | Hybrid or remote reasoning requires clear opt-in, redaction and fallback rules. | Before any online AI experiment. |

## Architecture Unlocks For Contextual Intelligence

Future intelligent behavior should depend on these unlocks, not bypass them:

- A typed context model that can describe week state, event horizon, category mix, recent changes and user-authored notes without exposing UI internals.
- A deterministic summary layer that produces stable facts before any model-assisted language.
- A memory policy covering what can be stored, cleared, exported and imported.
- A reasoning boundary that can run as no-op, deterministic-only, on-device or remote-assisted without changing feature screens.
- A redaction layer for any future online reasoning path.
- A user-control model for accepting, dismissing and editing suggestions.
- Tests that assert facts and constraints separately from generated phrasing.

## Skeptical Maintainer Notes

Emotionally appealing but low leverage:

- Year in review, race-specific trophies, micro-interactions as a release, PDF/image reports, PR calculator.

Likely identity violations:

- Race-prep recommendations, readiness scores, performance predictions, coach sync, social feed, broad analytics, chatbot-first UX.

Coaching/medical/analytics drift:

- Perceived effort if framed as training intensity analysis.
- Race prep checklist if it prescribes preparation.
- Weekly Report if it becomes coach-sync infrastructure instead of local text sharing.
- Analytics if it starts tracking detailed training behavior.
- AI assistance if it starts writing plans, ranking user behavior or creating pressure instead of clarifying existing context.

Persisted schema too early:

- Notes/effort before report demand is proven.
- Templates before repository workflows are cleaner.
- Per-item reminders before simple weekly reminder is tested.
- Saved report history before generated reports prove useful.
- Structured event fields before UI uses them.
- Persisted memory before export/import/delete semantics are designed.
- AI prompts or model logs before privacy and redaction boundaries exist.

## Open Questions

1. Should Browse/navigation foundation be the next major release after Progress closeout, ahead of Weekly Report?
2. Is Weekly Report primarily for self-review, or is coach sharing a core use case?
3. Is local Android share-sheet text enough for Weekly Report v1?
4. Is Firebase acceptable for crash-only reliability work, or should telemetry stay out entirely?
5. Should note-only context be considered separately from perceived effort?
6. Which old specs should be physically moved to an archive folder, instead of only marked superseded?
7. Should Events remain a direct destination after Browse ships, or should it eventually become a Browse destination too?
8. Is "calm offline weekly planner" still the product boundary, or should Hermes intentionally expand toward training diary/reporting?
9. What is the first acceptable AI-adjacent experiment: context-model architecture only, deterministic suggestions, on-device summarization, or remote-assisted report wording?
10. What context is allowed to become memory, and what must remain ephemeral derived state?
11. Should any online AI path be allowed at all, or should Hermes constrain itself to local/on-device intelligence until the privacy story is stronger?
12. What user controls are mandatory for future suggestions: dismiss, edit, explain, disable, clear memory, export memory?
