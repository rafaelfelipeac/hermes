[![📦 Build Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml)
[![🚀 Release Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml)

---

# 🪽 Hermes

<p align="center">
  <img src="docs/icon/hermes-app-icon.png" width="96" alt="Hermes app icon" />
</p>

**Hermes** is a simple, offline-first **weekly training planner** — built to help you plan the week and keep it adaptable.

Like **Hermes, the messenger god** 🪽, it’s made for movement: trainings can be **reordered, rescheduled and rearranged** as the week changes — without turning into a rigid routine or a performance tracker.

It focuses on clarity and consistency, with a lightweight weekly view that embraces one idea:

> **“Plan the week. Life happens. Adjust.”**

No noise. No pressure. Just a realistic plan you can keep reshaping as life happens.

---

## ✨ Features

- Weekly-based training view (calendar-style, but lighter)
- Plan sessions by day of the week
- A **“To be defined”** area for sessions not assigned yet
- Drag & drop to **reschedule** sessions between days
- Mark days or sessions as:
  - **Training**
  - **Rest day**
- Simple visual states:
  - Planned
  - Completed
  - Rest
- Light & dark themes
- Language support:
  - English (default)
  - Portuguese (Brazil)
  - Deutsch
  - Français
  - Español
  - Italiano
  - العربية
  - हिन्दी
  - 日本語
- Offline-first — no account, no server, no noise

> ⚠️ This is an early MVP focused on structure and flow. Many ideas are planned, but intentionally not rushed.

---

## 🖼️ Screenshots

A quick look at Hermes in action — focused on clarity and flexibility.

### ☀️ Light mode
<p float="left">
  <img src="docs/screenshots/light/hermes-light-1.jpeg" width="24%" />
  <img src="docs/screenshots/light/hermes-light-2.jpeg" width="24%" />
  <img src="docs/screenshots/light/hermes-light-3.jpeg" width="24%" />
</p>

### 🌙 Dark mode
<p float="left">
  <img src="docs/screenshots/dark/hermes-dark-1.jpeg" width="24%" />
  <img src="docs/screenshots/dark/hermes-dark-2.jpeg" width="24%" />
  <img src="docs/screenshots/dark/hermes-dark-3.jpeg" width="24%" />
</p>

---

## 🧠 Design philosophy

Hermes avoids the “hardcore fitness app” vibe.

No:
- aggressive charts
- gamification pressure
- constant performance comparison

Instead, the focus is on:
- **weekly planning**
- **easy rescheduling**
- **visual clarity**
- **calm interaction**
- **rest days as first-class citizens**

This is a tool meant to support training — not judge it.

---

## 🛠️ Tech stack

- **Kotlin + Android** – Single-platform app
- **Jetpack Compose + Material 3** – Declarative UI
- **Room** – Local persistence
- **DataStore (Preferences)** – Theme, language and settings
- **Hilt** – Dependency injection
- **Coroutines + Flow + StateFlow** – Async and reactive streams
- **Detekt + Ktlint** – Static analysis and formatting
- **GitHub Actions** – CI for build, lint and releases

---

## 🧪 Current status

Hermes is an early-stage personal project, built slowly and intentionally.
The focus is on getting the weekly planning experience right before adding complexity.

Expect changes. Expect refactors. Expect experiments.

---

## 🗺️ Ideas for the future

Some things on the radar (not guaranteed):

- Weekly summaries (planned vs completed)
- Notes + perceived effort
- Training templates / reusable routines
- Shareable weekly report (coach-friendly)
- Subtle animations and micro-interactions
- Fun yearly comparisons (“you ran X km — that’s like crossing Y”)

---

## 🚫 Contributing

Contributions are not open at the moment. This is a personal playground. 
But forks, ideas and feedback are always welcome.

---

## ⭐ Why this project exists

Hermes exists as:
- a design and architecture playground
- a Kotlin + Android learning space
- a way to build a training planner that feels calm and flexible

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
