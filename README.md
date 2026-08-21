# expo-device-admin

An Expo native module implementing Android's `DeviceAdminReceiver` / `DevicePolicyManager` APIs, for building device-owner and kiosk-mode apps.

> **Platform support:** Android only.

## Installation

```bash
npx expo install expo-device-admin
```

This module includes native Android code, so it does not work in Expo Go — use an [Expo development build](https://docs.expo.dev/develop/development-builds/introduction/) or a standalone build.

To use the device-owner-only APIs (`rebootDevice`, `addToLockTaskMode`, `setAsPersistentHomeActivity`, `setApplicationHidden`, `setLockTaskFeatures`), the app must first be provisioned as the device owner, e.g.:

```bash
adb shell dpm set-device-owner <your.package.name>/expo.modules.deviceadmin.MinimalDeviceAdminReceiver
```

## Usage

```ts
import ExpoDeviceAdmin from 'expo-device-admin';

// Device ownership
const isOwner = await ExpoDeviceAdmin.isDeviceOwner();

// Kiosk mode (lock task)
ExpoDeviceAdmin.startKioskMode();
ExpoDeviceAdmin.stopKioskMode();
const isKiosk = ExpoDeviceAdmin.checkIfKioskEnabled();

// Device-owner-only APIs
await ExpoDeviceAdmin.addToLockTaskMode();
await ExpoDeviceAdmin.setAsPersistentHomeActivity();
await ExpoDeviceAdmin.setApplicationHidden('com.example.otherlauncher', true);
await ExpoDeviceAdmin.setLockTaskFeatures(
  ExpoDeviceAdmin.LOCK_TASK_FEATURE_HOME | ExpoDeviceAdmin.LOCK_TASK_FEATURE_OVERVIEW
);
await ExpoDeviceAdmin.rebootDevice();

// Fullscreen / immersive mode
await ExpoDeviceAdmin.enableImmersiveMode();
await ExpoDeviceAdmin.disableImmersiveMode();
```

See `example/App.tsx` for a runnable reference of every method below.

## API

| Method | Description |
| --- | --- |
| `isDeviceOwner(): Promise<boolean>` | Whether this app is the device owner. |
| `startKioskMode(): void` | Starts lock task (kiosk) mode on the current activity. |
| `stopKioskMode(): void` | Stops lock task mode. |
| `checkIfKioskEnabled(): boolean` | Whether lock task mode is currently active. |
| `addToLockTaskMode(): Promise<void>` | Allow-lists this app's package for lock task mode. Requires device owner. |
| `setAsPersistentHomeActivity(): Promise<void>` | Forces the current activity as the sole Home target, so pressing Home never shows the launcher picker (and can't exit lock task mode via it). Requires device owner. See note below. |
| `setApplicationHidden(packageName: string, hidden: boolean): Promise<void>` | Hides (or unhides) an installed package fleet-wide — it becomes unavailable to launch or select anywhere, though its data is preserved and unhiding restores it. Requires device owner. See note below. |
| `setLockTaskFeatures(features: number): Promise<void>` | Sets which system UI features (see constants below) are available while locked. Requires device owner. |
| `rebootDevice(): Promise<void>` | Reboots the device immediately. Requires device owner. |
| `enableImmersiveMode(): Promise<void>` | Hides the system status/navigation bars. |
| `disableImmersiveMode(): Promise<void>` | Restores the system status/navigation bars. |

> **Note on `setAsPersistentHomeActivity`:** this only sets the *preference* for which activity handles Home — the app must still declare its own `HOME`/`DEFAULT` intent-filter in its manifest (e.g. via a config plugin) for the activity to be Home-capable in the first place. Call it from whichever activity should become Home. The underlying preference is designed to survive resets on its own, but re-calling it on every app-active resume alongside `addToLockTaskMode()` is still recommended — it's cheap and idempotent, and covers the case where the initial call never happened or failed (e.g. before device-owner provisioning completed).

> **Note on `setApplicationHidden`:** unlike `setAsPersistentHomeActivity`, which only closes the specific "wrong app resolves Home" path, hiding a competing launcher package closes *every* path to it at once (its icon, Recents entries, and Home resolution alike) since the package becomes fully unavailable to launch. Pass the exact package name(s) of the competing launcher(s) present on your fleet's hardware (e.g. an OEM launcher or kids-mode launcher) — this module has no way to discover which other HOME-capable packages are installed on a given device, so it doesn't enumerate or hide anything on its own.

### Constants

`LOCK_TASK_FEATURE_NONE`, `LOCK_TASK_FEATURE_GLOBAL_ACTIONS`, `LOCK_TASK_FEATURE_HOME`, `LOCK_TASK_FEATURE_OVERVIEW`, `LOCK_TASK_FEATURE_NOTIFICATIONS` — mirror the equivalent [`DevicePolicyManager.LOCK_TASK_FEATURE_*`](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#setLockTaskFeatures(android.content.ComponentName,%20int)) flags and are combined with bitwise OR for `setLockTaskFeatures`.

## Development

```bash
npm run build   # compile src/*.ts
npm run lint     # eslint
npm test         # jest
```

See `CLAUDE.md` for architecture notes on how the TypeScript and Kotlin sides fit together.
