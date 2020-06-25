/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpCookie

/**
 * Extension of the framework class containing some extended functions used
 * internally by the framework.
 */
internal class TSMobileAnalyticsBackend : TSMobileAnalytics {

    /**
     * Constructor used internally only.
     * Use createInstance() and getInstance() instead.
     */
    private constructor(context: Context, activity: ComponentActivity, cpId: String, applicationName: String, panelistId: String, trackPanelistOnly: Boolean) : super() {
        dataRequestHandler = TagDataRequestHandler(context, activity, cpId, applicationName, panelistId, trackPanelistOnly)
    }

    /**
     * Constructor used internally only.
     * Use createInstance() and getInstance() instead.
     */
    private constructor(context: Context, activity: ComponentActivity, cpId: String, applicationName: String, cookies: List<HttpCookie>, trackPanelistOnly: Boolean) : super() {
        dataRequestHandler = TagDataRequestHandler(context, activity, cpId, applicationName, cookies, trackPanelistOnly)
    }

    companion object {

        fun createInstance(activity: ComponentActivity, cpID: String?, applicationName: String?, onlyPanelist: Boolean, isWebBased: Boolean): TSMobileAnalyticsBackend? {
            if (activity == null) {
                logFatalError("Mobile Application Tagging Framework Failed to initiate - context must not be null")
                return frameworkInstance
            }
            val sharedPref = activity.getSharedPreferences(TagStringsAndValues.SIFO_PREFERENCE_KEY, Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean(TagStringsAndValues.SIFO_COOKIES_IS_PANELIST_ONLY, onlyPanelist).commit()
            sharedPref.edit().putBoolean(TagStringsAndValues.SIFO_COOKIES_IS_WEB_BASED, isWebBased).commit()

            if (frameworkInstance == null) {
                if (paramsAreValid(cpID, applicationName)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        TSMConfigUtil.syncConfig(activity,applicationName  ?: "")
                    }
                    PanelistHandler.syncCookies(activity, activity) {
                        val requestHandled = initTags(activity, cpID!!, applicationName!!, onlyPanelist)
                        if (!requestHandled) {
                            initLegacyTags(activity, cpID, applicationName, onlyPanelist)
                        }
                    }
                }
            } else {
                logMessage("Mobile Application Tagging Framework already initialized")
                logMessage("Refreshing panelist keys")
                val cookies = PanelistHandler.getCookies(activity, activity)
                if (cookies != null) {
                    frameworkInstance!!.dataRequestHandler.refreshCookies(activity, cookies)
                } else {
                    frameworkInstance!!.dataRequestHandler.refreshCookies(activity, PanelistHandler.getPanelistKey(activity))
                }
            }
            return frameworkInstance
        }

        /**
         * Print text to LogCat following a specific pattern and the tag "MobileAppTagging".
         *
         * @param message The message to print.
         */
        fun logMessage(message: String) {
            if (logPrintsActivated) {
                Log.i("MobileAppTagging", message)
                Log.i("MobileAppTagging", "***********************************")
            }
        }

        /**
         * Print an error message to LogCat following a specific pattern and the tag "MobileAppTagging"
         * only if logs are activated.
         *
         * @param message The error message.
         */
        fun logError(message: String) {
            if (logPrintsActivated) {
                Log.e("MobileAppTagging", "***********************************")
                Log.e("MobileAppTagging", message)
                Log.e("MobileAppTagging", "***********************************")
            }
        }

        /**
         * Print an error message to LogCat following a specific pattern and the tag "MobileAppTagging".
         *
         * @param message The error message.
         */
        fun logFatalError(message: String) {
            Log.e("MobileAppTagging", "***********************************")
            Log.e("MobileAppTagging", message)
            Log.e("MobileAppTagging", "***********************************")
        }

        private fun paramsAreValid(cpID: String?, applicationName: String?): Boolean {
            return when {
                (cpID.isNullOrEmpty()) -> {
                    logFatalError("Mobile Application Tagging Framework Failed to initiate - CPID must not be null or empty")
                    false
                }
                (cpID.length != TagStringsAndValues.CPID_LENGTH_CODIGO) -> {
                    logFatalError("Mobile Application Tagging Framework Failed to initiate - CPID must be " +
                            "${TagStringsAndValues.CPID_LENGTH_CODIGO} characters")
                    false
                }
                (applicationName.isNullOrEmpty()) -> {
                    logFatalError("Mobile Application Tagging Framework Failed to initiate - Application Name must not be null or empty")
                    false
                }
                (applicationName.length > TagStringsAndValues.MAX_LENGTH_APP_NAME) -> {
                    logFatalError("Mobile Application Tagging Framework Failed to initiate - Application Name must not have more than "
                            + TagStringsAndValues.MAX_LENGTH_APP_NAME + " characters")
                    false
                }
                else -> true
            }
        }

        private fun initTags(activity: ComponentActivity, cpID: String, applicationName: String, onlyPanelist: Boolean): Boolean {
            val cookies = PanelistHandler.getCookies(activity, activity) ?: return false
            if (onlyPanelist && cookies.isEmpty()) {
                logFatalError("Mobile Application Tagging Framework Failed to initiate - " +
                        "Cookies file was empty, panelist id not found")
            } else {
                frameworkInstance = TSMobileAnalyticsBackend(activity, activity, cpID, applicationName, cookies, onlyPanelist)
                logMessage("Mobile Application Tagging Framework initiated with the following values " +
                        "\nCPID: $cpID\nApplication name: $applicationName\nOnly panelist tracking : $onlyPanelist")
            }
            return true
        }

        private fun initLegacyTags(activity: ComponentActivity, cpID: String, applicationName: String, onlyPanelist: Boolean) {
            val panelistKey = PanelistHandler.getPanelistKey(activity)
            if (cpID.length != TagStringsAndValues.CPID_LENGTH_CODIGO) {
                logFatalError("Mobile Application Tagging Framework Failed to initiate - " +
                        "CPID must either be exactly " + TagStringsAndValues.CPID_LENGTH_CODIGO)
            } else if (onlyPanelist && panelistKey == TagStringsAndValues.NO_PANELIST_ID) {
                logFatalError("Mobile Application Tagging Framework Failed to initiate - " +
                        "Panelist Id was not found, it must exist if only panelist tracking is active")
            } else {
                // TODO print panelist setting
                frameworkInstance = TSMobileAnalyticsBackend(activity, activity, cpID, applicationName, panelistKey, onlyPanelist)
                logMessage("Mobile Application Tagging Framework initiated with the following values " +
                        "\nCPID: $cpID\nApplication name: $applicationName\nOnly panelist tracking : $onlyPanelist")
            }
        }

    }

}
