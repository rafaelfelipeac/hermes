# Backup Compatibility Policy

## Goal
Keep backup import stable across app releases by versioning the JSON schema explicitly.

## Compatibility contract
- Import compatibility is decided by `schemaVersion`, not by `appVersion`.
- `appVersion` is metadata for diagnostics and support triage only.
- Unknown future schemas must fail fast with a friendly import error.

## Current policy
- Current supported schema(s): `1`, `2`, `3`
- Decoder routing:
  - `schemaVersion = 1` -> `BackupV1Decoder`
  - `schemaVersion = 2` -> `BackupV2Decoder`
  - `schemaVersion = 3` -> `BackupV3Decoder`
  - Any other value -> unsupported schema error

## Current schema notes
- `schemaVersion = 2` adds `settings.weekStartDay`.
- `schemaVersion = 1` backups remain importable and default missing `weekStartDay` to `MONDAY` during decode.
- `schemaVersion = 3` adds the `RACE_EVENT` enum value to `workouts.eventType` and keeps the JSON shape otherwise unchanged.

## Rules for future schema changes
1. Add a new decoder (`BackupV2Decoder`, etc.) instead of rewriting old decoders.
2. Keep old decoders for all schemas we still support.
3. Add routing in `BackupJsonCodec.decode(...)` by explicit schema version.
4. Preserve import behavior:
   - invalid JSON -> invalid file error
   - missing required sections -> missing section error
   - unsupported schema -> unsupported version error
5. Add tests for:
   - `v1` backups importing on current app
   - unknown future schema failing gracefully
   - missing required sections failing gracefully

## Notes
- Replace-mode import remains transactional in the repository layer.
- Backward compatibility should be additive whenever possible to avoid breaking existing backups.
