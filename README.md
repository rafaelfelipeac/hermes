[![📦 Build Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml)
[![🚀 Release Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml)

---

# 🪽 Hermes

<p align="center">
  <img src="docs/icon/hermes-app-icon.png" width="160" alt="Hermes app icon" />
</p>

**Hermes** is a calm, offline-first **training planner and progress companion** — built to help you shape the week, adapt when life moves, and keep long-running goals visible without turning training into noise.

Like **Hermes, the messenger god** 🪽, it’s made for movement: workouts can be reordered, rescheduled and rearranged as the week changes, while events, challenges, personal records and progress stay connected to the same local training story.

It focuses on clarity and consistency, with one practical idea at the center:

> **“Plan the week. Life happens. Adjust.”**

No account. No server. No pressure. Just a realistic plan you can keep reshaping as life happens.

<a href="https://play.google.com/store/apps/details?id=com.rafaelfelipeac.hermes">
    <img 
        alt="Get it on Google Play" 
        src="https://user-images.githubusercontent.com/9745110/89697876-99ab9480-d8f4-11ea-869d-32131a31ab96.png" 
        width="200">
</a>  

---

## ✨ Features

### Plan the week
- Weekly training board with a calendar-like rhythm and lighter planning flow
- Drag & drop workouts between days, slots and the **“To be defined”** area
- Training, rest, busy, sick and race-event planning states
- Categories with colors, ordering, hiding and restore-default support
- Configurable week start day

### Track progress
- Progress overview with weekly readouts, training mix, activity preview and next-focus guidance
- Activity history with filters by type, category and week
- Trophy families with overview/detail screens and celebration feedback

### Prepare goals and events
- Events screen for races and other non-workout planning moments
- Challenges with daily or total goals, pacing guidance, progress history and completion celebrations
- Personal Records with customizable series, result history and best-result tracking
- Pace calculator for pace, time and distance

### Own your data
- Offline-first local storage with no account and no server
- JSON backup export/import
- Default backup folder selection in Settings

### Personalize the app
- Light, dark and Material You color options
- Configurable distance, pace and weight units
- App language support:
  - English (default)
  - Portuguese (Brazil)
  - Deutsch
  - Français
  - Español
  - Italiano
  - العربية
  - हिन्दी
  - 日本語

---

## 🖼️ Screenshots

A quick look at Hermes in action — focused on clarity and flexibility.

### ☀️ Light mode
<p align="center">
  <img src="docs/screenshots/light/weekly.png" width="48%" alt="Light theme weekly screen with the current week and planned workouts." />
  <img src="docs/screenshots/light/progress.png" width="48%" alt="Light theme progress screen with weekly readout, completion chart, and category mix." />
</p>
<p align="center">
  <img src="docs/screenshots/light/challenges.png" width="48%" alt="Light theme challenges screen with active goal progress and pacing guidance." />
  <img src="docs/screenshots/light/events.png" width="48%" alt="Light theme events screen with upcoming race and training events." />
</p>
<p align="center">
  <img src="docs/screenshots/light/personal-records.png" width="48%" alt="Light theme personal records screen with record families and best results." />
  <img src="docs/screenshots/light/trophies.png" width="48%" alt="Light theme trophies screen with unlocked and locked trophy cards." />
</p>

### 🌙 Dark mode
<p align="center">
  <img src="docs/screenshots/dark/weekly.png" width="48%" alt="Dark theme weekly screen with the current week and planned workouts." />
  <img src="docs/screenshots/dark/progress.png" width="48%" alt="Dark theme progress screen with weekly readout, completion chart, and category mix." />
</p>
<p align="center">
  <img src="docs/screenshots/dark/challenges.png" width="48%" alt="Dark theme challenges screen with active goal progress and pacing guidance." />
  <img src="docs/screenshots/dark/events.png" width="48%" alt="Dark theme events screen with upcoming race and training events." />
</p>
<p align="center">
  <img src="docs/screenshots/dark/personal-records.png" width="48%" alt="Dark theme personal records screen with record families and best results." />
  <img src="docs/screenshots/dark/trophies.png" width="48%" alt="Dark theme trophies screen with unlocked and locked trophy cards." />
</p>

---

## 🧠 Design philosophy

Hermes avoids the “hardcore fitness app” vibe.

No:
- aggressive charts
- heavy gamification pressure
- constant performance comparison

Instead, the focus is on:
- **weekly planning**
- **easy rescheduling**
- **visual clarity**
- **calm interaction**
- **rest days as first-class citizens**
- **progress without punishment**

This is a tool meant to support training — not judge it. Hermes includes gentle, optional recognition, but the app stays quiet enough to be useful on ordinary weeks too.

---

## 🛠️ Tech stack

- **Kotlin + Android** – Single-platform app
- **Jetpack Compose + Material 3** – Declarative UI and adaptive navigation
- **Room** – Local persistence
- **DataStore (Preferences)** – Theme, language, units and settings
- **AppCompat locales** – In-app language selection
- **Hilt** – Dependency injection
- **Coroutines + Flow + StateFlow** – Async and reactive streams
- **Detekt + Ktlint** – Static analysis and formatting
- **GitHub Actions** – CI for build, lint and releases

---

## 🚫 Contributing

Contributions are not open at the moment. This is a personal playground. 
But forks, ideas and feedback are always welcome.

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
