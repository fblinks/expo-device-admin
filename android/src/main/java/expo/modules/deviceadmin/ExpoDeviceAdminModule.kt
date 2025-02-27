package expo.modules.deviceadmin

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

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

        // Set lock task features and enable kiosk mode
        AsyncFunction("setLockTaskFeatures") { features: Int ->
            val activity = appContext.activityProvider?.currentActivity
                ?: throw IllegalStateException("Current activity is null.")

            val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val componentName = ComponentName(activity.packageName, MinimalDeviceAdminReceiver::class.java.name)

            if (!dpm.isDeviceOwnerApp(activity.packageName)) {
                throw IllegalStateException("App is not the device owner.")
            }

            dpm.setLockTaskFeatures(componentName, features)
            activity.startLockTask()
            enableFullscreenKioskMode(activity) // Ensure full kiosk mode is applied
        }

        // Enable fullscreen mode and hide navigation bar
        AsyncFunction("enableKioskMode") {
            val activity = appContext.activityProvider?.currentActivity
                ?: throw IllegalStateException("Current activity is null.")

            enableFullscreenKioskMode(activity)
        }
        /*
    AsyncFunction("enableKioskMode") {
        val context = appContext.reactContext ?: throw IllegalStateException("React Context is null")
    
        if (context is Activity) {
            context.runOnUiThread {
                val window = context.window
                val decorView = window.decorView
    
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+)
                    val controller = window.insetsController
                    controller?.let {
                        it.hide(WindowInsets.Type.systemBars()) // Hide status and navigation bars
                        it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT // Prevents bars from appearing on swipe
                    }
                } else {
                    // Android 10 and below (API 29-)
                    decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                }
            }
        } else {
            throw IllegalStateException("Context is not an Activity")
        }
    }
    */

    Constants(
      "LOCK_TASK_FEATURE_NONE" to DevicePolicyManager.LOCK_TASK_FEATURE_NONE,
      "LOCK_TASK_FEATURE_GLOBAL_ACTIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS,
      "LOCK_TASK_FEATURE_HOME" to DevicePolicyManager.LOCK_TASK_FEATURE_HOME,
      "LOCK_TASK_FEATURE_OVERVIEW" to DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW,
      "LOCK_TASK_FEATURE_NOTIFICATIONS" to DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
    )
  }
    private fun enableFullscreenKioskMode(activity: Activity) {
        activity.runOnUiThread {
            val window = activity.window
            val decorView = window.decorView

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                controller?.let {
                    it.hide(WindowInsets.Type.systemBars()) // Hide status and navigation bars
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
            }
        }
    }
}
