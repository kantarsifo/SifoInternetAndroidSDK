/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo, Sweden,       *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebView;
import java.util.List;

/**
 * Kantar Sifo Mobile Analytics Framework for Android:
 * TSMobileAnalytics.java:
 * <p/>
 * This framework will help you to measure usage of your application using Kantar Sifo's services.
 * In order to measure traffic, your application needs to send HTTP/s-requests to a server provided by Mobiletech or Codigo Analytics.
 * The framework can help you with the whole process, both creating these URLs, as well as sending them to the server.
 */
public class TSMobileAnalytics {

    /**
     * Note for developer: update it when you change your library version
     * @return library version
     */
    public String getLibraryVersion(){
        return "3.0.0";
    }

    /**
     * Default constructor.
     */
    protected TSMobileAnalytics() {
    }

    /**
     * Call this method upon application start, for example in the onCreate-method of
     * the main Activity, to initialize the framework.
     * <p/>
     * The CPID must be 4 or 32 digits, it must not be empty.
     * The Application name must not be more than 243 characters.
     * The parameters must not be null.
     * If any of the parameters are invalid, null will be returned and getInstance() will also return null.
     *
     * @param context         The context of this application, used to get device ID for unique Tagging.
     *                        You can obtain this value using the method getApplicationContext() in the Android Activity-class.
     * @param cpID            The customer ID provided by Kantar Sifo, Mobiletech or Codigo Analytics.
     *                        This information will be included in the attribute called “cpid” in the tags sent to the server.
     * @param applicationName The name of the application. Only specify the name, the interface will add platform.
     *                        This value will be sent in tags using the "type"-attribute. Max 243 characters.
     * @return The framework instance created with your values. Returns null if creation failed due to invalid parameters.
     */
    public static TSMobileAnalytics createInstance(Context context, String cpID, String applicationName) {
        return TSMobileAnalyticsBackend.createInstance(context, cpID, applicationName, false);
    }

    /**
     * Call this method upon application start if you only want to measure Kantar Sifo Panelist users.
     */
    public static TSMobileAnalytics createInstance(Context context, String cpID, String applicationName, boolean panelistTrackingOnly) {
        return TSMobileAnalyticsBackend.createInstance(context, cpID, applicationName, panelistTrackingOnly);
    }

    /**
     * Call this method to initialize framework with Builder class.
     *
     * @param builder new {@link TSMobileAnalytics.Builder}
     * @return The framework instance created with your values. Returns null if creation failed due to invalid parameters.
     */
    public static TSMobileAnalytics createInstance(TSMobileAnalytics builder) {
        return createInstance(builder.context, builder.cpId, builder.appName, builder.panelistTrackingOnly);
    }

    /**
     * Call to get an instance of the framework at any time after initialization.
     * Returns null if createInstance has not been called before.
     *
     * @return The framework instance with your values specified in createInstance, null if not created.
     */
    public static TSMobileAnalytics getInstance() {
        return frameworkInstance;
    }

    /**
     * Call to immediately send a tag to the server using the framework's http/s-functionality.
     * <p/>
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character
     * set will not be stored properly on the server.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The input strings must not me null.
     * If any of the parameters are invalid the request will fail and return a value >0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param category The name of category or page to be tagged. This value will be sent using
     *                 the "cat"-attribute. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    public int sendTag(String category) {
        return dataRequestHandler.performMetricsRequest(category);
    }

    /**
     * Call to immediately send a tag to the server using the framework's http/s-functionality.
     * <p/>
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character set will not be stored properly on the server.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The contentID must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid the request will fail and return a value >0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param category  The name of category or page to be tagged. This value will be sent using
     *                  the "cat"-attribute. Max 255 characters.
     * @param contentID Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    public int sendTag(String category, String contentID) {
        return dataRequestHandler.performMetricsRequest(category, contentID);
    }

    /**
     * Call to immediately send a tag to the server using the framework's http/s-functionality.
     * <p/>
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character set will not be stored properly on the server.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The contentID must not be more than 255 characters.
     * The contentName must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid the request will fail and return a value >0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param category    The name of category or page to be tagged. This value will be sent using
     *                    the "cat"-attribute. Max 255 characters.
     * @param contentID   Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @param contentName Name to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    public int sendTag(String category, String contentName, String contentID) {
        return dataRequestHandler.performMetricsRequest(category, contentID, contentName);
    }

    /**
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated Please use {@link #sendTag(String, String, String)}.
     */
    @Deprecated
    public int sendTag(String categories, String deprecated, String contentID, String contentName) {
        return dataRequestHandler.performMetricsRequest(categories, contentID, contentName);
    }

    /**
     * Call to immediately send a tag to the server using the framework's http/s-functionality.
     * <p/>
     * Use when a page is displayed, for example in the onResume-method of an Activity.
     * Strings will be encoded using Latin1 (ISO8859-1) and characters not supported in this character set will not be stored properly on the server.
     * <p/>
     * The category list must not contain more than 4 objects and they must not be more than 62 characters each.
     * The contentID must not be more than 255 characters.
     * The contentName must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid the request will fail and return a value >0.
     * If the request is sent successfully 0 will be returned.
     *
     * @param categories  Array of names in category structure. This value will be sent using
     *                    the "cat"-attribute. Max 4 objects with 62 characters each.
     * @param contentID   Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @param contentName Name to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return 0 if request is sent successfully, otherwise a value bigger than 0.
     */
    public int sendTag(String[] categories, String contentName, String contentID) {
        return dataRequestHandler.performMetricsRequest(categories, contentID, contentName);
    }

    /**
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated Please use {@link #sendTag(String[], String, String)}.
     */
    @Deprecated
    public int sendTag(String[] categories, String deprecated, String contentID, String contentName) {
        return dataRequestHandler.performMetricsRequest(categories, contentID, contentName);
    }

    /**
     * Create a URL to use to send a tag to the server, if you want to make the request manually.
     * <p/>
     * You only need to use this method if you are making the HTTP/s-request yourself. If you want the framework
     * to make the HTTP/s-request for you, you only need to use the "sendTag"-method.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The input strings must not me null.
     * If any of the parameters are invalid null will be returned.
     *
     * @param category The name of category or page to be tagged. This value will be sent using
     *                 the "cat"-attribute. Max 255 characters.
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String)} instead.
     */
    @Deprecated
    public String getURL(String category) {
        return dataRequestHandler.getURL(category);
    }

    /**
     * Create a URL to use to send a tag to the server, if you want to make the request manually.
     * <p/>
     * You only need to use this method if you are making the HTTP/s-request yourself. If you want the framework
     * to make the HTTP/s-request for you, you only need to use the "sendTag"-method.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The contentID must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid null will be returned.
     *
     * @param category  The name of category or page to be tagged. This value will be sent using
     *                  the "cat"-attribute. Max 255 characters.
     * @param contentID Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String, String)} instead.
     */
    @Deprecated
    public String getURL(String category, String contentID) {
        return dataRequestHandler.getURL(category, contentID);
    }

    /**
     * Create a URL to use to send a tag to the server, if you want to make the request manually.
     * <p/>
     * You only need to use this method if you are making the HTTP/s-request yourself. If you want the framework
     * to make the HTTP/s-request for you, you only need to use the "sendTag"-method.
     * <p/>
     * The category parameter must not be more than 255 characters.
     * The contentID must not be more than 255 characters.
     * The contentName must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid null will be returned.
     *
     * @param category    The name of category or page to be tagged. This value will be sent using
     *                    the "cat"-attribute. Max 255 characters.
     * @param contentID   Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @param contentName Name to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String, String, String)} instead.
     */
    @Deprecated
    public String getURL(String category, String contentName, String contentID) {
        return dataRequestHandler.getURL(category, contentID, contentName);
    }

    /**
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String, String, String)} instead.
     */
    @Deprecated
    public String getURL(String category, String deprecated, String contentID, String contentName) {
        return dataRequestHandler.getURL(category, contentID, contentName);
    }

    /**
     * Create a URL to use to send a tag to the server, if you want to make the request manually.
     * <p/>
     * You only need to use this method if you are making the HTTP/s-request yourself. If you want the framework
     * to make the HTTP/s-request for you, you only need to use the "sendTag"-method.
     * <p/>
     * The category list must not contain more than 4 objects and they must not be more than 62 characters each.
     * The contentID must not be more than 255 characters.
     * The contentName must not be more than 255 characters.
     * The input strings must not be null.
     * If any of the parameters are invalid null will be returned.
     *
     * @param category    Array of names in category structure. This value will be sent using
     *                    the "cat"-attribute. Max 4 objects with 62 characters each.
     * @param contentID   Value to identify specific content within the category, such as a specific article. Max 255 characters.
     * @param contentName Name to identify specific content within the category, such as a specific article. Max 255 characters.
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String[], String, String)} instead.
     */
    @Deprecated
    public String getURL(String[] category, String contentName, String contentID) {
        return dataRequestHandler.getURL(category, contentID, contentName);
    }

    /**
     * @return The String holding the created URL. NULL if unsuccessful.
     * @deprecated This doesn't take into account some important cookies
     * and support is planned to be dropped in future versions.
     * Please use {@link #sendTag(String[], String, String)} instead.
     */
    @Deprecated
    public String getURL(String[] category, String deprecated, String contentID, String contentName) {
        return dataRequestHandler.getURL(category, contentID, contentName);
    }

    /**
     * Activate framework cookies for the given WebView.
     * This enables the third party cookie option on the WebView.
     * <p/>
     * This function needs to be called once for every WebView before it can be guaranteed
     * to pass on the proper cookies. This is especially important from API 21 onwards.
     *
     * @param webView The WebView that should use cookies from the framework.
     * @deprecated Framework uses java.net.Cookie library now
     * Please use {@link #activateCookies()} instead.
     */
    @Deprecated
    public void activateCookies(WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

    /**
     * Activate third-party cookies.
     */
    public void activateCookies() {
        SifoCookieManager.getInstance().activeCookies();
    }

    /**
     * Activate LogCat prints for the framework.
     *
     * @deprecated This library uses Builder to initialize.
     * Please use {@link #TSMobileAnalytics(Builder)} instead.
     * Default is false.
     */
    @Deprecated
    public static void setLogPrintsActivated(boolean printToLog) {
        logPrintsActivated = printToLog;
    }

    /**
     * Activate https url for send data. Default is true.
     *
     * @deprecated This library uses Builder to initialize.
     * Please use {@link #TSMobileAnalytics(Builder)} instead.
     */
    public static void useHttps(boolean https) {
        useHttpsActivated = https;
    }

    /***** Advanced methods *****/
    /**
     * Advanced/debugging: Get all pending requests.
     * This list can be used to handle memory issues, or for debugging purpose.
     *
     * @return The list of pending requests.
     */
    public List<TagDataRequest> getRequestQueue() {
        return dataRequestHandler.getDataRequestQueue();
    }

    /**
     * Advanced/debugging: Get the number of failed requests since the instance was created.
     *
     * @return The number of failed requests.
     */
    public int getNbrOfFailedRequests() {
        return dataRequestHandler.getNbrOfFailedRequests();
    }

    /**
     * Advanced/debugging: Get the number of successful requests since the instance was created.
     *
     * @return The number of successful requests.
     */
    public int getNbrOfSuccessfulRequests() {
        return dataRequestHandler.getNbrOfSuccessfulRequests();
    }

    /**
     * Advanced/debugging: Add a callback-listener to get notified when a tag request fails or is successful.
     *
     * @param callbackListener The callback-listener implementing the TagDataRequestCallbackListener interface.
     */
    public void setCallbackListener(TagDataRequestCallbackListener callbackListener) {
        dataRequestHandler.setCallbackListener(callbackListener);
    }

    /***** End of public methods *****/

    /**
     * Our framework instance.
     */
    protected static TSMobileAnalyticsBackend frameworkInstance = null;

    /**
     * Our TagDataRequestHandler.
     */
    protected TagDataRequestHandler dataRequestHandler;

    /**
     * Are logs enabled?
     */
    protected static boolean logPrintsActivated = false;

    /**
     * Send data with https url.
     */
    protected static boolean useHttpsActivated = true;

    /**
     * The customer ID provided by TNS Sifo, Mobiletech or Codigo Analytics.
     * This information will be included in the attribute called “cpid” in the tags sent to the server.
     */
    protected String cpId;

    /**
     * The name of the application. Only specify the name, the interface will add platform.
     * This value will be sent in tags using the "type"-attribute. Max 243 characters.
     */
    protected String appName;

    /**
     * You only want to measure TNS-Sifo Panelist users, set true with {@link #TSMobileAnalytics(Builder)}
     */
    protected boolean panelistTrackingOnly = false;

    /**
     * The context of this application, used to get device ID for unique Tagging.
     * You can obtain this value using the method getApplicationContext() in the Android Activity-class.
     */
    protected Context context;


    /**
     * TSMobileAnalytics constructor with Builder class.
     *
     * @param builder new Builder object to specify params
     */
    public TSMobileAnalytics(Builder builder) {
        this.context = builder.context;
        this.cpId = builder.cpId;
        this.appName = builder.appName;
        this.panelistTrackingOnly = builder.panelistTrackingOnly;
        useHttpsActivated = builder.useHttpsActivated;
        logPrintsActivated = builder.logPrintsActivated;
    }

    /**
     * Destroy method of current framework.
     * Use this to destroy current framework.
     */
    public static void destroyFramework() {
        frameworkInstance = null;
    }


    /**
     * Builder class for initialize framework.
     */
    public static class Builder {
        private final Context context;
        private String cpId;
        private String appName;
        private boolean panelistTrackingOnly = false;
        private boolean logPrintsActivated = false;
        private boolean useHttpsActivated = true;


        /**
         * Construct a new Builder object to set params.
         *
         * @param context The context of this application, used to get device ID for unique Tagging.
         *                You can obtain this value using the method getApplicationContext() in the Android Activity-class.
         */
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Specify the cpId you want to set(required).
         *
         * @param cpId The customer ID provided by TNS Sifo, Mobiletech or Codigo Analytics.
         *             This information will be included in the attribute called “cpid” in the tags sent to the server.
         * @return the current builder object.
         */
        public Builder setCpId(String cpId) {
            this.cpId = cpId;
            return this;
        }

        /**
         * Specify the application name you want to set(required).
         *
         * @param appName The name of the application. Only specify the name, the interface will add platform.
         *                This value will be sent in tags using the "type"-attribute. Max 243 characters.
         * @return the current builder object.
         */
        public Builder setApplicationName(String appName) {
            this.appName = appName;
            return this;
        }

        /**
         * Specify the panelistTrackingOnly you want to set(not required, default value is false).
         *
         * @param panelistTrackingOnly Set this to true if you wish to track SIFO panelists only.
         * @return the current builder object.
         */
        public Builder setPanelistTrackingOnly(boolean panelistTrackingOnly) {
            this.panelistTrackingOnly = panelistTrackingOnly;
            return this;
        }

        /**
         * Enable or disable sending data with HTTPS.
         *
         * @param https Set this to true if you wish to sent data with HTTPS.
         *              Default is true.
         * @return the current builder object.
         */
        public Builder useHttps(boolean https) {
            this.useHttpsActivated = https;
            return this;
        }

        /**
         * Enable logging.
         *
         * @param logPrintsActivated Set this to true to enable logging.
         *
         * @return the current builder object.
         */
        public Builder setLogPrintsActivated(boolean logPrintsActivated) {
            this.logPrintsActivated = logPrintsActivated;
            return this;
        }


        /**
         * @return return constructor of TSMobileAnalytics {@link #TSMobileAnalytics(Builder)}.
         */
        public TSMobileAnalytics build() {
            return new TSMobileAnalytics(this);
        }
    }
}