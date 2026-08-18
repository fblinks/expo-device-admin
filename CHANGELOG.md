# Changelog

## Unpublished

### 🛠 Breaking changes

### 🎉 New features

### 🐛 Bug fixes

- Fix `addToLockTaskMode()` silently succeeding when the app is not the device owner; it now rejects with `IllegalStateException("App is not the device owner.")`, matching `rebootDevice()` and `setLockTaskFeatures()`.
- Fix `setLockTaskFeatures()` crashing with a generic error when called before any Activity is attached, instead of rejecting with `IllegalStateException("App is not the device owner.")`; the device-owner check now uses `context.packageName` instead of `currentActivity.packageName`.
- Fix `enableImmersiveMode()`'s TypeScript type, which was declared `void` but always returns a Promise; it's now typed `Promise<void>` so callers can await/catch it.

### 💡 Others
