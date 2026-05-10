# Progress Coaching Summary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework Progress into a weekly coaching summary with a neutral top readout, clearer charts, compact support cards for next focus/upcoming event/trophy, and a concise activity preview.

**Architecture:** Keep Progress read-only and continue building a single `ProgressState` from existing workouts, categories, trophy progress and activity records. Expand the presentation state with coaching-focused fields, then update Compose to render the new hierarchy. Keep all UI copy in string resources and all sizing in `Dimens`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Hilt ViewModel, StateFlow, JUnit, Android string resources.

---

## File Structure

- Modify `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressState.kt`
  - Add weekly readout, next focus, trend insight and training mix insight models.
  - Keep existing models where they still serve charts and support cards.
- Modify `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilder.kt`
  - Build coaching readout, next focus, upcoming event, chart insights and 3-item activity preview.
- Modify `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressScreen.kt`
  - Replace summary-card-first layout with the coaching screen structure.
  - Keep Compose functions private and focused by section.
- Modify `app/src/main/java/com/rafaelfelipeac/hermes/core/ui/theme/Dimens.kt`
  - Add named dimensions for chart axis labels, stacked bars, progress bars and activity dots.
- Modify all string resource files:
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/values-ar/strings.xml`
  - `app/src/main/res/values-de/strings.xml`
  - `app/src/main/res/values-es/strings.xml`
  - `app/src/main/res/values-fr/strings.xml`
  - `app/src/main/res/values-hi/strings.xml`
  - `app/src/main/res/values-it/strings.xml`
  - `app/src/main/res/values-ja/strings.xml`
  - `app/src/main/res/values-pt-rBR/strings.xml`
- Modify `app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilderTest.kt`
  - Cover readout, next focus, upcoming event and insights.
- Modify `app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressViewModelTest.kt`
  - Cover activity preview limit and integrated state.
- Update `LEARNING.md`
  - Add a short post-hoc note after implementation.

Before implementation, check `git status --short`. This branch currently has unrelated unstaged edits in `ProgressScreen.kt` and `docs/specs/progress-screen-spec.md`; preserve them and do not revert them.

## Task 1: Expand Progress Builder Tests

**Files:**
- Modify: `app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilderTest.kt`

- [ ] **Step 1: Add test for weekly readout and next focus**

Append this test inside `ProgressSummaryBuilderTest`:

```kotlin
@Test
fun buildProgressState_buildsWeeklyReadoutWithNextIncompleteWorkout() {
    val workouts =
        listOf(
            workout(1L, currentWeek, DayOfWeek.MONDAY, EventType.WORKOUT, isCompleted = true, categoryId = 2L),
            workout(2L, currentWeek, DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 3L),
            workout(3L, currentWeek, DayOfWeek.FRIDAY, EventType.WORKOUT, isCompleted = false, categoryId = 2L),
        )

    val state =
        buildProgressState(
            workouts = workouts,
            categories = categories,
            trophyCards = emptyList(),
            recentActivities = emptyList(),
            today = today,
            currentWeekStart = currentWeek,
        )

    assertEquals(3, state.weeklyReadout.plannedWorkouts)
    assertEquals(1, state.weeklyReadout.completedWorkouts)
    assertEquals(33, state.weeklyReadout.completionPercent)
    assertEquals(2L, state.weeklyReadout.nextFocus?.id)
    assertEquals("Workout 2", state.weeklyReadout.nextFocus?.title)
    assertEquals(1, state.weeklyReadout.nextFocus?.daysUntil)
}
```

- [ ] **Step 2: Add test for hiding next focus when current week is complete**

Append:

```kotlin
@Test
fun buildProgressState_hidesNextFocusWhenCurrentWeekHasNoIncompleteWorkout() {
    val workouts =
        listOf(
            workout(1L, currentWeek, DayOfWeek.MONDAY, EventType.WORKOUT, isCompleted = true, categoryId = 2L),
            workout(2L, currentWeek.plusWeeks(1), DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 3L),
        )

    val state =
        buildProgressState(
            workouts = workouts,
            categories = categories,
            trophyCards = emptyList(),
            recentActivities = emptyList(),
            today = today,
            currentWeekStart = currentWeek,
        )

    assertNull(state.weeklyReadout.nextFocus)
}
```

- [ ] **Step 3: Add test for upcoming event remaining separate from next focus**

Append:

```kotlin
@Test
fun buildProgressState_keepsUpcomingEventSeparateFromNextFocus() {
    val workouts =
        listOf(
            workout(1L, currentWeek, DayOfWeek.WEDNESDAY, EventType.WORKOUT, isCompleted = false, categoryId = 2L),
            workout(2L, currentWeek.plusWeeks(1), DayOfWeek.SATURDAY, RACE_EVENT, isCompleted = false, categoryId = 3L),
        )

    val state =
        buildProgressState(
            workouts = workouts,
            categories = categories,
            trophyCards = emptyList(),
            recentActivities = emptyList(),
            today = today,
            currentWeekStart = currentWeek,
        )

    assertEquals(1L, state.weeklyReadout.nextFocus?.id)
    assertEquals(2L, state.upcomingEvent?.id)
    assertEquals(11, state.upcomingEvent?.daysUntil)
}
```

- [ ] **Step 4: Add test for weekly and mix insights**

Append:

```kotlin
@Test
fun buildProgressState_buildsTrendAndTrainingMixInsights() {
    val workouts =
        week(currentWeek.minusWeeks(2), completed = 3, pending = 0, categoryId = 2L) +
            week(currentWeek.minusWeeks(1), completed = 4, pending = 0, categoryId = 3L) +
            week(currentWeek, completed = 6, pending = 1, categoryId = 2L)

    val state =
        buildProgressState(
            workouts = workouts,
            categories = categories,
            trophyCards = emptyList(),
            recentActivities = emptyList(),
            today = today,
            currentWeekStart = currentWeek,
        )

    assertEquals(ProgressWeeklyTrendInsightKind.CURRENT_WEEK_HEAVIEST, state.weeklyTrendInsight?.kind)
    assertEquals(ProgressTrainingMixInsightKind.BALANCED, state.trainingMixInsight?.kind)
}
```

- [ ] **Step 5: Run builder tests and verify they fail for missing fields**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*ProgressSummaryBuilderTest" -q
```

Expected: compilation fails because `weeklyReadout`, `ProgressWeeklyTrendInsightKind`, and `ProgressTrainingMixInsightKind` do not exist yet.

- [ ] **Step 6: Commit failing tests**

```bash
git add app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilderTest.kt
git commit -m "test: cover progress coaching summary state"
```

## Task 2: Add Coaching Presentation State And Builder Logic

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressState.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilder.kt`

- [ ] **Step 1: Add state models**

In `ProgressState.kt`, update `ProgressState` and append the new models:

```kotlin
data class ProgressState(
    val weeklyReadout: ProgressWeeklyReadoutUi = ProgressWeeklyReadoutUi(),
    val summaryCards: List<ProgressSummaryCardUi> = emptyList(),
    val thisWeek: ProgressWeekSnapshotUi = ProgressWeekSnapshotUi(),
    val weeklyTrend: List<ProgressWeekBarUi> = emptyList(),
    val weeklyTrendInsight: ProgressWeeklyTrendInsightUi? = null,
    val categoryDistribution: List<ProgressCategoryShareUi> = emptyList(),
    val trainingMixInsight: ProgressTrainingMixInsightUi? = null,
    val trophyHighlight: FeaturedTrophyUi? = null,
    val recentActivities: List<ActivityItemUi> = emptyList(),
    val upcomingEvent: ProgressUpcomingEventUi? = null,
    val emptyReason: ProgressEmptyReason? = ProgressEmptyReason.NO_WEEKLY_HISTORY,
)

data class ProgressWeeklyReadoutUi(
    val plannedWorkouts: Int = 0,
    val completedWorkouts: Int = 0,
    val completionPercent: Int = 0,
    val nextFocus: ProgressNextFocusUi? = null,
)

data class ProgressNextFocusUi(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val daysUntil: Int,
)

data class ProgressWeeklyTrendInsightUi(
    val kind: ProgressWeeklyTrendInsightKind,
)

enum class ProgressWeeklyTrendInsightKind {
    CURRENT_WEEK_HEAVIEST,
}

data class ProgressTrainingMixInsightUi(
    val kind: ProgressTrainingMixInsightKind,
    val categoryName: String? = null,
)

enum class ProgressTrainingMixInsightKind {
    BALANCED,
    DOMINANT_CATEGORY,
}
```

- [ ] **Step 2: Build weekly readout and insights**

In `ProgressSummaryBuilder.kt`, add these functions before `buildSummaryCards`:

```kotlin
private fun buildWeeklyReadout(
    thisWeek: ProgressWeekSnapshotUi,
    workouts: List<Workout>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressWeeklyReadoutUi {
    return ProgressWeeklyReadoutUi(
        plannedWorkouts = thisWeek.plannedWorkouts,
        completedWorkouts = thisWeek.completedWorkouts,
        completionPercent = thisWeek.completionPercent,
        nextFocus = buildNextFocus(workouts, today, currentWeekStart),
    )
}

private fun buildNextFocus(
    workouts: List<Workout>,
    today: LocalDate,
    currentWeekStart: LocalDate,
): ProgressNextFocusUi? {
    return workouts
        .asSequence()
        .filter {
            it.weekStartDate == currentWeekStart &&
                it.dayOfWeek != null &&
                it.eventType == WORKOUT &&
                !it.isCompleted
        }
        .map { workout ->
            val workoutDate = workout.weekStartDate.plusDays((requireNotNull(workout.dayOfWeek).value - 1).toLong())
            workout to workoutDate
        }
        .filter { (_, workoutDate) -> !workoutDate.isBefore(today) }
        .sortedWith(compareBy<Pair<Workout, LocalDate>> { it.second }.thenBy { it.first.order })
        .firstOrNull()
        ?.let { (workout, workoutDate) ->
            ProgressNextFocusUi(
                id = workout.id,
                title = workout.type.ifBlank { workout.description }.ifBlank { EMPTY },
                date = workoutDate,
                daysUntil = ChronoUnit.DAYS.between(today, workoutDate).toInt(),
            )
        }
}

private fun buildWeeklyTrendInsight(weeklyTrend: List<ProgressWeekBarUi>): ProgressWeeklyTrendInsightUi? {
    val currentWeek = weeklyTrend.firstOrNull { it.isCurrentWeek } ?: return null
    val maxPlanned = weeklyTrend.maxOfOrNull { it.plannedWorkouts } ?: return null
    if (currentWeek.plannedWorkouts <= 0 || currentWeek.plannedWorkouts < maxPlanned) return null

    return ProgressWeeklyTrendInsightUi(ProgressWeeklyTrendInsightKind.CURRENT_WEEK_HEAVIEST)
}

private fun buildTrainingMixInsight(items: List<ProgressCategoryShareUi>): ProgressTrainingMixInsightUi? {
    val top = items.firstOrNull() ?: return null
    return when {
        top.sharePercent >= DOMINANT_CATEGORY_PERCENT ->
            ProgressTrainingMixInsightUi(
                kind = ProgressTrainingMixInsightKind.DOMINANT_CATEGORY,
                categoryName = top.name,
            )
        items.size >= BALANCED_CATEGORY_MIN_COUNT && top.sharePercent <= BALANCED_CATEGORY_MAX_TOP_PERCENT ->
            ProgressTrainingMixInsightUi(ProgressTrainingMixInsightKind.BALANCED)
        else -> null
    }
}
```

Add constants near the existing constants:

```kotlin
private const val BALANCED_CATEGORY_MIN_COUNT = 3
private const val BALANCED_CATEGORY_MAX_TOP_PERCENT = 35
private const val DOMINANT_CATEGORY_PERCENT = 50
```

- [ ] **Step 3: Wire the new fields**

In `buildProgressState`, compute and return the new values:

```kotlin
val weeklyReadout =
    buildWeeklyReadout(
        thisWeek = thisWeek,
        workouts = workouts,
        today = today,
        currentWeekStart = currentWeekStart,
    )
val weeklyTrendInsight = buildWeeklyTrendInsight(weeklyTrend)
val trainingMixInsight = buildTrainingMixInsight(categoryDistribution)
```

Then include:

```kotlin
weeklyReadout = weeklyReadout,
weeklyTrendInsight = weeklyTrendInsight,
trainingMixInsight = trainingMixInsight,
recentActivities = recentActivities.take(RECENT_ACTIVITY_PREVIEW_LIMIT),
```

Rename the builder constant:

```kotlin
private const val RECENT_ACTIVITY_PREVIEW_LIMIT = 3
```

- [ ] **Step 4: Run builder tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*ProgressSummaryBuilderTest" -q
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit state and builder**

```bash
git add app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressState.kt app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilder.kt app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressSummaryBuilderTest.kt
git commit -m "feat: build progress coaching summary state"
```

## Task 3: Update ViewModel Test For 3-Item Activity Preview

**Files:**
- Modify: `app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressViewModelTest.kt`

- [ ] **Step 1: Update the integrated state assertion**

In `state_summarizesCurrentWeekAndRecentActivity`, add:

```kotlin
assertEquals(2, state.weeklyReadout.plannedWorkouts)
assertEquals(1, state.weeklyReadout.completedWorkouts)
assertEquals(3L, state.weeklyReadout.nextFocus?.id)
```

- [ ] **Step 2: Add an activity preview limit test**

Append:

```kotlin
@Test
fun state_limitsRecentActivityPreviewToThreeItems() =
    runTest(mainDispatcherRule.testDispatcher) {
        val currentWeek = LocalDate.now().with(previousOrSame(DayOfWeek.MONDAY))
        val actions =
            (1L..5L).map { id ->
                action(
                    id = id,
                    actionType = UserActionType.UPDATE_WORKOUT,
                    entityType = UserActionEntityType.WORKOUT,
                    timestamp = id * 1_000L,
                )
            }
        val viewModel =
            createViewModel(
                workouts = listOf(workout(1L, currentWeek, DayOfWeek.MONDAY, isCompleted = true, categoryId = 2L)),
                actions = actions,
            )

        viewModel.state.test {
            awaitItem()
            val state = awaitItem()

            assertEquals(3, state.recentActivities.size)
            assertEquals(5L, state.recentActivities.first().id)
            assertEquals(3L, state.recentActivities.last().id)

            cancelAndIgnoreRemainingEvents()
        }
    }
```

- [ ] **Step 3: Run ViewModel tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*ProgressViewModelTest" -q
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit ViewModel tests**

```bash
git add app/src/test/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressViewModelTest.kt
git commit -m "test: cover progress activity preview limit"
```

## Task 4: Add Localized Progress Strings

**Required sub-skill before editing strings:** Use `hermes-localization-check`.

**Files:**
- Modify all `app/src/main/res/values*/strings.xml` files.

- [ ] **Step 1: Add English strings**

In `app/src/main/res/values/strings.xml`, update Progress labels:

```xml
<string name="progress_section_weekly_readout">Weekly readout</string>
<string name="progress_readout_on_track">You are on track</string>
<string name="progress_readout_completed">%1$d of %2$d planned workouts complete</string>
<plurals name="progress_readout_workouts_left">
    <item quantity="one">%1$d planned workout left</item>
    <item quantity="other">%1$d planned workouts left</item>
</plurals>
<string name="progress_section_training_mix">Training mix</string>
<string name="progress_weekly_chart_caption">100% means every planned workout in that week was completed.</string>
<string name="progress_weekly_chart_count">%1$d/%2$d</string>
<string name="progress_weekly_chart_current_label">Now</string>
<string name="progress_weekly_chart_previous_label">Last</string>
<string name="progress_weekly_insight_heaviest">This is your heaviest planned week in the last 8 weeks.</string>
<string name="progress_training_mix_caption">Completed workouts over the last 8 weeks. Color shows category; width shows share.</string>
<string name="progress_training_mix_balanced">Your mix is balanced across recent categories.</string>
<string name="progress_training_mix_dominant">%1$s is dominating this window.</string>
<string name="progress_section_supporting_progress">Supporting progress</string>
<string name="progress_support_next_focus">Next focus</string>
<string name="progress_support_upcoming_event">Upcoming event</string>
<string name="progress_support_trophy">Trophy</string>
<string name="progress_no_support_cards">Keep planning workouts to reveal the next focus.</string>
<string name="progress_recent_activity_caption">Latest changes from your training history.</string>
```

- [ ] **Step 2: Add translations to every localized file**

Add equivalent keys to each localized file. Preserve placeholder parity exactly:

- `%1$d`, `%2$d` in `progress_readout_completed`
- plural `%1$d` in `progress_readout_workouts_left`
- `%1$d`, `%2$d` in `progress_weekly_chart_count`
- `%1$s` in `progress_training_mix_dominant`

Use proper translations, not English fallback text.

- [ ] **Step 3: Run localization and resource checks**

Run:

```bash
./gradlew :app:compileDebugKotlin -q
./gradlew :app:testDebugUnitTest --tests "*ProgressSummaryBuilderTest" --tests "*ProgressViewModelTest" -q
```

Expected: both commands finish with `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit strings**

```bash
git add app/src/main/res/values*/strings.xml
git commit -m "feat: add progress coaching strings"
```

## Task 5: Rebuild Progress Compose Layout

**Required sub-skill before editing Compose:** Use `hermes-compose-guardrails`.

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressScreen.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/core/ui/theme/Dimens.kt`

- [ ] **Step 1: Add dimensions**

In `Dimens.kt`, add named values near existing Progress dimensions:

```kotlin
val ProgressReadoutBarHeight = 10.dp
val ProgressTrendAxisWidth = 34.dp
val ProgressTrendCountMinHeight = 14.dp
val ProgressTrainingMixBarHeight = 18.dp
val ProgressActivityDotSize = 10.dp
val ProgressSupportCardMinHeight = 88.dp
```

- [ ] **Step 2: Replace top summary cards with weekly readout**

In `ProgressContent`, replace:

```kotlin
item {
    ProgressSummaryCards(cards = state.summaryCards)
}

item {
    ProgressSection(
        title = stringResource(R.string.progress_section_this_week),
    ) {
        ProgressThisWeek(snapshot = state.thisWeek)
    }
}
```

with:

```kotlin
item {
    ProgressWeeklyReadout(readout = state.weeklyReadout)
}
```

Keep `ProgressSummaryCards` only if another task still uses it; otherwise delete `ProgressSummaryCards`, `summaryCardSupportingText`, and `cardTitleRes` in the same commit.

- [ ] **Step 3: Add weekly readout composable**

Add:

```kotlin
@Composable
private fun ProgressWeeklyReadout(readout: ProgressWeeklyReadoutUi) {
    ProgressSection(title = stringResource(R.string.progress_section_weekly_readout)) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(
                text = stringResource(R.string.progress_readout_on_track),
                style = typography.titleMedium,
                color = colorScheme.onSurface,
            )
            Text(
                text =
                    stringResource(
                        R.string.progress_readout_completed,
                        readout.completedWorkouts,
                        readout.plannedWorkouts,
                    ),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ProgressReadoutBarHeight)
                        .clip(shapes.small)
                        .background(colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((readout.completionPercent / 100f).coerceIn(0f, 1f))
                            .background(colorScheme.primary),
                )
            }
            readout.nextFocus?.let { focus ->
                Text(
                    text = stringResource(R.string.progress_support_next_focus) + ": " + focus.title,
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
```

After adding this, import only the used `Dimens` names explicitly.

- [ ] **Step 4: Update weekly chart**

Refactor `ProgressWeeklyTrend` to:

- Add a left axis column with `100%`, `50%`, `0%`.
- Keep bar height based on `completionPercent`.
- Show `completed/planned` under current and previous weeks.
- Show weekday/week labels for the remaining bars.
- Render `progress_weekly_chart_caption`.
- Render `state.weeklyTrendInsight` below the chart when present.

Change the call site to:

```kotlin
ProgressWeeklyTrend(
    weeks = state.weeklyTrend,
    insight = state.weeklyTrendInsight,
    locale = locale,
)
```

- [ ] **Step 5: Update category section to Training mix**

Change the section title to `progress_section_training_mix`.

Inside `ProgressCategoryDistribution`, add a stacked horizontal bar before the list:

```kotlin
Row(
    modifier =
        Modifier
            .fillMaxWidth()
            .height(ProgressTrainingMixBarHeight)
            .clip(shapes.small)
            .background(colorScheme.surfaceVariant),
) {
    items.forEach { item ->
        Box(
            modifier =
                Modifier
                    .weight(item.sharePercent.coerceAtLeast(1).toFloat())
                    .fillMaxHeight()
                    .background(categoryAccentColor(item.colorId)),
        )
    }
}
```

Render `progress_training_mix_caption` above the rows and the optional `trainingMixInsight` below the rows.

- [ ] **Step 6: Add support cards row**

Replace separate trophy/upcoming sections with one `Supporting progress` section. It should contain compact cards for:

- `state.weeklyReadout.nextFocus`
- `state.upcomingEvent`
- `state.trophyHighlight`

Use two cards per row with `defaultMinSize(minHeight = ProgressSupportCardMinHeight)`. If no cards exist, show `progress_no_support_cards`.

- [ ] **Step 7: Compact recent activity**

Update `ProgressRecentActivity` to a timeline-style preview:

- Use `items.take(3)`.
- Use a row with time, dot, and title/subtitle.
- Use `ProgressActivityDotSize`.
- Keep `View all`.
- Keep `ActivityItemUi.title` and `subtitle` for this implementation; do not parse action metadata in the composable.

- [ ] **Step 8: Run Compose compile**

Run:

```bash
./gradlew :app:compileDebugKotlin -q
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit Compose layout**

```bash
git add app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressScreen.kt app/src/main/java/com/rafaelfelipeac/hermes/core/ui/theme/Dimens.kt
git commit -m "feat: redesign progress coaching layout"
```

## Task 6: Final Verification And Learning Note

**Required sub-skills before finishing:** Use `hermes-test-gap-check`, `hermes-compose-guardrails`, `hermes-localization-check`, and `superpowers:verification-before-completion`.

**Files:**
- Modify: `LEARNING.md`

- [ ] **Step 1: Add learning note**

Append one note to `LEARNING.md`:

```markdown
- Progress coaching summaries work best when interpretation and evidence stay separate: the top card gives the readout, while chart captions, counts and optional insights explain why without turning the screen into a dense report.
```

- [ ] **Step 2: Run focused tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*ProgressSummaryBuilderTest" --tests "*ProgressViewModelTest" -q
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run compile and static checks**

Run:

```bash
./gradlew :app:compileDebugKotlin -q
./gradlew :app:lintDebug --quiet
./gradlew ktlintCheck
```

Expected: all commands complete successfully.

- [ ] **Step 4: Optional visual check**

If an emulator is available, run:

```bash
./gradlew installDebug
```

Open Progress and verify:

- Weekly readout appears first.
- Top card uses neutral styling.
- Weekly chart has vertical scale labels and visible counts.
- Training mix has a stacked category bar.
- Next focus and upcoming event can appear separately.
- Recent activity shows at most 3 compact rows.

- [ ] **Step 5: Commit final verification note**

```bash
git add LEARNING.md
git commit -m "docs: capture progress coaching layout learning"
```

## Self-Review

Spec coverage:

- Weekly coaching readout: Task 2 and Task 5.
- Weekly chart scale and counts: Task 5.
- Training mix stacked bar and insight: Task 2 and Task 5.
- Upcoming event as separate support card: Task 1, Task 2 and Task 5.
- Compact recent activity: Task 3 and Task 5.
- Localization: Task 4.
- Tests and verification: Tasks 1, 3 and 6.

Type consistency:

- `ProgressWeeklyReadoutUi`, `ProgressNextFocusUi`, `ProgressWeeklyTrendInsightUi`, and `ProgressTrainingMixInsightUi` are introduced in Task 2 before UI code uses them in Task 5.
- `RECENT_ACTIVITY_PREVIEW_LIMIT` is introduced in Task 2 and covered by Task 3.

Scope:

- The plan does not add write-side state, Activity logging, race readiness scoring, chart dependencies, or navigation changes.
