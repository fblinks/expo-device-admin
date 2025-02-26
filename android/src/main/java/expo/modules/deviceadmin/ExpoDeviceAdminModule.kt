package expo.modules.deviceadmin

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.app.Activity


class ExpoDeviceAdminModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoDeviceAdmin")

    AsyncFunction("isDeviceOwner") {
      val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
      val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
      dpm.isDeviceOwnerApp(context.packageName)
    }

    AsyncFunction("rebootDevice") {
      val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
      val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

      if (!dpm.isDeviceOwnerApp(context.packageName)) {
        throw SecurityException("App must be a device owner to reboot the device.")
      }

      val componentName = ComponentName(context.packageName, MinimalDeviceAdminReceiver::class.java.name)

      dpm.reboot(componentName)
    }

    AsyncFunction("setLockTaskFeatures") { features: Int ->
        //try {
            val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(context.packageName, MinimalDeviceAdminReceiver::class.java.name)
    
            // Check if app is the device owner
            if (!dpm.isDeviceOwnerApp(context.packageName)) {
                throw IllegalStateException("App is not the device owner.")
            }
    
            // Set the lock task features
            dpm.setLockTaskFeatures(componentName, features)
    
            // Ensure context is an instance of Activity before calling startLockTask
            if (context is Activity) {
                context.startLockTask()  // Start lock task mode
            } //else {
                //throw IllegalStateException("Context is not an Activity")
            //}
        //} catch (e: Exception) {
            // Handle errors by rejecting the promise or logging them
            //log.error("Error setting lock task features: ${e.message}")
            // Handle rejection if it's a promise-based function
            //promise.reject("SET_LOCK_TASK_FEATURES_ERROR", "Error setting lock task features", e)
        //}
    }

    Constants(
      "LOCK_TASK_FEATURE_NONE" to DevicePolicyManager.LOCK_TASK_FEATURE_NONE,
      "LOCK_TASK_FEATURE_GLOBAL_ACTIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS,
      "LOCK_TASK_FEATURE_HOME" to DevicePolicyManager.LOCK_TASK_FEATURE_HOME,
      "LOCK_TASK_FEATURE_OVERVIEW" to DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW,
      "LOCK_TASK_FEATURE_NOTIFICATIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
    )
  }
}
