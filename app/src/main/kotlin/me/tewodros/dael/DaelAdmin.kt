package me.tewodros.dael

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context

/**
 * Device admin receiver. Only matters on a dedicated phone where the app has been made
 * device owner via `adb shell dpm set-device-owner`; then lock task mode cannot be escaped.
 */
class DaelAdmin : DeviceAdminReceiver() {
    companion object {
        fun isDeviceOwner(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isDeviceOwnerApp(context.packageName)
        }

        /** Allow this package to enter lock task mode without the pinning prompt. */
        fun allowLockTask(context: Context) {
            if (!isDeviceOwner(context)) return
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = android.content.ComponentName(context, DaelAdmin::class.java)
            runCatching { dpm.setLockTaskPackages(admin, arrayOf(context.packageName)) }
        }
    }
}
