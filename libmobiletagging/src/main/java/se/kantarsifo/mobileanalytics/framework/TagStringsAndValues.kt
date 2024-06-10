/*************************************************
 * Kantar Sifo Mobile Analytics Framework                  *
 * Commercial SDK (c) 2016-2024 Kantar Media Sweden AB,    *
 * All rights reserved.                                    *
 */
package se.kantarsifo.mobileanalytics.framework

/**
 * Holds some pre-defined Strings used by the framework.
 *
 */
object TagStringsAndValues {

    /**
     * The length of Customer identifier value where measurement provider is Codigo Analytics
     */
    const val CPID_LENGTH_CODIGO = 32

    /**
     * The maximum allowed length of the Category String (cat).
     */
    const val MAX_LENGTH_CATEGORY = 255

    /**
     * The maximum allowed length of the Application Name String (ref).
     * This value is because [APP_NAME_PREFIX] will be concatenated to the app name later.
     */
    const val MAX_LENGTH_APP_NAME = 243

    /**
     * The maximum allowed length of the Content ID String (ref).
     */
    const val MAX_LENGTH_CONTENT_ID = 255

    /**
     * The URL-base used to connect to the Codigo server.
     */
    const val CODIGO_URL_BASE_HTTPS = "https://trafficgateway.research-int.se/TrafficCollector?"

    /**
     * The URL-base used to connect to fetch the api config.
     */
    const val BASE_CONFIG_URL = "https://sifopanelen.research-int.se"

    /**
     * The value to be used for the EUIDQ-parameter in tag requests.
     */
    const val EUIDQ = "ext-id-returning"

    /**
     * The value to be used for the EUIDQ-parameter in tag requests.
     */
    const val TYPE = "application"

    /**
     * The prefix added to the application name before added to the type-parameter in
     * tag requests.
     */
    const val APP_NAME_PREFIX = "APP_ANDROID_"

    /**
     * Result codes used in the sendTag functions to identify bad input values.
     * The value for Success.
     */
    const val RESULT_SUCCESS = 0

    /**
     * Category was NULL.
     */
    const val ERROR_CATEGORY_NULL = 1

    /**
     * Category String was too long.
     */
    const val ERROR_CATEGORY_TOO_LONG = 2

    /**
     * Extra (ref) was NULL.
     */
    const val ERROR_EXTRA_NULL = 3

    /**
     * Extra (ref) was too long.
     */
    const val ERROR_EXTRA_TOO_LONG = 4

    /**
     * Content ID was NULL.
     */
    const val ERROR_CONTENT_ID_NULL = 5

    /**
     * Content ID was too long.
     */
    const val ERROR_CONTENT_ID_TOO_LONG = 6

    /**
     * Framework instance is null.
     */
    const val ERROR_FRAMEWORK_INSTANCE_IS_NULL = 8

    /**
     * The URL-encoding to be used
     */
    const val URL_ENCODING = "UTF-8"

    /**
     * If no sifo id is detected
     */
    const val NO_PANELIST_ID = ""

    /**
     * Codigo base domain
     */
    const val DOMAIN_CODIGO = ".research-int.se"

    /**
     * Package name of the TNS SIFO-Panelen application
     *
     */
    @Deprecated("The framework uses this package as a fallback, " +
            "but will primarily focus on [SIFO_PANELIST_PACKAGE_NAME_V2]")
    const val SIFO_PANELIST_PACKAGE_NAME = "se.poll.android"

    /**
     * Package name of the TNS SIFO-Panelen application v2.
     */
    const val SIFO_PANELIST_PACKAGE_NAME_V2 = "se.tns_sifo.ipm"

    /**
     * Name of the file to save Panelist user credentials in
     */
    const val SIFO_PANELIST_CREDENTIALS_FILENAME = "sifo_cookie_key"

    /**
     * Name of the file to save Panelist user credentials in, for version 2 of the framework.
     */
    const val SIFO_PANELIST_CREDENTIALS_FILENAME_V2 = "sifo_cookie_key_json"

    /**
     * Key for Sifo Measure Cookie.
     */
    internal const val SIFO_MEASURE_COOKIE = "SIFO_MEASURE"

    /**
     * Key for Panelist Measure Cookie.
     */
    internal const val SIFO_PANELIST_COOKIE = "SIFO_PANEL"

    /**
     * Key preferences
     */
    internal const val SIFO_PREFERENCE_KEY = "SIFO_PREFERENCE_KEY"

    /**
     * Key for cookie information stored in preferences
     */
    internal const val SIFO_PREFERENCE_COOKIES = "SIFO_PREFERENCE_COOKIES"

    /**
     * Key for last cookie sync time
     */
    internal const val SIFO_PREFERENCE_COOKIES_SYNC_TIME = "SIFO_PREFERENCE_COOKIES_SYNC_TIME"

    /**
     * Key for config data
     */
    internal const val SIFO_PREFERENCE_CONFIG = "SIFO_PREFERENCE_CONFIG"

    /**
     *  Default config for when config server is down
     */
    internal const val SIFO_DEFAULT_CONFIG = "https://trafficgateway.research-int.se/TrafficCollector?siteId={siteIdValue}&appClientId={appClientIdValue}&cp={cpValue}&appId={appIDValue}&appName={appNameValue}&appRef={appRefValue}"

    /**
     * Meta cookie name
     */
    internal const val SIFO_META_COOKIE_NAME = "sifo_config"

    /**
     * app start event category name
     */
    internal const val SIFO_APP_START_EVENT_CATEGORY = "appstart"

}