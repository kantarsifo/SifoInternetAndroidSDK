/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.webkit.WebView
import androidx.activity.ComponentActivity

/**
 * Kantar Sifo Mobile Analytics Framework for Android:
 * TSMobileAnalytics.java:
 *
 *
 * This framework will help you to measure usage of your application using Kantar Sifo's services.
 * In order to measure traffic, your application needs to send HTTPS-requests to a server provided by Codigo Analytics.
 * The framework can help you with the whole process, both creating these URLs, as well as sending them to the server.
 */
open class TSMobileAnalytics protected constructor() {

    val libraryVersion: String = BuildConfig.VERSION_NAME

    /**
     * Advanced/debugging: Get all pending requests.
     * This list can be used to handle memory issues, or for debugging purpose.
     *
     * @return The list of pending requests.
     */
    val requestQueue: List<TagDataRequest>
        get() = dataRequestHandler.dataRequestQueue

    /**
     * Advanced/debugging: Get the number of failed requests since the instance was created.
     *
     * @return The number of failed requests.
     */
    val nbrOfFailedRequests: Int
        get() = dataRequestHandler.nbrOfFailedRequests

    /**
     * Advanced/debugging: Get the number of successful requests since the instance was created.
     *
     * @return The number of successful requests.
     */
    val nbrOfSuccessfulRequests: Int
        get() = dataRequestHandler.nbrOfSuccessfulRequests

    internal lateinit var dataRequestHandler: TagDataRequestHandler

    /**
     * The customer ID provided by TNS Sifo or Codigo Analytics.
     * This information will be included in the attribute called “cpid” in the tags sent to the server.
     */
    protected var cpId: String? = null

    /**
     * The name of the application. Only specify the name, the interface will add platform.
     * This value will be sent in tags using the "type"-attribute. Max 243 characters.
     */
    protected var appName: String? = null

    /**
     * You only want to measure TNS-Sifo Panelist users, set true with [.TSMobileAnalytics]
     */
    protected var panelistTrackingOnly = false

    protected var isWebViewBased = false

    /**
     * Call to immediately send a tag to the server using the framework's HTTPS-functionality.
     *
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character
     * set will not be stored properly on the server.
     *
     * The category parameter must not be more than 255 characters.
     * The input strings must not me null.
     * If any of the parameters are invalid the request will fail and return a value greater than 0.
     * If the request is sent successfully, 0 will be returned.
     *
     * @param category   The name of category or page to be tagged. This value will be sent using
     *                   the "cat"-attribute. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    fun sendTag(category: String?): Int {
        return dataRequestHandler.performMetricsRequest(category)
    }

    /**
     * Call to immediately send a tag to the server using the framework's HTTPS-functionality.
     *
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character set will not be stored properly on the server.
     *
     * The category parameter must not be more than 255 characters.
     * The contentID must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid the request will fail and return a value greater than 0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param category   The name of category or page to be tagged. This value will be sent using
     *                   the "cat"-attribute. Max 255 characters.
     * @param contentID  Value to identify specific content within the category, such as a specific article.
     *                   Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    fun sendTag(category: String?, contentID: String?): Int {
        return dataRequestHandler.performMetricsRequest(category, contentID)
    }

    /**
     * Call to immediately send a tag to the server using the framework's HTTPS-functionality.
     *
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character set will not be stored properly on the server.
     *
     * The category list must not contain more than 4 objects and they must not be more than 62 characters each.
     * The contentID must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid the request will fail and return a value greater than 0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param categories    Array of names in category structure. This value will be sent using
     *                      the "cat"-attribute. Max 4 objects with 62 characters each.
     * @param contentID     Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    fun sendTag(categories: Array<String>?, contentID: String?): Int {
        return dataRequestHandler.performMetricsRequest(categories, contentID)
    }

    /**
     * Activate third-party cookies.
     */
    fun activateCookies(webView: WebView) {
        SifoCookieManager.activateCookies(webView)
    }

    /**
     * Advanced/debugging: Add a callback-listener to get notified when a tag request fails or is successful.
     *
     * @param callbackListener The callback-listener implementing the TagDataRequestCallbackListener interface.
     */
    fun setCallbackListener(callbackListener: TagDataRequestCallbackListener?) {
        dataRequestHandler.userCallbackListener = callbackListener
    }

    /**
     * TSMobileAnalytics constructor with Builder class.
     *
     * @param builder new Builder object to specify params
     */
    constructor(builder: Builder) : this() {
        cpId = builder.cpId
        appName = builder.appName
        panelistTrackingOnly = builder.panelistTrackingOnly
        logPrintsActivated = builder.logPrintsActivated
        isWebViewBased = builder.isWebViewBased
    }

    /**
     * Builder class for initialize framework.
     */
    class Builder
    /**
     * Construct a new Builder object to set params.
     */
    {

        var cpId: String? = null
            private set
        var appName: String? = null
            private set
        var panelistTrackingOnly = false
            private set
        var logPrintsActivated = false
            private set
        var isWebViewBased = false
            private set

        /**
         * Specify the cpId you want to set(required).
         *
         * @param cpId  The customer ID provided by TNS Sifo or Codigo Analytics.
         *              This information will be included in the attribute called “cpid” in the tags sent to the server.
         * @return the current builder object.
         */
        fun setCpId(cpId: String?) = apply {
            this.cpId = cpId
        }

        /**
         * Specify the application name you want to set(required).
         *
         * @param appName   The name of the application. Only specify the name, the interface will add platform.
         *                  This value will be sent in tags using the "type"-attribute. Max 243 characters.
         * @return the current builder object.
         */
        fun setApplicationName(appName: String?) = apply {
            this.appName = appName
        }

        /**
         * Specify the panelistTrackingOnly you want to set(not required, default value is false).
         *
         * @param panelistTrackingOnly Set this to true if you wish to track SIFO panelists only.
         * @return the current builder object.
         */
        fun setPanelistTrackingOnly(panelistTrackingOnly: Boolean) = apply {
            this.panelistTrackingOnly = panelistTrackingOnly
        }

        /**
         * Enable logging.
         *
         * @param logPrintsActivated Set this to true to enable logging.
         * @return the current builder object.
         */
        fun setLogPrintsActivated(logPrintsActivated: Boolean) = apply {
            this.logPrintsActivated = logPrintsActivated
        }

        /**
         * Specify the isWebViewBased you want to set(not required, default value is false).
         *
         * @param isWebViewBased Set IsWebViewBased to True if the app’s primary interface is displayed in one or many web views.
         * If web views are used only for content such as privacy policy, terms and conditions etc, set this to false.
         * @return the current builder object.
         */
        fun setIsWebViewBased(isWebViewBased: Boolean) = apply {
            this.isWebViewBased = isWebViewBased
        }

        /**
         * @return return constructor of TSMobileAnalytics.
         */
        fun build() = TSMobileAnalytics(this)

    }

    companion object {

        internal var logPrintsActivated = false
        internal var isWebViewBased = false
        internal var frameworkInstance: TSMobileAnalyticsBackend? = null

        /**
         * Call to get an instance of the framework at any time after initialization.
         * Returns null if createInstance has not been called before.
         *
         * @return The framework instance with your values specified in createInstance, null if not created.
         */
        @JvmStatic
        val instance: TSMobileAnalytics?
            get() = frameworkInstance

        /**
         * Call this method upon application start, for example in the onCreate method of
         * the main Activity, to initialize the framework.
         *
         *
         * The CPID must be 4 or 32 digits, it must not be empty.
         * The Application name must not be more than 243 characters.
         * The parameters must not be null.
         * If any of the parameters are invalid, null will be returned and getInstance() will also return null.
         *
         * @param context         The context of this application, used to get device ID for unique Tagging.
         *                        You can obtain this value using the method getApplicationContext()
         *                        in the Android Activity-class.
         * @param cpID            The customer ID provided by Kantar Sifo or Codigo Analytics.
         *                        This information will be included in the attribute called “cpid” in the tags sent to the server.
         * @param applicationName The name of the application. Only specify the name, the interface will add platform.
         *                        This value will be sent in tags using the "type"-attribute. Max 243 characters.
         * @return The framework instance created with your values. Returns null if creation failed due to invalid parameters.
         */
        @JvmStatic
        fun createInstance(activity: ComponentActivity, cpID: String?, applicationName: String?): TSMobileAnalytics? {
            return TSMobileAnalyticsBackend.createInstance(activity, cpID, applicationName, false, false)
        }

        /**
         * Call this method upon application start if you only want to measure Kantar Sifo Panelist users.
         */
        @JvmStatic
        fun createInstance(activity: ComponentActivity, cpID: String?, applicationName: String?, panelistTrackingOnly: Boolean, isWebViewBased: Boolean): TSMobileAnalytics? {
            return TSMobileAnalyticsBackend.createInstance(activity, cpID, applicationName, panelistTrackingOnly, isWebViewBased)
        }

        /**
         * Call this method to initialize framework with Builder class.
         *
         * @param context The context of this application, used to get device ID for unique Tagging.
        You can obtain this value using the method getApplicationContext() in the Android Activity-class.
         * @param builder new [TSMobileAnalytics.Builder]
         * @return The framework instance created with your values. Returns null if creation failed due to invalid parameters.
         */
        @JvmStatic
        fun createInstance(activity: ComponentActivity, builder: TSMobileAnalytics): TSMobileAnalytics? {
            return createInstance(activity, builder.cpId, builder.appName, builder.panelistTrackingOnly, builder.isWebViewBased)
        }

        /**
         * Activate LogCat prints for the framework.
         *
         * Default is false.
         */
        @JvmStatic
        fun setLogPrintsActivated(printToLog: Boolean) {
            logPrintsActivated = printToLog
        }

        /**
         * Destroy method of current framework.
         * Use this to destroy current framework.
         */
        @JvmStatic
        fun destroyFramework() {
            frameworkInstance = null
        }

    }

}