[![📦 Build Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/build-and-lint.yml)
[![🚀 Release Status](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml/badge.svg)](https://github.com/rafaelfelipeac/hermes/actions/workflows/release.yml)

---

# 🪽 Hermes

**Hermes** is a personal project built to help organize **weekly training routines** in a way that’s simple, visual and focused on consistency — not pressure.

Inspired by the idea of movement and rhythm (and, yes, the messenger god 🪽), Hermes is designed for people who train during the week and want a clear answer to one simple question:

> “What’s my plan this week — and how did it actually go?”

No clutter. No overwhelming metrics. Just a calm, weekly view of your training life.

---

## 🏃‍♂️ What is Hermes?

Hermes is a **weekly training planner** where each week is the main unit of organization.

You define what you want (or need) to do during the week, drag things into place, mark them as done — or as rest — and move on.

It’s not about perfection.  
It’s about **showing up**, week after week.

---

## ✨ What it does (current MVP)

- Weekly-based training view (calendar-style, but lighter)
- Organize trainings by day of the week
- A special **“To be defined”** area for trainings you haven’t assigned yet
- Drag & drop trainings between days
- Mark days or items as:
    - **Training**
    - **Rest day**
- Simple visual states:
    - Planned
    - Completed
    - Rest
- Dark & light themes
- Language support:
    - English (default)
    - Portuguese (Brazil)
    - System default
- Offline-first — no account, no server, no noise

> ⚠️ This is an early MVP focused on structure and flow. Many ideas are planned, but intentionally not rushed.

---

## 🖼️ Screenshots

### ☀️ Light Mode

<p float="left">
  <img src="screenshots/hermes-light-1.png" width="24%" />
  <img src="screenshots/hermes-light-2.png" width="24%" />
</p>
---

### 🌙 Dark Mode

<p float="left">
  <img src="screenshots/hermes-dark-1.png" width="24%" />
  <img src="screenshots/hermes-dark-2.png" width="24%" />
</p>

## 🧠 Design philosophy

Hermes intentionally avoids looking like a “hardcore fitness app”.

- No aggressive charts
- No gamification pressure
- No constant performance comparison

Instead, the focus is on:
- Weekly rhythm
- Visual clarity
- Calm interaction
- Respecting rest days as first-class citizens

This is a tool meant to support training — not judge it.

---

## 🛠️ Tech Stack

- **Kotlin + Android** – Single-platform app
- **Jetpack Compose + Material 3** – Declarative UI
- **Room** – Local persistence
- **DataStore (Preferences)** – Theme, language and preferences
- **Hilt** – Dependency injection
- **Coroutines + Flow + StateFlow** – Async and reactive streams
- **Detekt + ktlint** – Static analysis and formatting
- **GitHub Actions** – CI for build, lint and releases

---

## 🧪 Current Status

Hermes is an **early-stage personal project**.

It’s being built slowly and intentionally, focusing on:
- Clean architecture
- Thoughtful UI decisions
- A solid MVP before expanding features

Expect changes. Expect refactors. Expect experiments.

---

## 🗺️ Ideas for the future

Some things already on the radar (but not guaranteed):

- Weekly summaries (planned vs completed)
- Effort perception & notes
- Yearly distance comparisons (“you ran X km — that’s like crossing Y”)
- Shareable weekly report for coaches
- Multiple training types & templates
- Subtle animations and micro-interactions

---

## 🚫 Contributing

This is currently a solo project and personal playground.  
Contributions are not open at the moment — but forks are always welcome.

---

## ⭐ Why this project exists

Hermes exists as:
- A design and architecture playground
- A Kotlin + Android learning space
- A way to rethink how training apps *feel*, not just what they track

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
