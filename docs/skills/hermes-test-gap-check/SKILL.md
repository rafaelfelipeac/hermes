---
name: hermes-test-gap-check
description: Identify missing or weak automated test coverage for Hermes code changes and propose concrete fake-based tests. Use when reviewing pull requests, adding features, refactoring ViewModels/repositories, or validating that behavior changes are covered by unit tests.
---

# Hermes Test Gap Check

## Goal

Map every behavior change to coverage status and propose targeted fake-based tests for uncovered paths.

## Input Contract

Expect:
- production diff or changed files
- optional related test files
- optional mode: `quick`, `standard`, `strict`

Default to `standard`.

## Strictness Modes

- `quick`: include only uncovered high-risk behaviors.
- `standard`: include uncovered + partial coverage items.
- `strict`: include all gaps, plus weak assertions and flaky-risk patterns.

## Workflow

1. List changed behaviors from production files.
2. Map each behavior to `covered`, `partial`, or `uncovered`.
3. Prioritize by risk: state transitions, persistence mapping, logging side effects.
4. Propose concrete tests aligned with Hermes patterns.

## Hermes Testing Guardrails

- Prefer fakes over mocks.
- Follow existing module patterns; do not introduce new mocking frameworks.
- Keep tests behavior-focused.
- Use deterministic inputs (fixed dates/times/ids).
- For backup contract changes, include schema-routing coverage and graceful failure tests for unsupported future schemas or missing required sections.

## Suggested Test Template

- `Behavior`
- `Risk`
- `Test name`
- `Setup`
- `Action`
- `Assertions`

## Output Format

1. Coverage Map
- `Behavior -> covered/partial/uncovered` with references.

2. Missing Tests (risk-ordered)
- One item per proposed test.

3. Optional Follow-ups
- refactors that improve testability directly.

## Dry Run Example

Input: "Check test gaps for this ViewModel refactor in standard mode."
Expected shape:
- Coverage Map
- Missing Tests
- Optional Follow-ups
