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
