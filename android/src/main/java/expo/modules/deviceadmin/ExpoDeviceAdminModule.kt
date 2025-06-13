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

    private val componentName: ComponentName
        get() = ComponentName(context, MinimalDeviceAdminReceiver::class.java) as ComponentName

    private val isLockTaskModeRunning: Boolean
        @SuppressLint("ObsoleteSdkInt")
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED ||
                        activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_PINNED
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Deprecated in API level 23.
                activityManager.isInLockTaskMode
            } else {
                false
            }
        }

    override fun definition() = ModuleDefinition {
        Name("ExpoDeviceAdmin")

        /** Checks if app is device owner */
        AsyncFunction("isDeviceOwner") { dpm.isDeviceOwnerApp(context.packageName) }

        /** Starts kiosk mode */
        Function("startKioskMode") { currentActivity.startLockTask() }

        /** Exits kiosk mode */
        Function("stopKioskMode") { currentActivity.stopLockTask() }

        /** Checks if running in kiosk mode */
        Function("checkIfKioskEnabled") {
            return@Function isLockTaskModeRunning
        }

        /** Reboots the device */
        AsyncFunction("rebootDevice") {
            if (!dpm.isDeviceOwnerApp(context.packageName)) {
                throw SecurityException("App must be a device owner to reboot the device.")
            }

            // val componentName =
            //         ComponentName(context, MinimalDeviceAdminReceiver::class.java)

            dpm.reboot(componentName)
        }

        /** Adds current package to lock task */
        AsyncFunction("addToLockTaskMode") {
            // val componentName = ComponentName(context, MinimalDeviceAdminReceiver::class.java)
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setLockTaskPackages(componentName, arrayOf(context.packageName, "com.google.android.captiveportallogin"))
            }
        }

        /**
         * Sets enabled features duting kiosk mode (lock task)
         * https://developer.android.com/reference/android/app/admin/DevicePolicyManager#setLockTaskFeatures(android.content.ComponentName,%20int)
         */
        AsyncFunction("setLockTaskFeatures") { features: Int ->

            // val dpm =
            //         activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as
            // DevicePolicyManager
            // val componentName =
            //         ComponentName(activity.packageName,
            // MinimalDeviceAdminReceiver::class.java.name)

            if (!dpm.isDeviceOwnerApp(currentActivity.packageName)) {
                throw IllegalStateException("App is not the device owner.")
            }

            dpm.setLockTaskFeatures(componentName, features)
        }

        /** Enables fullscreen mode and hides system bars */
        AsyncFunction("enableImmersiveMode") {
            // val activity =
            //         appContext.activityProvider?.currentActivity
            //                 ?: throw IllegalStateException("Current activity is null.")

            // enableImmersiveMode(currentActivity)
            currentActivity.runOnUiThread {
                val window = currentActivity.window
                val decorView = window.decorView

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+ (API 30+)
                    val controller = window.insetsController
                    controller?.let {
                        it.hide(WindowInsets.Type.systemBars()) // Hide status and navigation bars
                        it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                    }
                } else { // Android 10 and below (API 29-)
                    decorView.systemUiVisibility =
                            (View.SYSTEM_UI_FLAG_IMMERSIVE or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                }
            }
        }

        AsyncFunction("disableImmersiveMode") {
           currentActivity.runOnUiThread {
              val decorView = currentActivity.window.decorView
              decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
           }
        }

        Constants(
                "LOCK_TASK_FEATURE_NONE" to DevicePolicyManager.LOCK_TASK_FEATURE_NONE,
                "LOCK_TASK_FEATURE_GLOBAL_ACTIONS" to
                        DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS,
                "LOCK_TASK_FEATURE_HOME" to DevicePolicyManager.LOCK_TASK_FEATURE_HOME,
                "LOCK_TASK_FEATURE_OVERVIEW" to DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW,
                "LOCK_TASK_FEATURE_NOTIFICATIONS" to
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
        )
    }
    // private fun enableImmersiveMode(activity: Activity) {
    //     activity.runOnUiThread {
    //         val window = activity.window
    //         val decorView = window.decorView

    //         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+ (API 30+)
    //             val controller = window.insetsController
    //             controller?.let {
    //                 it.hide(WindowInsets.Type.systemBars()) // Hide status and navigation bars
    //                 it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
    //             }
    //         } else {// Android 10 and below (API 29-)
    //             decorView.systemUiVisibility =
    //                     (View.SYSTEM_UI_FLAG_IMMERSIVE or
    //                             View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
    //                             View.SYSTEM_UI_FLAG_FULLSCREEN or
    //                             View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
    //                             View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
    //                             View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    //         }
    //     }
    // }
}
