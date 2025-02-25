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

    // Existing function to reboot the device.
    Function("rebootDevice") { promise: Promise ->
      try {
        val dpm = appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(appContext, MinimalDeviceAdminReceiver::class.java)
        dpm.reboot(componentName)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("REBOOT_ERROR", e)
      }
    }

    // New function to set lock task features.
    Function("setLockTaskFeatures") { features: Int, promise: Promise ->
      try {
        val dpm = appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(appContext, MinimalDeviceAdminReceiver::class.java)
        dpm.setLockTaskFeatures(componentName, features)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("SET_FEATURES_ERROR", e)
      }
    }
  }
}
