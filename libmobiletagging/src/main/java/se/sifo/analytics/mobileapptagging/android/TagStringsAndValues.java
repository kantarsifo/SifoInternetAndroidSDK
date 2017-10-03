/*************************************************
 * TNS SIFO Mobile Application Tagging Framework *
 * (c) Copyright 2012 TNS SIFO, Sweden,          *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

/**
 * Holds some pre-defined Strings used by the framework.
 *
 * @author Jakob Schyberg (jakob.schyberg@wecode.se)
 */
public final class TagStringsAndValues {
    /**
     * The maximum length of the Customer identifier value (CPID).
     */
    public static final int MAX_LENGTH_CPID = 4;

    /**
     * The length of Customer identifier value where measurement provider is Codigo Analytics
     */
    public static final int CPID_LENGTH_CODIGO = 32;

    /**
     * The length of Customer identifier value where measurement provider is Mobitech Analytics
     */
    public static int CPID_LENGTH_MOBITECH = 6;

    /**
     * The maximum allowed length of the Category String (cat).
     */
    public static final int MAX_LENGTH_CATEGORY = 255;

    /**
     * The maximum allowed length of the Extra String (ref).
     */
    public static final int MAX_LENGTH_EXTRA = 100;

    /**
     * The maximum allowed length of the Application Name String (ref).
     */
    public static final int MAX_LENGTH_APP_NAME = 243;

    /**
     * The maximum allowed length of the Content ID String (ref).
     */
    public static final int MAX_LENGTH_CONTENT_ID = 255;

    /**
     * The maximum allowed length of the Content Name String (ref).
     */
    public static final int MAX_LENGTH_CONTENT_NAME = 255;

    /**
     * The URL-base used to connect to the Mobiletech server.
     */
    public static final String MOBILETECH_URL_BASE = "http://bh.mobiletech.no/sifo/img?";

    /**
     * The URL-base used to connect to the Codigo server.
     */
    public static final String CODIGO_URL_BASE = "http://trafficgateway.research-int.se/TrafficCollector?";

    /**
     * The URL-base used to connect to the Mobiletech server.
     */
    public static final String MOBILETECH_URL_BASE_HTTPS = "https://bh.mobiletech.no/sifo/img?";

    /**
     * The URL-base used to connect to the Codigo server.
     */
    public static final String CODIGO_URL_BASE_HTTPS = "https://trafficgateway.research-int.se/TrafficCollector?";

    /**
     * The URL-base used to connect to the Mobiletech-server.
     *
     * @deprecated The framework now uses multiple providers. For the default provider, see {@link #MOBILETECH_URL_BASE}.
     */
    @Deprecated
    public static final String URL_BASE = MOBILETECH_URL_BASE;


    /**
     * The value to be used for the EUIDQ-parameter in tag requests.
     */
    public static final String EUIDQ = "ext-id-returning";

    /**
     * The value to be used for the EUIDQ-parameter in tag requests.
     */
    public static final String TYPE = "application";

    /**
     * The prefix added to the application name before added to the type-parameter in
     * tag requests.
     */
    public static final String APP_NAME_PREFIX = "APP_ANDROID_";

    /**
     * Result codes used in the sendTag functions to identify bad input values.
     * The value for Success.
     */
    public static final int RESULT_SUCCESS = 0;

    /**
     * Category was NULL.
     */
    public static final int ERROR_CATEGORY_NULL = 1;

    /**
     * Category String was too long.
     */
    public static final int ERROR_CATEGORY_TOO_LONG = 2;

    /**
     * Extra (ref) was NULL.
     */
    public static final int ERROR_EXTRA_NULL = 3;

    /**
     * Extra (ref) was too long.
     */
    public static final int ERROR_EXTRA_TOO_LONG = 4;

    /**
     * Content ID was NULL.
     */
    public static final int ERROR_CONTENT_ID_NULL = 5;

    /**
     * Content ID was too long.
     */
    public static final int ERROR_CONTENT_ID_TOO_LONG = 6;

    /**
     * Content name was too long.
     */
    public static final int ERROR_CONTENT_NAME_TOO_LONG = 7;

    public static final int ERROR_FRAMEWORK_INSTANCE_IS_NULL = 8;

    /**
     * The URL-encoding to be used
     */
    public static final String URL_ENCODING = "UTF-8";

    /**
     * If no sifo id is detected
     */
    public static final String NO_PANELIST_ID = "";

    /**
     * Mobiletech base domain
     */
    public static final String DOMAIN_MOBILETECH = "bh.mobiletech.no";

    /**
     * Codigo base domain
     */
    public static final String DOMAIN_CODIGO = ".research-int.se";

    /**
     * Mobiletech base domain
     *
     * @deprecated The framework now supports multiple providers. For the default provider, see {@link #DOMAIN_MOBILETECH}.
     */
    @Deprecated
    public static final String COOKIE_URL_BASE = DOMAIN_MOBILETECH;

    /**
     * Packagename of the TNS SIFO-Panelen application
     *
     * @deprecated The framework uses this package as a fallback, but will primarily focus on
     * {@link #SIFO_PANELIST_PACKAGE_NAME_V2}
     */
    @Deprecated
    public static final String SIFO_PANELIST_PACKAGE_NAME = "se.poll.android";

    /**
     * Packagename of the TNS SIFO-Panelen application v2.
     */
    public static final String SIFO_PANELIST_PACKAGE_NAME_V2 = "se.tns_sifo.ipm";

    /**
     * Name of the file to save Panelist user credentials in
     */
    public static final String SIFO_PANELIST_CREDENTIALS_FILENAME = "sifo_cookie_key";

    /**
     * Name of the file to save Panelist user credentials in, for version 2 of the framework.
     */
    public static final String SIFO_PANELIST_CREDENTIALS_FILENAME_V2 = "sifo_cookie_key_json";

    /**
     * Start of Sifo Measure Cookie String
     *
     * @deprecated Cookies are now created from JSON in the configuration file rather than constant strings.
     */
    @Deprecated
    public static final String SIFO_MEASSURE_COOKIE_START = "SIFO_MEASURE=\"";

    /**
     * Start of Panelist Measure Cookie String
     *
     * @deprecated Cookies are now created from JSON in the configuration file rather than constant strings.
     */
    @Deprecated
    public static final String PANELIST_COOKIE_START = "SIFO_PANEL=\"";

    /**
     * End of Cookie String
     *
     * @deprecated Cookies are now created from JSON in the configuration file rather than constant strings.
     */
    @Deprecated
    public static final String COOKIE_HEADER_END = "\"; Version=1; Domain=bh.mobiletech.no; Max-Age=315360000; Expires=Fri, 30-Apr-2022 12:33:21 GMT; Path=/";


    // End public members


    /**
     * Key for Sifo Measure Cookie.
     */
    static final String SIFO_MEASURE_COOKIE = "SIFO_MEASURE";

    /**
     * Key for Panelist Measure Cookie.
     */
    static final String SIFO_PANELIST_COOKIE = "SIFO_PANEL";
}
