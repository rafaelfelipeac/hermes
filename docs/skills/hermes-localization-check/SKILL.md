---
name: hermes-localization-check
description: Validate Hermes string resource changes across all locales and enforce translation consistency. Use when any `values*/strings.xml` file changes, when adding new UI copy, or when reviewing features that introduce new user-facing text.
---

# Hermes Localization Check

## Goal

Ensure every changed key is present and correct across all supported locales.

## Input Contract

Expect:
- changed `values*/strings.xml` files
- optional mode: `quick`, `standard`, `strict`

Default to `standard`.

## Strictness Modes

- `quick`: missing keys and placeholder mismatches only.
- `standard`: quick checks + English fallback text and naming issues.
- `strict`: standard checks + terminology consistency warnings.

## Workflow

1. Detect changed keys in base `values/strings.xml`.
2. Verify key presence in every localized `values-*/strings.xml`.
3. Validate placeholders and translation constraints.
4. Report misses with exact file references.

## Checks

- Key propagation across locales.
- Placeholder parity (`%1$s`, `%d`, etc.).
- No English placeholders in localized files.
- Resource hygiene: stable naming, no accidental duplicate semantics.

## Output Format

1. Missing or Broken Keys
- key name, affected locales, file references.

2. Translation Quality Issues
- placeholder mismatch, fallback English, terminology inconsistency.

3. Suggested Fix Plan
- minimal ordered repair steps.

## Dry Run Example

Input: "Validate this strings diff in standard mode."
Expected shape:
- Missing or Broken Keys
- Translation Quality Issues
- Suggested Fix Plan
