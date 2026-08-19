# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run build   # expo-module build — compiles src/*.ts to build/ (tsc)
npm run clean   # expo-module clean — removes build/
npm run lint    # expo-module lint — eslint, flat config (eslint.config.js -> expo-module-scripts/eslint.config.base -> universe/native)
npm test        # expo-module test — jest, via expo-module-scripts (currently no test files exist, so this always fails with "No tests found")
```

These scripts wrap the `expo-module-scripts` CLI (`expo-module <cmd>`), the standard tooling for Expo native modules — there is no custom build/lint/test config beyond `eslint.config.js` and `tsconfig.json`. ESLint 9 (bundled by `expo-module-scripts` ^56) requires the flat config format; there is no `.eslintrc.js` any more.

There is no `example/package.json` and no lockfile for `example/`, so the example app is not currently runnable/installable as-is — it's a Kotlin/TSX reference only.

## Architecture

This is an **Android-only Expo native module** (see `expo-module.config.json`: `"platforms": ["android"]`) that wraps Android's Device Owner / `DevicePolicyManager` APIs, primarily for building kiosk-mode apps.

**Two layers, one contract:**
- `src/ExpoDeviceAdminModule.ts` declares the native module's TS shape and calls `requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin")`. `src/index.ts` re-exports it as the package default.
- `android/src/main/java/expo/modules/deviceadmin/ExpoDeviceAdminModule.kt` implements every method declared in the `.ts` file via the Expo Modules Kotlin DSL (`Module()` / `ModuleDefinition { ... }`). The module name registered here (`Name("ExpoDeviceAdmin")`) must match the string passed to `requireNativeModule` in the TS file, and the class path in `expo-module.config.json`'s `android.modules` must match the Kotlin package/class.
- When adding or changing a native method, **both files must change together**: add the `Function`/`AsyncFunction` in the `.kt` `ModuleDefinition`, then mirror its signature in the `declare class` block in the `.ts` file. As of `expo-modules-core` 57, `NativeModule`'s TS base type no longer has a `[key: string]: any` index signature, so constants (e.g. `LOCK_TASK_FEATURE_*`, exposed via `Constants(...)` in Kotlin) must now be declared explicitly on the class too, or TS won't see them.
- Every `AsyncFunction` in the `.kt` file returns a Promise to JS — double check the matching `.ts` signature says `Promise<...>`, not a bare type. `enableImmersiveMode`/`disableImmersiveMode` were both mistyped as `void` before this was caught.

**Device admin plumbing:**
- `android/src/main/java/expo/modules/deviceadmin/MinimalDeviceAdminReceiver.kt` is the `DeviceAdminReceiver` subclass required by Android for device-admin/device-owner activation; it's intentionally minimal (logs + toasts on enable/disable).
- `android/src/main/res/xml/device_admin.xml` declares the device-admin policies the app requests (`force-lock`, `wipe-data`) — extend this when adding methods that need additional policies.
- `android/src/main/AndroidManifest.xml` registers the receiver with `BIND_DEVICE_ADMIN` and the `DEVICE_ADMIN_ENABLED` intent filter, and requests `REORDER_TASKS`. The receiver is deliberately `android:exported="false"` (see the comment above it) — this project only provisions device ownership via `adb shell dpm set-device-owner`, which delivers `ACTION_DEVICE_ADMIN_ENABLED` as a system-sent broadcast that reaches non-exported receivers fine; don't "fix" this to `true` without re-reading that comment first.
- Most privileged operations (`rebootDevice`, `addToLockTaskMode`, `setLockTaskFeatures`) gate on `dpm.isDeviceOwnerApp(...)` and throw if the app isn't the device owner — this is a real runtime precondition (the app must be provisioned as device owner via `adb shell dpm set-device-owner ...` or similar), not just defensive code.
- Kiosk mode (`startKioskMode`/`stopKioskMode`/`checkIfKioskEnabled`) uses Android's `startLockTask`/`stopLockTask`/`ActivityManager.lockTaskModeState` on the *current activity*, which is separate from device-owner status — lock task mode can work without device ownership if the package is allow-listed via `setLockTaskPackages` (device-owner-only) or the activity opts in itself.

## Dependencies

Pinned to Expo SDK 57 (`expo` ~57, `react-native` 0.87, `react` 19, `@types/react` ~19, `expo-module-scripts` ^56). `peerDependencies` for `expo`/`react`/`react-native` stay wildcarded (`"*"`) — only the devDependency versions matter for local dev/lint/build in this repo.

`expo-modules-core` is an explicit devDependency even though `src/ExpoDeviceAdminModule.ts` only imports it as if it were transitively available through `expo`: npm doesn't reliably hoist it to the top-level `node_modules` (it can end up nested under `node_modules/expo/node_modules/expo-modules-core`, which TS/Node can't resolve from `src/`). Don't remove it as "unused" — removing it silently breaks the import.

`android/build.gradle`'s fallback SDK versions (`compileSdkVersion`/`targetSdkVersion` 36, `minSdkVersion` 24) mirror `expo-modules-core`'s own `useDefaultAndroidSdkVersions()` defaults (in `node_modules/expo-modules-core/android/ExpoModulesCorePlugin.gradle`) — when bumping `expo-modules-core`, check that file for new defaults and bump these fallbacks to match.

`"prepare": "expo-module build"` in `package.json` is deliberately **not** `"expo-module prepare"` — don't "fix" this to look consistent with the other scripts. This package is consumed via a git URL dependency (not the npm registry), and `build/` is gitignored/never committed, so `prepare` (the only lifecycle hook npm runs automatically for a git-sourced dependency) must actually produce `build/index.js` itself. `expo-module-scripts`'s own bundled `prepare` binary can't be relied on for this: older versions ship it as a bash script (`set -eo pipefail`) that crashes on Windows when Node's loader tries to parse it as JS instead of executing it through a shell, and the current version (`^56`) turned it into a pure no-op that would silently skip building entirely, shipping a broken module.

## Versioning

Keep `package.json`'s `"version"` and `android/build.gradle`'s `version` / `versionName` in sync — they are not read from a single source of truth.
