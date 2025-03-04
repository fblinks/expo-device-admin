package expo.modules.deviceadmin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
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

    private val context: Context
    get() = requireNotNull(appContext.reactContext)

  private val currentActivity: android.app.Activity
    get() = requireNotNull(appContext.currentActivity)

  private val activityManager: ActivityManager
    get() = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val dpm: DevicePolicyManager
    get() = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val isLockTaskModeRunning: Boolean
    @SuppressLint("ObsoleteSdkInt")
    get() {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activityManager.lockTaskModeState ==
                ActivityManager.LOCK_TASK_MODE_LOCKED
                || activityManager.lockTaskModeState ==
                ActivityManager.LOCK_TASK_MODE_PINNED
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // Deprecated in API level 23.
        activityManager.isInLockTaskMode
      } else {
        false
      }
    }
    
  override fun definition() = ModuleDefinition {
    Name("ExpoDeviceAdmin")

    AsyncFunction("isDeviceOwner") {
      dpm.isDeviceOwnerApp(context.packageName)
    }

    Function("exitKioskMode") {
        currentActivity.stopLockTask()
    }

    Function("startKioskMode") {
        currentActivity.startLockTask()
    }

    Function("checkIfKioskEnabled") {
        return@Function isLockTaskModeRunning
    }

    AsyncFunction("rebootDevice") {
      if (!dpm.isDeviceOwnerApp(context.packageName)) {
        throw SecurityException("App must be a device owner to reboot the device.")
      }

      val componentName = ComponentName(context.packageName, MinimalDeviceAdminReceiver::class.java.name)

      dpm.reboot(componentName)
    }

    AsyncFunction("lockTaskMode") {
            val componentName = ComponentName(context, MinimalDeviceAdminReceiver::class.java)
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setLockTaskPackages(componentName, arrayOf(context.packageName))
            }
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
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            } else {
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
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
