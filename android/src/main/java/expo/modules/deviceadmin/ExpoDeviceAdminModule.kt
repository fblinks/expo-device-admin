package expo.modules.deviceadmin

import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

class ExpoDeviceAdminModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoDeviceAdmin")

    Function("rebootDevice") { promise: Promise ->
      try {
        val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, MinimalDeviceAdminReceiver::class.java)
        dpm.reboot(componentName)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("REBOOT_ERROR", e)
      }
    }

    Function("setLockTaskFeatures") { features: Int, promise: Promise ->
      try {
        val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, MinimalDeviceAdminReceiver::class.java)

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
          promise.reject("NOT_DEVICE_OWNER", "App is not the device owner.")
          return@Function
        }

        dpm.setLockTaskFeatures(componentName, features)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("SET_FEATURES_ERROR", e)
      }
    }

    Function("lockEverythingExceptPowerButton") { promise: Promise ->
      try {
        val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, MinimalDeviceAdminReceiver::class.java)

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
          promise.reject("NOT_DEVICE_OWNER", "App is not the device owner.")
          return@Function
        }

        // Enable only the Global Actions (power button menu)
        dpm.setLockTaskFeatures(componentName, DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("SET_FEATURES_ERROR", e)
      }
    }

    Constants {
      "LOCK_TASK_FEATURE_NONE" to DevicePolicyManager.LOCK_TASK_FEATURE_NONE
      "LOCK_TASK_FEATURE_GLOBAL_ACTIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
      "LOCK_TASK_FEATURE_HOME" to DevicePolicyManager.LOCK_TASK_FEATURE_HOME
      "LOCK_TASK_FEATURE_OVERVIEW" to DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW
      "LOCK_TASK_FEATURE_NOTIFICATIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
    }
  }
}
