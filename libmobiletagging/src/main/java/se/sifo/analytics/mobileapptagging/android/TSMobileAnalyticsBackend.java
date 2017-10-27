/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;


/**
 * Extension of the framework class containing some extended functions used
 * internally by the framework.
 *
 * @author Jakob Schyberg (jakob.schyberg@wecode.se)
 */
class TSMobileAnalyticsBackend extends TSMobileAnalytics {
    /**
     * Create an instance of the framework.
     */
    public static TSMobileAnalyticsBackend createInstance(final Context context, final String cpID, final String applicationName, final boolean onlyPanelist) {
        if (context == null) {
            fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - context must not be null");
            return frameworkInstance;
        }

        if (frameworkInstance == null) {
            if (cpID == null) {
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - CPID must not be null");
            } else if (cpID.length() == 0) {
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - CPID must not be empty");
            } else if(!(cpID.length() == TagStringsAndValues.CPID_LENGTH_CODIGO || cpID.length() == TagStringsAndValues.CPID_LENGTH_MOBITECH)){
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - CPID must be 4 or 32 characters");
            } else if (applicationName == null) {
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - Application Name must not be null");
            } else if (applicationName.length() == 0) {
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - Application Name must not be empty");
            } else if (applicationName.length() > TagStringsAndValues.MAX_LENGTH_APP_NAME) {
                fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - Application Name must not be more than " + TagStringsAndValues.MAX_LENGTH_APP_NAME + " characters");
            } else {

                Runnable runnable = new Runnable() {
                    public void run() {
                        boolean requestHandled = initTags(context, cpID, applicationName, onlyPanelist);
                        if (!requestHandled) {
                            initLegacyTags(context, cpID, applicationName, onlyPanelist);
                        }                    }
                };
                Thread initTagsThreat = new Thread(runnable);
                initTagsThreat.start();

            }
        } else {
            printToLog("Mobile Application Tagging Framework already initiated");
            printToLog("Refreshing panelist keys");
            Runnable runnable = new Runnable() {
                public void run() {
                    List<HttpCookie> cookies = PanelistHandler.getCookies(context);
                    if (cookies != null) {
                        frameworkInstance.dataRequestHandler.refreshCookies(context, cookies);
                    } else {
                        frameworkInstance.dataRequestHandler.refreshCookies(context, PanelistHandler.getPanelistKey(context));
                    }                 }
            };
            Thread refreshCookiesThread = new Thread(runnable);
            refreshCookiesThread.start();

        }

        Runnable runnable = new Runnable() {
            public void run() {
                VolleyManager.getInstance().getRequestQueue(context); // TODO: 19/09/2017 test it, it can be wrong and crash
            }
        };
        Thread volleyThread = new Thread(runnable);
        volleyThread.start();

        return frameworkInstance;
    }

    private static boolean initTags(Context context, String cpID, String applicationName, boolean onlyPanelist) {

        final List<HttpCookie> cookies = PanelistHandler.getCookies(context);
        if (cookies == null)
            return false;

        if (onlyPanelist && cookies.isEmpty()) {
            fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - Cookies file was empty, panelist id not found");
        } else {
            frameworkInstance = new TSMobileAnalyticsBackend(context, cpID, applicationName, cookies);
            printToLog("Mobile Application Tagging Framework initiated with the following values \nCPID: " + cpID + "\nApplication name: " + applicationName + "\nOnly panelist tracking : " + onlyPanelist);
        }
        return true;
    }

    private static void initLegacyTags(Context context, String cpID, String applicationName, boolean onlyPanelist) {
        final String panelistKey = PanelistHandler.getPanelistKey(context);
        if (cpID.length() > TagStringsAndValues.MAX_LENGTH_CPID && cpID.length() != TagStringsAndValues.CPID_LENGTH_CODIGO) {
            fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - CPID must either be exactly " + TagStringsAndValues.CPID_LENGTH_CODIGO +
                    " or no more than " + TagStringsAndValues.MAX_LENGTH_CPID + " characters");
        } else if (onlyPanelist && panelistKey.equals(TagStringsAndValues.NO_PANELIST_ID)) {
            fatalErrorToLog("Mobile Application Tagging Framework Failed to initiate - Panelist Id was not found, it must exist if only panelist tracking is active");
        } else {
            frameworkInstance = new TSMobileAnalyticsBackend(context, cpID, applicationName, panelistKey);
            //TODO print panelist setting
            printToLog("Mobile Application Tagging Framework initiated with the following values \nCPID: " + cpID + "\nApplication name: " + applicationName + "\nOnly panelist tracking : " + onlyPanelist);
        }
    }

    /**
     * Get an instance of the framework.
     *
     * @return The framework instance.
     */

    public static TSMobileAnalyticsBackend getInstance() {
        return frameworkInstance;
    }

    /**
     * Used by the framework to see if to print logs or not.
     *
     * @return True if log prints are activated, false otherwise.
     */
    public static boolean isLogPrintsActivated() {
        return logPrintsActivated;
    }

    /**
     * Used by the framework to send data with https.
     *
     * @return True if useHttps is activated, false otherwise.
     */
    public static boolean useHttpsActivated() {
        return useHttpsActivated;
    }

    /**
     * Constructor used internally only.
     * Use createInstance() and getInstance() instead.
     */
    public TSMobileAnalyticsBackend(Context c, String cpId, String applicationName, String panelistId) {
        super();
        dataRequestHandler = new TagDataRequestHandler(c, cpId, applicationName, panelistId);
    }


    /**
     * Constructor used internally only.
     * Use createInstance() and getInstance() instead.
     */
    public TSMobileAnalyticsBackend(Context c, String cpId, String applicationName, List<HttpCookie> cookies) {
        super();
        dataRequestHandler = new TagDataRequestHandler(c, cpId, applicationName, cookies);
    }

    /**
     * Print text to LogCat following a specific pattern and the tag "MobileAppTagging"
     *
     * @param message The string to print.
     */
    public static void printToLog(String message) {
        if (isLogPrintsActivated()) {
            Log.i("MobileAppTagging", message);
            Log.i("MobileAppTagging", "***********************************");
        }
    }

    /**
     * Print an error message to LogCat following a specific pattern and the tag "MobileAppTagging"
     *
     * @param message The error message.
     */
    public static void errorToLog(String message) {
        if (isLogPrintsActivated()) {
            Log.e("MobileAppTagging", "***********************************");
            Log.e("MobileAppTagging", message);
            Log.e("MobileAppTagging", "***********************************");
        }
    }

    /**
     * Print an error message to LogCat following a specific pattern and the tag "MobileAppTagging"
     *
     * @param message The error message.
     */
    public static void fatalErrorToLog(String message) {
        Log.e("MobileAppTagging", "***********************************");
        Log.e("MobileAppTagging", message);
        Log.e("MobileAppTagging", "***********************************");
    }


    /**
     * This class purpose is to look for a shared File containing a CookieKey String inside the TNS-Sifo Panelen application.
     * It will try to open a FileInputStream from the TNS-Sifo Panelen application.
     * If the package is not found a or the CookieKey has no values an empty String will be returned and no Panelist users will be measured.
     * If a CookieKey is found it will be returned and used as cookie header for all HTTP requests.
     * The CookieKey will also be stored in the applications local CookieManager to be used in WebView requests.
     *
     * @author Niklas Björkholm (niklas.bjorkholm@idealapps.se)
     */
    abstract static class PanelistHandler {

        /**
         * Get a Panelist CookieKey String
         *
         * @param c The context of the application
         * @return The CookieKey found or a empty value if no key is found
         */
        public static String getPanelistKey(Context c) {
            String userKey = TagStringsAndValues.NO_PANELIST_ID;
            FileInputStream fi = getSifoInputStream(c,
                    TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME,
                    TagStringsAndValues.SIFO_PANELIST_CREDENTIALS_FILENAME);
            if (fi != null) {
                userKey = readCookieKeyString(fi);
            }
            return userKey;
        }

        public static List<HttpCookie> getCookies(Context c) {
            FileInputStream fi = getSifoInputStream(c,
                    TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME_V2,
                    TagStringsAndValues.SIFO_PANELIST_CREDENTIALS_FILENAME_V2);
            if (fi != null) {
                return readCookieStore(fi);
            }
            return null;
        }

        /**
         * Opens a FileInputStream from TNS Sifo-Panelen
         * reads the inputstream from the TNS-Sifo Panelen application
         *
         * @param context The context of the application
         * @return The FileInputStream from TNS Sifo-Panelen or null if not found
         */
        private static FileInputStream getSifoInputStream(Context context, String packageName, String filename) {
            FileInputStream fi = null;
            try {
                fi = context.createPackageContext(packageName, 0).openFileInput(filename);
            } catch (FileNotFoundException e) {
            } catch (NameNotFoundException e) {
            }
            return fi;
        }

        private static List<HttpCookie> readCookieStore(FileInputStream stream) {
            String content = readFile(stream, "Error reading TNS Panelist cookies");
            List<HttpCookie> cookieList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject entry = jsonArray.getJSONObject(i);
                    cookieList.add(CookieHandler.getCookieFromJson(entry));
                }

            } catch (JSONException e) {
                TSMobileAnalyticsBackend.printToLog("Error parsing TNS Panelist JSON data");
            }
            return cookieList;
        }

        /**
         * Get the CookieKey from the TNS Sifo-Panelen
         *
         * @param stream The stream to use
         * @return The CookieKey read from the file or an empty String if not found
         */
        private static String readCookieKeyString(FileInputStream stream) {
            String content = readFile(stream, "Error reading TNS Panelist CookieKey");
            return content.trim();
        }

        private static String readFile(FileInputStream stream, String errorString) {
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder buffer = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            } catch (Exception e) {
                TSMobileAnalyticsBackend.printToLog(errorString);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        //Should never happen
                        TSMobileAnalyticsBackend.printToLog("Error Closing InputStream");
                    }
                }
            }
            return "";
        }
    }
}
