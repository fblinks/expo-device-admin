# Changelog

## Unpublished

### 🛠 Breaking changes

### 🎉 New features

### 🐛 Bug fixes

- Fix `addToLockTaskMode()` silently succeeding when the app is not the device owner; it now rejects with `IllegalStateException("App is not the device owner.")`, matching `rebootDevice()` and `setLockTaskFeatures()`.

### 💡 Others
