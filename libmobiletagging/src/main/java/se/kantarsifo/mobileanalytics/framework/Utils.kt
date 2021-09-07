package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.content.pm.PackageManager

object Utils {

    fun Context.isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}