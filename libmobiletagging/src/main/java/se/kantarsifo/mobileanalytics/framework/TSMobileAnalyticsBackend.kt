/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.kantarsifo.mobileanalytics.framework.Logger.fatalError
import se.kantarsifo.mobileanalytics.framework.Logger.log
import se.kantarsifo.mobileanalytics.framework.TagStringsAndValues.SIFO_APP_START_EVENT_CATEGORY
import se.kantarsifo.mobileanalytics.framework.Utils.getApplicationVersion
import se.kantarsifo.mobileanalytics.framework.Utils.isPackageInstalled
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
    private constructor(context: Context, activity: ComponentActivity, cpId: String, applicationName: String, cookies: List<HttpCookie>?, trackPanelistOnly: Boolean,twaInfo: TWAModel,isWebViewBased: Boolean) : super() {
        dataRequestHandler = TagDataRequestHandler(context, activity, cpId, applicationName, cookies, trackPanelistOnly,twaInfo,isWebViewBased)
    }

    companion object {

        fun createInstance(activity: ComponentActivity, cpID: String?, applicationName: String?, onlyPanelist: Boolean, isWebBased: Boolean = false,twaInfo: TWAModel = TWAModel()): TSMobileAnalyticsBackend? {
            if (activity == null) {
                fatalError("Mobile Application Tagging Framework Failed to initiate - context must not be null")
                return frameworkInstance
            }
            val sdkVersion = BuildConfig.VERSION_NAME
            val appVersion = activity.getApplicationVersion()
            val cookieValue = "trackPanelistOnly=$onlyPanelist&isWebViewBased=$isWebBased&sdkVersion=$sdkVersion&appVersion=$appVersion"
            val metaCookie = CookieHandler.createHttpCookie(TagStringsAndValues.SIFO_META_COOKIE_NAME, cookieValue)
            CookieHandler.setupPanelistCookies(listOf(metaCookie))
            if (paramsAreValid(cpID, applicationName)) {
                if (frameworkInstance == null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        TSMConfigUtil.syncConfig(activity, applicationName ?: "")
                    }

                    initTags(activity, cpID!!, applicationName!!, onlyPanelist, isWebViewBased,twaInfo)

                    sendAppStartEvent()

                    PanelistHandler.syncCookies(activity, activity) {
                        refreshCookiesAndKeys(activity,onlyPanelist)
                        frameworkInstance?.dataRequestHandler?.setStateReady()
                    }
                } else {
                    log("Mobile Application Tagging Framework already initialized")
                    refreshCookiesAndKeys(activity, onlyPanelist)
                }
            }
            return frameworkInstance
        }


        private fun refreshCookiesAndKeys(activity: ComponentActivity, onlyPanelist: Boolean) {
            val isInstalled = activity.isPackageInstalled(TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME_V2)
            if (!isInstalled) {
                if (onlyPanelist && !isInstalled) {
                    frameworkInstance = null
                    fatalError("To track panelists only you need to have the internet app installed")
                }
                //No need to refresh the cookies since there is no panelist app to get the cookies from
                return
            }
            log("Refreshing panelist keys(Cookies)")
            val cookies = PanelistHandler.getCookies(activity, activity)
            frameworkInstance?.dataRequestHandler?.apply {
                if (cookies != null) {
                    refreshCookies(cookies)
                } else {
                    val panelistKey = PanelistHandler.getPanelistKey(activity)
                    if (onlyPanelist && panelistKey == TagStringsAndValues.NO_PANELIST_ID) {
                        fatalError("Mobile Application Tagging Framework Failed to initiate - " +
                                "Panelist Id was not found, it must exist if only panelist tracking is active")
                        return
                    }
                    refreshCookies(panelistKey)
                }
            }
        }

        private fun paramsAreValid(cpID: String?, applicationName: String?): Boolean {
            return when {
                (cpID.isNullOrEmpty()) -> {
                    fatalError("Mobile Application Tagging Framework Failed to initiate - CPID must not be null or empty")
                    false
                }
                (cpID.length != TagStringsAndValues.CPID_LENGTH_CODIGO) -> {
                    fatalError("Mobile Application Tagging Framework Failed to initiate - CPID must be " +
                            "${TagStringsAndValues.CPID_LENGTH_CODIGO} characters")
                    false
                }
                (applicationName.isNullOrEmpty()) -> {
                    fatalError("Mobile Application Tagging Framework Failed to initiate - Application Name must not be null or empty")
                    false
                }
                (applicationName.length > TagStringsAndValues.MAX_LENGTH_APP_NAME) -> {
                    fatalError("Mobile Application Tagging Framework Failed to initiate - Application Name must not have more than "
                            + TagStringsAndValues.MAX_LENGTH_APP_NAME + " characters")
                    false
                }
                else -> true
            }
        }


        private fun initTags(
            activity: ComponentActivity,
            cpID: String,
            applicationName: String,
            onlyPanelist: Boolean,
            isWebViewBased: Boolean,
            twaInfo: TWAModel
        ): Boolean {
            frameworkInstance = TSMobileAnalyticsBackend(activity, activity, cpID, applicationName, null, onlyPanelist,twaInfo,isWebViewBased)
            log("Mobile Application Tagging Framework initiated with the following values " +
                        "\nCPID: $cpID\nApplication name: $applicationName\nOnly panelist tracking : $onlyPanelist")
            return true
        }

        /**
         * Sends the app start event
         */
        private fun sendAppStartEvent() {
            if (frameworkInstance ==null) {
                log("Cannot create app start event")
                return
            }

            frameworkInstance?.sendTag(SIFO_APP_START_EVENT_CATEGORY)
        }

    }


}
