# Changelog

All notable changes to this project will be documented in this file.

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
