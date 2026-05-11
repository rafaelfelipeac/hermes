# Relative Date Summaries Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Use `today`, `tomorrow`, and `yesterday` in shared user-facing date summaries wherever those relative labels are more readable than numeric offsets.

**Architecture:** Add one small shared formatter in `core/strings` that turns a `LocalDate` or day offset into a localized relative label, then wire Activity and Progress to use that formatter instead of each screen hardcoding its own rule. Keep the fallback behavior unchanged for dates outside the relative window.

**Tech Stack:** Kotlin, Compose, Android string resources, JVM unit tests.

---

### Task 1: Add shared relative-date formatting and localization

**Files:**
- Create: `app/src/main/java/com/rafaelfelipeac/hermes/core/strings/RelativeDateFormatter.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-ar/strings.xml`
- Modify: `app/src/main/res/values-de/strings.xml`
- Modify: `app/src/main/res/values-es/strings.xml`
- Modify: `app/src/main/res/values-fr/strings.xml`
- Modify: `app/src/main/res/values-hi/strings.xml`
- Modify: `app/src/main/res/values-it/strings.xml`
- Modify: `app/src/main/res/values-ja/strings.xml`
- Modify: `app/src/main/res/values-pt-rBR/strings.xml`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/core/strings/RelativeDateFormatterTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class RelativeDateFormatterTest {
    @Test
    fun `relativeDateText returns today tomorrow yesterday and formatted fallback`() {
        val today = LocalDate.of(2026, 5, 11)
        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

        assertEquals(
            "Today",
            relativeDateText(
                date = today,
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "Tomorrow",
            relativeDateText(
                date = today.plusDays(1),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "Yesterday",
            relativeDateText(
                date = today.minusDays(1),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "May 14",
            relativeDateText(
                date = today.plusDays(3),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests com.rafaelfelipeac.hermes.core.strings.RelativeDateFormatterTest`
Expected: fail because `relativeDateText` does not exist yet.

- [ ] **Step 3: Write the shared formatter and resource string**

```kotlin
fun relativeDateText(
    date: LocalDate,
    today: LocalDate,
    todayLabel: String,
    tomorrowLabel: String,
    yesterdayLabel: String,
    formatter: DateTimeFormatter,
): String =
    when (date) {
        today -> todayLabel
        today.plusDays(1) -> tomorrowLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(formatter)
    }
```

Add `<string name="activity_tomorrow">Tomorrow</string>` beside the existing `activity_today` / `activity_yesterday` entries in every locale file, with the matching localized translation in each supported locale.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests com.rafaelfelipeac.hermes.core.strings.RelativeDateFormatterTest`
Expected: PASS.

### Task 2: Wire Activity and Progress to the shared formatter

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/ActivityScreen.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressScreen.kt`
- Test: `app/src/test/java/com/rafaelfelipeac/hermes/features/activity/presentation/ActivityScreenTest.kt` if the screen tests cover section titles, otherwise add a focused formatter test in the shared formatter file

- [ ] **Step 1: Replace screen-local relative-date branches with the shared helper**

```kotlin
val header = relativeDateText(
    date = section.date,
    today = today,
    todayLabel = todayLabel,
    tomorrowLabel = tomorrowLabel,
    yesterdayLabel = yesterdayLabel,
    formatter = dayFormatter,
)
```

```kotlin
private fun daysUntilText(daysUntil: Int): String {
    return when (daysUntil) {
        0 -> stringResource(R.string.activity_today)
        1 -> stringResource(R.string.activity_tomorrow)
        else -> stringResource(R.string.progress_days_until, daysUntil)
    }
}
```

- [ ] **Step 2: Run the focused unit test and the module test suite**

Run:
`./gradlew :app:testDebugUnitTest --tests com.rafaelfelipeac.hermes.core.strings.RelativeDateFormatterTest`
`./gradlew :app:testDebugUnitTest`

Expected: formatter test passes, and the app unit test suite stays green.

- [ ] **Step 3: Update LEARNING.md with the shared-formatting decision**

Capture the lesson that relative day words now belong in one shared formatter so screens do not diverge on `today` / `tomorrow` / `yesterday` wording.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/rafaelfelipeac/hermes/core/strings/RelativeDateFormatter.kt \
  app/src/main/java/com/rafaelfelipeac/hermes/features/activity/presentation/ActivityScreen.kt \
  app/src/main/java/com/rafaelfelipeac/hermes/features/progress/presentation/ProgressScreen.kt \
  app/src/main/res/values*/strings.xml \
  app/src/test/java/com/rafaelfelipeac/hermes/core/strings/RelativeDateFormatterTest.kt \
  docs/superpowers/plans/2026-05-11-relative-date-summaries.md \
  LEARNING.md
git commit -m "feat: share relative date labels"
```

