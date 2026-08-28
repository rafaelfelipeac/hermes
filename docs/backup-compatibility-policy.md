# Backup Compatibility Policy

## Goal
Keep backup import stable across app releases by versioning the JSON schema explicitly.

## Compatibility contract
- Import compatibility is decided by `schemaVersion`, not by `appVersion`.
- `appVersion` is metadata for diagnostics and support triage only.
- Unknown future schemas must fail fast with a friendly import error.

## Current policy
- Current supported schema(s): `1`, `2`, `3`, `4`, `5`
- Decoder routing:
  - `schemaVersion = 1` -> `BackupV1Decoder`
  - `schemaVersion = 2` -> `BackupV2Decoder`
  - `schemaVersion = 3` -> `BackupV3Decoder`
  - `schemaVersion = 4` -> `BackupV4Decoder`
  - `schemaVersion = 5` -> `BackupV5Decoder`
  - Any other value -> unsupported schema error

## Current schema notes
- `schemaVersion = 2` adds `settings.weekStartDay`.
- `schemaVersion = 1` backups remain importable and default missing `weekStartDay` to `MONDAY` during decode.
- `schemaVersion = 3` adds the `RACE_EVENT` enum value to `workouts.eventType` and keeps the JSON shape otherwise unchanged.
- `schemaVersion = 4` adds `settings.distanceUnit`, `settings.paceUnit`, `settings.weightUnit`, `personalRecordFamilies`, and `personalRecordEntries`.
- `schemaVersion = 1`, `2`, and `3` backups continue to import with empty personal-record collections and default unit preferences when those fields are absent from the older schema.
- `schemaVersion = 5` adds `challenges` and `challengeProgressEntries`.
- `schemaVersion = 5` challenge records include stable target types, integer target quantities, and no unit field; progress records keep integer quantities, ISO dates, and timestamps.
- `schemaVersion = 1`, `2`, `3`, and `4` backups continue to import with empty challenge collections when those fields are absent from the older schema.

## Rules for future schema changes
1. Add a new decoder (`BackupV2Decoder`, etc.) instead of rewriting old decoders.
2. Keep old decoders for all schemas we still support.
3. Add routing in `BackupJsonCodec.decode(...)` by explicit schema version.
4. Preserve import behavior:
   - invalid JSON -> invalid file error
   - missing required sections -> missing section error
   - unsupported schema -> unsupported version error
   - older schemas may omit newer sections, but the decoder must synthesize safe defaults instead of rejecting them
5. Add tests for:
  - `v1` backups importing on current app
  - unknown future schema failing gracefully
  - missing required sections failing gracefully
  - `v4` round-trip coverage for personal records and unit preferences
  - `v5` round-trip coverage for challenges and challenge progress entries

## Notes
- Replace-mode import remains transactional in the repository layer.
- Backward compatibility should be additive whenever possible to avoid breaking existing backups.
