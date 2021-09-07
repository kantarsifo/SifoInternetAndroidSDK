package se.kantarsifo.mobileanalytics.framework

import android.util.Log

object Logger {
    const val TAG = "MobileAppTagging"
    fun log(message: String) {
        if (TSMobileAnalytics.logPrintsActivated) {
            Log.d(TAG, "***********************************")
            Log.d(TAG, message)
            Log.d(TAG, "***********************************")
        }
    }

    fun error(message: String) {
        if (TSMobileAnalytics.logPrintsActivated) {
            Log.e(TAG, "***********************************")
            Log.e(TAG, message)
            Log.e(TAG, "***********************************")
        }
    }

    fun fatalError(message: String) {
        Log.e(TAG, "***********************************")
        Log.e(TAG, message)
        Log.e(TAG, "***********************************")
    }

}