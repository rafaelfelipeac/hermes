# UI Refactor Screenshots

Persistent visual review captures for the UI refactor branch.

## Structure

Each wave gets a feature folder:

```text
wave-N-feature/
  before-light.png
  before-dark.png
  after-light.png
  after-dark.png
  after-*.png       # optional extra states, e.g. menus/dialogs
```

## Captured Waves

- `wave-0-theme`: Theme detail screen before/after, including the Material You setting after Wave 0.
- `wave-1-categories`: Categories list before/after, plus after overflow menu states.
- `wave-2-browse`: Browse destination launcher before/after.
- `wave-3-settings`: Main Settings launcher before/after.
- `wave-4-backup`: Backup utility screen before/after.
- `wave-5-trophies`: Trophies overview shelf before/after, with after captures using mixed trophy demo data.
- `wave-6-events`: Events grid before/after, captured with demo race events.
- `wave-7-personal-records`: Personal Records shelf before/after, captured with demo personal records.
- `wave-8-pace-calculator`: Pace Calculator result surface before/after.

## Future Waves

Add new captures under `docs/ui-refactor/screenshots/wave-N-feature/` before each wave review. Keep filenames stable so comparisons stay easy to scan.
