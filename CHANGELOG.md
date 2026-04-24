# Changelog

All notable changes to this project will be documented in this file.

## [v1.8.0]
### Added
- Release notes in Settings so shipped changes are available inside the app
- Haptic feedback when completing workouts and dragging weekly items

## [v1.7.0]
### Added
- Trophy families with overview, detail and celebration banners
- Developer Settings previews for mixed, locked and completed trophy states

### Changed
- Trophy events now appear in Activity with trophy metadata and naming
- Settings developer copy now uses completed-trophies terminology

### Fixed
- Trophy progress, thresholds and grid layout regressions
- Trophy copy polish across locales and debug previews

## [v1.6.0]
### Added
- Activity filters by type, category and week
- Category-aware Activity filter context for supported workout history
- Backup schema v2 with `weekStartDay` restore support

### Changed
- Weekly drag-and-drop now uses measured slot bounds for more accurate slot targeting
- Backup import/export handling now treats `appVersion` as metadata while routing compatibility by `schemaVersion`
- Repo-local Hermes skills documentation and review guidance

### Fixed
- Backup restore now preserves the configured start-of-week setting during import
- Backup decode paths now fail more gracefully for malformed optional data and forward-compatible user-action payloads
- Drag preview/drop alignment regressions in slot mode

## [v1.5.0]
### Added
- Configurable start of the week (any day, Monday through Sunday) in Settings
- Weekly header summary with progress and completion feedback
- Activity timeline entry for weekly-completion milestone

### Changed
- Weekly week-guardrails and cross-week handling when display-week boundaries change
- Weekly summary copy now highlights completion and non-workout context more clearly
- Weekly training ViewModel tests split into focused suites with shared test support helpers

### Fixed
- Weekly header summary assertion/test coverage for secondary line rendering
- Completion transition handling to avoid duplicate milestone feedback in rapid toggles

## [v1.4.0]
### Added
- JSON backup export/import support with schema-versioned compatibility handling
- Backup folder actions in Settings (save default folder and clear default folder)
- Backup compatibility policy documentation

## [v1.3.0]
### Added
- Slot mode for weekly planning with morning/afternoon/night grouping
- New non-workout events: rest, busy, and sick (including slot-aware placement)
- Slot mode settings detail screen with help dialog
- Activity support for slot mode changes and event-specific busy/sick/rest actions

### Changed
- Weekly day indicator ownership now prioritizes the last workout on mixed days
- Drag and drop feedback with live drop preview and improved slot targeting behavior
- Settings structure: Slot mode moved under Workouts section
- Demo data seeding expanded to cover slot mode and new weekly event flows

### Fixed
- Ghost/drag selection and drop-position consistency issues in slot mode
- Localization consistency across locales for new weekly event and slot-mode strings
- Help icon sizing/alignment consistency across Categories, Settings, and Weekly screens

## [v1.2.0]
### Added
- Categories management (create/edit/reorder/hide/restore defaults) with color support
- Demo data seeding completion feedback in Settings

### Changed
- Settings navigation flows and safer external intent handling
- Updated Compose BOM to support typed menu anchors

### Fixed
- Localization consistency across multiple locales
- Workout row chip/checkmark contrast behavior

## [v1.1.0]
### Added
- Undo support for delete, move/reorder and completion actions
- “Copy last week” to duplicate the previous week into the current one

### Fixed
- Test stability and lint/detekt compliance improvements

## [v1.0.0]
### Added
- First public app release
- Weekly training planner (calendar-style weekly view)
- “To be defined” section for unscheduled sessions
- Drag & drop rescheduling across days
- Rest day support and completion states
- Light/Dark themes and multi-language support
- Offline-first local storage

## [v0.1.0]
### Added
- Initial feature foundation and internal build
