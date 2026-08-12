# Personal Records PR UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh Personal Records so creation flows use a single FAB menu, family terminology is replaced with PR series language, and time entries use a wheel-style selector instead of a raw unit field.

**Architecture:** Keep the existing Personal Records screen shell and state flow intact. Update the shelf/detail UI so the root screen owns the creation menu, the family editor becomes a series editor with hidden default-unit selection, and the entry editor switches between a numeric input flow and a time wheel flow depending on the selected series metric. Preserve the current repository and ViewModel contracts unless the UI change forces a narrow adapter in the presentation layer.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android string resources, Compose UI tests

---

### Task 1: Replace the shelf FAB stack with one floating action menu

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsScreen.kt`
- Modify: `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsContentTest.kt`

- [ ] **Step 1: Write the failing test**

Add a UI test that asserts the Personal Records shelf exposes one FAB, that tapping it reveals the action choices for creating a PR series and adding a result, and that the old stacked family FABs are gone.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin -q`
Expected: fail because the new menu nodes do not exist yet.

- [ ] **Step 3: Write the minimal implementation**

Use the Weekly Training add-menu pattern: one FAB, an overlay scrim, and two action pills that open the series editor or the result editor.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew --no-daemon :app:compileDebugAndroidTestKotlin -q`
Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsScreen.kt app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsContentTest.kt
git commit -m "feat: update personal records action menu"
```

### Task 2: Hide unit fields and add the time wheel selector

**Files:**
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsScreen.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordValueFormatter.kt`
- Modify: `app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsViewModel.kt`
- Modify: `app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsContentTest.kt`

- [ ] **Step 1: Write the failing test**

Add a focused compose test or helper test that proves a TIME series entry uses hours/minutes/seconds inputs and that the stored value is converted to total seconds.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew --no-daemon :app:compileDebugKotlin -q`
Expected: fail until the time wheel and save path exist.

- [ ] **Step 3: Write the minimal implementation**

Remove the visible unit picker from the series editor and the entry editor for normal metrics. For TIME, render a three-column vertical picker that stores seconds and formats history exactly as the existing formatter already expects.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew --no-daemon :app:compileDebugKotlin -q`
Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsScreen.kt app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordValueFormatter.kt app/src/main/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsViewModel.kt app/src/androidTest/java/com/rafaelfelipeac/hermes/features/personalrecords/presentation/PersonalRecordsContentTest.kt
git commit -m "feat: add time picker to personal records"
```

### Task 3: Rename visible copy and propagate strings

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-*/strings.xml`

- [ ] **Step 1: Write the failing test**

Use the localization check to verify every locale keeps the updated Personal Records labels and there is no fallback English text in localized files.

- [ ] **Step 2: Run the check to verify it fails**

Run: `./gradlew --no-daemon :app:compileDebugKotlin -q`
Expected: fail until the renamed labels exist in every locale.

- [ ] **Step 3: Write the minimal implementation**

Rename the visible PR text from family-centric wording to PR series wording and keep the browse subtitle aligned with the new language.

- [ ] **Step 4: Run the check to verify it passes**

Run: `./gradlew --no-daemon :app:compileDebugKotlin -q`
Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-*/strings.xml
git commit -m "feat: rename personal records copy"
```

### Task 4: Verify the feature end to end

**Files:**
- Modify: `LEARNING.md` if the implementation revealed a durable product decision

- [ ] **Step 1: Run the focused checks**

Run:
```bash
./gradlew --no-daemon :app:compileDebugKotlin -q
./gradlew --no-daemon :app:compileDebugAndroidTestKotlin -q
./gradlew --no-daemon :app:testDebugUnitTest -q
```

- [ ] **Step 2: Fix any regressions**

Address only failures introduced by this feature.

- [ ] **Step 3: Commit**

```bash
git add LEARNING.md
git commit -m "docs: capture personal records ui decisions"
```
