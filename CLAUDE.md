# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run build   # expo-module build — compiles src/*.ts to build/ (tsc)
npm run clean   # expo-module clean — removes build/
npm run lint    # expo-module lint — eslint (config: universe/native, universe/web)
npm test        # expo-module test — jest, via expo-module-scripts
```

These scripts wrap the `expo-module-scripts` CLI (`expo-module <cmd>`), the standard tooling for Expo native modules — there is no custom build/lint/test config beyond `.eslintrc.js` and `tsconfig.json`.

There is no `example/package.json` and no lockfile for `example/`, so the example app is not currently runnable/installable as-is — it's a Kotlin/TSX reference only.

## Architecture

This is an **Android-only Expo native module** (see `expo-module.config.json`: `"platforms": ["android"]`) that wraps Android's Device Owner / `DevicePolicyManager` APIs, primarily for building kiosk-mode apps.

**Two layers, one contract:**
- `src/ExpoDeviceAdminModule.ts` declares the native module's TS shape and calls `requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin")`. `src/index.ts` re-exports it as the package default.
- `android/src/main/java/expo/modules/deviceadmin/ExpoDeviceAdminModule.kt` implements every method declared in the `.ts` file via the Expo Modules Kotlin DSL (`Module()` / `ModuleDefinition { ... }`). The module name registered here (`Name("ExpoDeviceAdmin")`) must match the string passed to `requireNativeModule` in the TS file, and the class path in `expo-module.config.json`'s `android.modules` must match the Kotlin package/class.
- When adding or changing a native method, **both files must change together**: add the `Function`/`AsyncFunction` in the `.kt` `ModuleDefinition`, then mirror its signature in the `declare class` block in the `.ts` file. `NativeModule`'s TS base type has a `[key: string]: any` index signature, so constants (e.g. `LOCK_TASK_FEATURE_*`, exposed via `Constants(...)` in Kotlin) don't need explicit TS declarations to type-check.

**Device admin plumbing:**
- `android/src/main/java/expo/modules/deviceadmin/MinimalDeviceAdminReceiver.kt` is the `DeviceAdminReceiver` subclass required by Android for device-admin/device-owner activation; it's intentionally minimal (logs + toasts on enable/disable).
- `android/src/main/res/xml/device_admin.xml` declares the device-admin policies the app requests (`force-lock`, `wipe-data`) — extend this when adding methods that need additional policies.
- `android/src/main/AndroidManifest.xml` registers the receiver with `BIND_DEVICE_ADMIN` and the `DEVICE_ADMIN_ENABLED` intent filter, and requests `REORDER_TASKS`.
- Most privileged operations (`rebootDevice`, `addToLockTaskMode`, `setLockTaskFeatures`) gate on `dpm.isDeviceOwnerApp(...)` and throw if the app isn't the device owner — this is a real runtime precondition (the app must be provisioned as device owner via `adb shell dpm set-device-owner ...` or similar), not just defensive code.
- Kiosk mode (`startKioskMode`/`stopKioskMode`/`checkIfKioskEnabled`) uses Android's `startLockTask`/`stopLockTask`/`ActivityManager.lockTaskModeState` on the *current activity*, which is separate from device-owner status — lock task mode can work without device ownership if the package is allow-listed via `setLockTaskPackages` (device-owner-only) or the activity opts in itself.

## Versioning

Keep `package.json`'s `"version"` and `android/build.gradle`'s `version` / `versionName` in sync — they are not read from a single source of truth.
