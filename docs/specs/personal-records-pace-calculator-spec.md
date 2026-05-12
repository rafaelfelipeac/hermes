# Personal Records And Pace Calculator Spec

Target release: after the Browse hub exists, or as a dedicated Browse destination once Progress and navigation direction are stable.

## Goal

Let users record meaningful personal records across their training categories and use a simple running pace calculator without turning Hermes into a performance-analysis app.

The feature should support reflection and practical race planning while staying offline-first, calm, and user-controlled.

## Product Fit

Hermes already helps users plan and review training weeks. Personal Records add a lightweight "what have I achieved?" layer that pairs naturally with:

- Race events.
- Progress summaries.
- Trophies.
- Weekly reports.
- Notes and perceived effort, if that feature ships later.

This should not become coaching, prediction, or ranking. The user enters records manually or saves them from completed race/event flows; Hermes stores and presents them clearly.

## Placement

Personal Records should be a future **Browse** destination.

Recommended Browse entries after this feature exists:

- Activity.
- Categories.
- Trophies.
- Personal Records.
- Pace Calculator.
- Backup and Import.
- Settings.

Two placement options:

1. **Single Browse card: Personal Records**
   - Records list.
   - Add/edit record.
   - Calculator entry point inside the screen.

2. **Two Browse cards: Personal Records + Pace Calculator**
   - Better if the calculator becomes a frequent utility.
   - Slightly more Browse surface area.

Recommendation for v1: use one `Personal Records` Browse card and expose the pace calculator as a prominent tool inside it. If usage justifies it later, promote `Pace Calculator` to its own Browse card.

## User Stories

- As a user, I can add a personal record for running, cycling, strength, or another category.
- As a user, I can see my records grouped by sport/category.
- As a runner, I can record a result for common distances like 5K, 10K, half marathon, and marathon.
- As a runner, I can calculate pace from distance and time.
- As a runner, I can calculate target finish time from distance and pace.
- As a runner, I can calculate distance from time and pace.
- As a user, I can edit or delete a personal record.
- As a user, I can optionally save a race-event result as a running personal record.

## First Version Scope

### Personal Records

Recommended v1 record types:

- Running:
  - fixed-distance result: distance + time + calculated pace.
  - free-distance result: distance + time + calculated pace.
- Cycling:
  - distance + time + optional average speed.
- Strength:
  - exercise name + best value.
  - value unit: weight, reps, duration, or custom text.

Recommended v1 fields:

- sport/category type.
- title.
- date.
- value fields by type.
- optional note.
- optional linked race/event id.

Keep v1 manual-first. Do not infer PRs automatically from weekly workout titles or descriptions.

### Running Pace Calculator

Calculator modes:

- Distance + time -> pace.
- Distance + pace -> finish time.
- Time + pace -> distance.

Distance presets:

- 1 km.
- 1 mile.
- 5K.
- 10K.
- 15K.
- Half marathon.
- Marathon.
- Custom.

Units:

- Metric first: kilometers and min/km.
- Consider miles/min-mile later, or include an explicit unit toggle if implementation cost is small.

Inputs:

- Distance.
- Hours/minutes/seconds.
- Pace minutes/seconds per kilometer.

Outputs:

- Pace.
- Finish time.
- Distance.

Optional v1 action:

- "Save as personal record" after calculating distance + time.

## Out Of Scope For V1

- Automatic record detection from workouts.
- VO2 max, race prediction, or training readiness.
- Leaderboards, social comparison, or sharing to public feeds.
- Charts comparing every record over time.
- Per-sport advanced calculators beyond running pace.
- Multiple unit systems unless the UI remains simple.
- Importing GPS files or external app data.

## Data Model Direction

This feature likely needs persisted data.

Possible model:

```kotlin
data class PersonalRecord(
    val id: Long,
    val categoryId: Long?,
    val sport: PersonalRecordSport,
    val title: String,
    val recordDate: LocalDate,
    val distanceMeters: Double?,
    val durationSeconds: Long?,
    val paceSecondsPerKm: Long?,
    val strengthValue: Double?,
    val strengthUnit: PersonalRecordStrengthUnit?,
    val note: String?,
    val linkedEventId: Long?,
)
```

Suggested enums:

- `PersonalRecordSport.RUNNING`
- `PersonalRecordSport.CYCLING`
- `PersonalRecordSport.STRENGTH`
- `PersonalRecordSport.OTHER`

Strength units:

- `WEIGHT`
- `REPS`
- `DURATION`
- `CUSTOM`

Open decision:

- Use category IDs for sport grouping, sport enum, or both.

Recommendation:

- Use a sport enum for stable behavior.
- Allow optional category ID for user organization and category color reuse.

## Backup Compatibility

This feature requires backup schema review before implementation.

If personal records are persisted:

- Add a new Room table.
- Add a new backup schema version.
- Export/import records.
- Keep older backups importable with an empty records list.
- Update `docs/backup-compatibility-policy.md`.
- Add decoder tests for old and new schemas.

The pace calculator alone does not require backup changes if it has no saved history.

## Activity Logging

Personal Records are user-visible state changes and should be logged.

Recommended action types:

- `CREATE_PERSONAL_RECORD`
- `UPDATE_PERSONAL_RECORD`
- `DELETE_PERSONAL_RECORD`
- `SAVE_PACE_CALCULATION_AS_RECORD`

Recommended entity type:

- `PERSONAL_RECORD`

Metadata should avoid free-text note contents.

Safe metadata:

- sport.
- category id/name when present.
- record date.
- distance.
- duration.
- pace.
- old/new numeric values where useful.
- linked event id when present.

Do not log:

- note body.
- custom title if that is treated as user free text. If a title is generated from fixed distance labels like `5K`, that label is safe.

## UI Direction

### Browse Card

Browse card copy should present the feature as practical and calm:

- Label: `Personal Records`.
- Supporting copy: "Track your best efforts and calculate running pace."

### Personal Records Screen

Recommended sections:

- Header summary:
  - total records.
  - most recent record.
  - running PR count.
- Sport/category filters.
- Record list grouped by sport or category.
- Add record action.
- Pace calculator card/action.

Record card content:

- Sport/category label and color.
- Record title.
- Main value.
- Date.
- Optional linked event indicator.

### Add/Edit Record

Use a focused form:

- Sport selector.
- Category selector, optional.
- Record type fields based on sport.
- Date.
- Optional note.

For running, support distance presets and custom distance.

For strength, avoid over-modeling exercises in v1. A text exercise/title field plus value/unit is enough.

### Pace Calculator UI

Use a segmented control for mode:

- Pace.
- Time.
- Distance.

Use compact numeric inputs:

- Distance input with preset chips.
- Time input split into hours/minutes/seconds.
- Pace input split into minutes/seconds.

Show output in one clear result block.

Avoid motivational or predictive copy. The calculator should be utilitarian.

## Progress And Report Integration

Progress later can show:

- newest PR.
- count of records by sport.
- nearest upcoming race plus target pace if user entered one.

Weekly Report later can include:

- records achieved during the selected week.
- race-event result if linked.

Do not block v1 on Progress or Report integration.

## Trophies Integration

Optional later trophy families:

- First personal record.
- Records in multiple sports.
- New running PR after a race event.

Do not include trophy changes in v1 unless the feature is otherwise small.

## Localization

All labels and calculator units must be localized.

Pay attention to:

- decimal separators.
- distance abbreviations.
- pace formatting.
- right-to-left layouts.
- plural forms for records.

## Testing

Recommended unit tests:

- Pace calculator: distance + time -> pace.
- Pace calculator: distance + pace -> time.
- Pace calculator: time + pace -> distance.
- Common distance preset conversion.
- Rounding behavior for pace and time.
- Personal record mapper persists and restores running records.
- Personal record mapper persists and restores strength records.
- Activity logging metadata excludes note text.
- Backup vNext round trip includes personal records.
- Older backup schema imports with empty records.

Recommended UI tests:

- Empty records screen.
- Add running record.
- Add strength record.
- Edit record.
- Delete record.
- Calculator mode switching.
- Save calculator result as record if included in v1.

## Acceptance Criteria

- Personal Records are reachable from Browse.
- User can create, edit, and delete records.
- Running records can store distance, time, and calculated pace.
- Strength records can store exercise/title plus value/unit.
- Pace calculator supports the three core calculation modes.
- Persisted records are included in backup schema when the feature ships.
- Activity logging covers record mutations without logging note contents.
- All UI copy is localized.
