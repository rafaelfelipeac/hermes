# App Privacy Lock Spec

Target release: after notes/context features make Hermes more personally sensitive.

## Goal

Let users optionally protect Hermes with device authentication such as biometric or system credential prompt.

This should protect casual access to personal training notes without inventing a separate password system.

## User Stories

- As a user, I can enable app lock.
- As a user, I can unlock with device authentication.
- As a user, I can disable app lock.
- As a user, I can still rely on Android device security.

## First Version Scope

- Settings toggle for app lock.
- Prompt on app foreground when enabled.
- Use Android-supported device credential/biometric flow.
- Fail closed until authentication succeeds.

## Out Of Scope For V1

- Custom PIN.
- Encrypted database.
- Per-section locks.
- Remote wipe.

## Data Direction

Persist only app-lock enabled flag and possibly last-unlocked timestamp if timeout is supported.

No backup requirement unless settings backup includes privacy preferences.

## Activity Logging

Do not log unlock attempts.

Log enabling/disabling app lock only if settings changes normally appear in Activity.

## Acceptance Criteria

- App lock is optional.
- App uses device-supported authentication.
- No custom password storage is introduced.
