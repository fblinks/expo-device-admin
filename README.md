# expo-device-admin

An Expo native module implementing Android's `DeviceAdminReceiver` / `DevicePolicyManager` APIs, for building device-owner and kiosk-mode apps.

> **Platform support:** Android only.

## Installation

```bash
npx expo install expo-device-admin
```

This module includes native Android code, so it does not work in Expo Go — use an [Expo development build](https://docs.expo.dev/develop/development-builds/introduction/) or a standalone build.

To use the device-owner-only APIs (`rebootDevice`, `addToLockTaskMode`, `setLockTaskFeatures`), the app must first be provisioned as the device owner, e.g.:

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
await ExpoDeviceAdmin.setLockTaskFeatures(
  ExpoDeviceAdmin.LOCK_TASK_FEATURE_HOME | ExpoDeviceAdmin.LOCK_TASK_FEATURE_OVERVIEW
);
await ExpoDeviceAdmin.rebootDevice();

// Fullscreen / immersive mode
await ExpoDeviceAdmin.enableImmersiveMode();
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
| `setLockTaskFeatures(features: number): Promise<void>` | Sets which system UI features (see constants below) are available while locked. Requires device owner. |
| `rebootDevice(): Promise<void>` | Reboots the device immediately. Requires device owner. |
| `enableImmersiveMode(): Promise<void>` | Hides the system status/navigation bars. |

### Constants

`LOCK_TASK_FEATURE_NONE`, `LOCK_TASK_FEATURE_GLOBAL_ACTIONS`, `LOCK_TASK_FEATURE_HOME`, `LOCK_TASK_FEATURE_OVERVIEW`, `LOCK_TASK_FEATURE_NOTIFICATIONS` — mirror the equivalent [`DevicePolicyManager.LOCK_TASK_FEATURE_*`](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#setLockTaskFeatures(android.content.ComponentName,%20int)) flags and are combined with bitwise OR for `setLockTaskFeatures`.

## Development

```bash
npm run build   # compile src/*.ts
npm run lint     # eslint
npm test         # jest
```

See `CLAUDE.md` for architecture notes on how the TypeScript and Kotlin sides fit together.
