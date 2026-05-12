# Year In Review Spec

Target release: later, after Progress, Personal Records, Training Context, and Race Events have enough history.

## Goal

Create an offline annual recap that helps users reflect on their training year.

It should feel warm and personal without becoming competitive or social.

## Product Fit

Hermes already values calm reflection. A yearly review can summarize:

- weeks planned.
- completed workouts.
- categories.
- races/events.
- personal records.
- common context tags.
- favorite library workouts.

## User Stories

- As a user, I can open a yearly review.
- As a user, I can see high-level summaries for the year.
- As a user, I can see category and event highlights.
- As a user, I can share a local text or image recap later.

## First Version Scope

- Year selector.
- Summary sections:
  - weeks with planned items.
  - completed workouts.
  - category distribution.
  - races/events.
  - personal records.
  - most common context tags.
- Text share option if Weekly Report sharing patterns exist.

## Out Of Scope For V1

- Public social cards.
- Server-generated recap.
- Competitive comparisons.
- Performance prediction.

## Data Direction

Derived from existing local data. No persisted recap needed.

## Privacy

Free-text notes should not appear by default.

## Acceptance Criteria

- User can review a selected year from local data.
- Recap copy stays reflective, not judgmental.
- No network/account required.
