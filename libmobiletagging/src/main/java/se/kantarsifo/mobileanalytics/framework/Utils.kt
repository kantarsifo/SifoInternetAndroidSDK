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

    fun Context.getApplicationVersion(): String? {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            Logger.error("Failed to retrieve application version, will not set be set in request header")
            null
        }
    }

}