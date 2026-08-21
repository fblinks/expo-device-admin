# Changelog

## Unpublished

### 🛠 Breaking changes

### 🎉 New features

### 🐛 Bug fixes

### 💡 Others

## 0.5.1 (2026-08-21)

### 🛠 Breaking changes

### 🎉 New features

### 🐛 Bug fixes

- Fix `enableImmersiveMode()` on Android 11+ (API 30+) leaving the system bars visible indefinitely once revealed by a swipe gesture (long enough to tap Back and exit); `systemBarsBehavior` was set to `BEHAVIOR_DEFAULT` instead of `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, which auto-hides the bars again after the swipe. Note this narrows the reveal window rather than eliminating it — Android intentionally keeps a system-bar reveal gesture reachable as an accessibility/escape valve, regardless of behavior setting.

### 💡 Others

## 0.5.0 (2026-08-21)

### 🛠 Breaking changes

### 🎉 New features

- Add `setApplicationHidden(packageName, hidden)`: hides or unhides an installed package fleet-wide via `DevicePolicyManager.setApplicationHidden`, closing every path to a competing HOME-capable launcher at once (icon, Recents, and Home resolution alike) rather than one specific path at a time.

### 🐛 Bug fixes

### 💡 Others

## 0.4.0 (2026-08-21)

### 🛠 Breaking changes

### 🎉 New features

- Add `setAsPersistentHomeActivity()`: forces the current activity as the sole Home target via `DevicePolicyManager.addPersistentPreferredActivity`, so pressing Home while locked no longer shows Android's launcher picker (picking a different installed launcher from that picker previously exited lock task mode entirely).

### 🐛 Bug fixes

### 💡 Others

## 0.3.0 (2026-08-20)

### 🛠 Breaking changes

- Bump default Android SDK versions to match `expo-modules-core`'s new defaults: `compileSdkVersion`/`targetSdkVersion` 34 → 36, `minSdkVersion` 21 → 24. Apps that don't override these via their own `rootProject.ext` now need Android 7.0+ (API 24) as their minimum supported OS.

### 🎉 New features

- Add `disableImmersiveMode()`, which restores the system status/navigation bars hidden by `enableImmersiveMode()`.

### 🐛 Bug fixes

- Fix `addToLockTaskMode()` silently succeeding when the app is not the device owner; it now rejects with `IllegalStateException("App is not the device owner.")`, matching `rebootDevice()` and `setLockTaskFeatures()`.
- Fix `setLockTaskFeatures()` crashing with a generic error when called before any Activity is attached, instead of rejecting with `IllegalStateException("App is not the device owner.")`; the device-owner check now uses `context.packageName` instead of `currentActivity.packageName`.
- Fix `enableImmersiveMode()`'s TypeScript type, which was declared `void` but always returns a Promise; it's now typed `Promise<void>` so callers can await/catch it.
- Fix `npm install` of this package as a git dependency failing entirely during `prepare`: `typescript` was never declared as a dependency, so `npx tsc` could resolve an unrelated abandoned `tsc` package from the registry instead of the TypeScript compiler when `typescript` wasn't incidentally hoisted from a sub-dependency. Added `typescript` as an explicit devDependency.

### 💡 Others

- `addToLockTaskMode()` now also allow-lists `com.google.android.captiveportallogin`, so captive portal sign-in pages can display while locked in kiosk mode.
- Update `expo`, `react-native`, `react`, `@types/react`, and `expo-module-scripts` to their latest versions (Expo SDK 57).
- Add `expo-modules-core` as an explicit devDependency; it's no longer reliably hoisted to the top-level `node_modules` and was breaking `src/ExpoDeviceAdminModule.ts`'s import.
- Explicitly type the `LOCK_TASK_FEATURE_*` constants on the native module class, since `NativeModule`'s TS base type dropped its `[key: string]: any` index signature in this `expo-modules-core` version.
- Migrate ESLint config from `.eslintrc.js` to the flat `eslint.config.js` format required by ESLint 9 (bundled by the updated `expo-module-scripts`).
