/*************************************************
 * TNS SIFO Mobile Application Tagging Framework *
 * (c) Copyright 2012 TNS SIFO, Sweden,          *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;


import java.io.UnsupportedEncodingException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * This class creates request-tags using the values cpid, type, cat, ref, id, euid and euidq.
 * It's main purpose is to combine these values to create a valid tag request-URL with correct encoding.
 * The values CPID and Type are provided on creation, while the values Cat, Ref and ID is provided each
 * time a URL is created. The EUID is created by a device identifier retrieved directly by this class using
 * an application Context object.
 *
 * @author Jakob Schyberg (jakob.schyberg@wecode.se)
 */
class TagHandler {

    private String urlBase;
    private String cpId;
    private String type;
    private String euid;
    private String euidq;
    private String ref;
    private String applicationName;
    private String applicationVersion;
    private int androidSDK;

    private CookieStore cookies;


    /**
     * Create a new TagHandler with specified CPID and type, for the specified application context.
     *
     * @param c           The context of this application.
     * @param cpId        The CPID to be used in all tags.
     * @param ref
     * @param panelistKey
     */
    public TagHandler(Context c, String cpId, String ref, String panelistKey) {
        super();

        generateEuid(c);
        this.cpId = cpId;
        this.type = TagStringsAndValues.TYPE;
        this.ref = TagStringsAndValues.APP_NAME_PREFIX + ref;

        if (cpId.length() == TagStringsAndValues.CPID_LENGTH_CODIGO) {
            if(MobileTaggingFrameworkBackend.isSendWithHttpsActivated()){
                urlBase = TagStringsAndValues.CODIGO_URL_BASE_HTTPS;
            }else{
                urlBase = TagStringsAndValues.CODIGO_URL_BASE;
            }
        } else {
            if(MobileTaggingFrameworkBackend.isSendWithHttpsActivated()){
                urlBase = TagStringsAndValues.MOBILETECH_URL_BASE_HTTPS;
            }else{
                urlBase = TagStringsAndValues.MOBILETECH_URL_BASE;
            }
        }

        this.euidq = TagStringsAndValues.EUIDQ;
        this.applicationName = ref;

        List<HttpCookie> cookies = CookieHandler.createLegacyCookies(panelistKey);
        initCookies(c, cookies);

        //Get application version and android SDK
        try {
            this.applicationVersion = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            this.androidSDK = Build.VERSION.SDK_INT;
        } catch (Exception e) {
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to retrieve application version, will not set be set in request header");
        }
    }

    /**
     * Create a new TagHandler with specified CPID and type, for the specified application context.
     *
     * @param c       The context of this application.
     * @param cpId    The CPID to be used in all tags.
     * @param ref
     * @param cookies The list of cookies to send with measurement requests
     */
    public TagHandler(Context c, String cpId, String ref, List<HttpCookie> cookies) {
        super();

        generateEuid(c);
        this.cpId = cpId;
        this.type = TagStringsAndValues.TYPE;
        this.ref = TagStringsAndValues.APP_NAME_PREFIX + ref;

        if (cpId.length() == TagStringsAndValues.CPID_LENGTH_CODIGO) {
            if(MobileTaggingFrameworkBackend.isSendWithHttpsActivated()){
                urlBase = TagStringsAndValues.CODIGO_URL_BASE_HTTPS;
            }else{
                urlBase = TagStringsAndValues.CODIGO_URL_BASE;
            }
        } else {
            if(MobileTaggingFrameworkBackend.isSendWithHttpsActivated()){
                urlBase = TagStringsAndValues.MOBILETECH_URL_BASE_HTTPS;
            }else{
                urlBase = TagStringsAndValues.MOBILETECH_URL_BASE;
            }
        }

        euidq = TagStringsAndValues.EUIDQ;
        this.applicationName = ref;

        initCookies(c, cookies);

        //Get application version and android SDK
        try {
            this.applicationVersion = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            this.androidSDK = Build.VERSION.SDK_INT;
        } catch (Exception e) {
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to retrieve application version, will not set be set in request header");
        }
    }

    public void refresh(Context context, String panelistKey) {
        List<HttpCookie> cookies = CookieHandler.createLegacyCookies(panelistKey);
        initCookies(context, cookies);
    }

    public void refresh(Context context, List<HttpCookie> cookies) {
        initCookies(context, cookies);
    }

    /**
     * Workaround for NameNotFoundException when app tries to access
     * webview while it is reinstalling Android System Webview for Android Lollipop
     * @See <a https://code.google.com/p/android/issues/detail?id=175124">android Issue tracking</a>
     * @See <a https://code.google.com/p/chromium/issues/detail?id=506369">chromium Issue tracking</a>
     */
    private void initCookies(final Context context, final List<HttpCookie> cookieList) {
        final SetupPanelListCookies setupPanelListCookies = new SetupPanelListCookies(context, cookieList);
        setupPanelListCookies.run();
    }


    private class SetupPanelListCookies implements Runnable {

        private static final String SYSTEM_WEBVIEW_PACKAGE_NAME = "com.google.android.webview";
        private static final int MAX_WEBVIEW_PACKAGE_RETRY = 6;
        private static final int TWO_SEC = 2000;

        private final Handler handler = new Handler();
        private final Context context;
        private final List<HttpCookie> cookieList;

        private int retryCounter;

        public SetupPanelListCookies(Context context, List<HttpCookie> cookieList) {
            this.context = context;
            this.cookieList = cookieList;
            this.retryCounter = 0;
        }

        @Override
        public void run() {
            try {
                if (SDK_INT >= LOLLIPOP && retryCounter++ < MAX_WEBVIEW_PACKAGE_RETRY) {

                    context.getPackageManager().getPackageInfo(SYSTEM_WEBVIEW_PACKAGE_NAME,
                            PackageManager.GET_ACTIVITIES);
                }

                cookies = CookieHandler.setupPanelistCookies(context, cookieList, urlBase);

            } catch (PackageManager.NameNotFoundException e) {
                MobileTaggingFrameworkBackend.errorToLog("Failed to setup panel list cookies - " + String.format("Retry counter=%s", retryCounter));
                handler.postDelayed(this, TWO_SEC);
            }
        }
    }

    /**
     * Get a tag-request URL for the specified values of cat and id.
     *
     * @param cat Value for parameter cat.
     * @param id  Value for parameter id.
     * @return The URL created with values cpid, type, cat, ref, id, euid and euidq.
     */
    public String getURL(String cat, String id, String name) {
        boolean result = true;
        String url;
        if (cat == null) {
            result = false;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to create URL - category may not be null");
        } else if (cat.length() > TagStringsAndValues.MAX_LENGTH_CATEGORY) {
            result = false;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to create URL - category may not be more than " + TagStringsAndValues.MAX_LENGTH_CATEGORY + " characters");
        } else if (id == null) {
            result = false;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to create URL - contentID may not be null");
        } else if (id.length() > TagStringsAndValues.MAX_LENGTH_CONTENT_ID) {
            result = false;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to create URL - contentID may not be more than " + TagStringsAndValues.MAX_LENGTH_CONTENT_ID + " characters");
        } else if (name != null && name.length() > TagStringsAndValues.MAX_LENGTH_CONTENT_NAME) {
            result = false;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to create URL - contentName may not be more than " + TagStringsAndValues.MAX_LENGTH_CONTENT_NAME + " characters");
        }

        if (result) {
            String ref = urlEncode(this.ref.trim());
            cat = urlEncode(cat.trim());
            id = urlEncode(id.trim());
            Log.v("appIddd", "appId :" + id);

            if (TagStringsAndValues.CODIGO_URL_BASE.equals(urlBase)) {
                url = String.format("%ssiteId=%s&appClientId=%s&cp=%s&appId=%s&appName=%s&appRef=%s",
                        urlBase, cpId, euid, cat, id, type, ref);
            } else {
                String nameTag = "";
                if (!TextUtils.isEmpty(name)) {
                    name = urlEncode(name.trim());
                    nameTag = "&name=" + urlEncode(name.trim());
                }

//                url = urlBase + "cpid=" + cpId + "&cat=" + cat + "&ref=" + ref + "&id=" + id + nameTag + "&type=" + type +
//                        "&euid=" + euid + "&euidq=" + euidq;
                url = String.format("%scpid=%s&cat=%s&ref=%s&id=%s%s&type=%s&euid=%s&euidq=%s",
                        urlBase, cpId, cat, ref, id, nameTag, type, euid, euidq);
            }
        } else {
            url = null;
        }
        return url;
    }

    /**
     * Get the CPID set for this handler.
     *
     * @return The CPID for this handler.
     */
    public String getCpid() {
        return cpId;
    }

    /**
     * Set the CPID for this handler.
     *
     * @param cpid The CPID to be used by this handler.
     */
    public void setCpid(String cpid) {
        this.cpId = cpid;
    }

    /**
     * Get the type-value used by this handler.
     *
     * @return The type-value used by this hanlder.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type-value to be used by this handler.
     *
     * @param type The type-value to be used by this hanlder.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the Unique identifier value for this device used as euid by this handler.
     *
     * @return The unique identifier for this device.
     */
    public String getEuid() {
        return euid;
    }

    /**
     * Generate the Unique identifier value for this device used as euid by this handler.
     *
     * @param c The context of this application.
     */
    public void generateEuid(Context c) {
        try {
            euid = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
        } catch (NullPointerException e) {
            euid = "";
        }
    }

    /**
     * Get the euidq currently in use.
     *
     * @return The euidq currently in use.
     */
    public String getEuidq() {
        return euidq;
    }

    /**
     * Change the euidq value. (Not recommended)
     *
     * @param euidq The euidq value to use.
     */
    public void setEuidq(String euidq) {
        this.euidq = euidq;
    }

    /**
     * Get the name of the application
     *
     * @return The name of the application (from the manifest)
     */
    public String getApplicationName() {
        return applicationName;
    }

    public CookieStore getCookies() {
        return cookies;
    }

    /**
     * Get the Application version of the Application using the framework
     *
     * @return The application version set in the manifest
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * Get the Android SDK of the Device using the framework
     *
     * @return The android sdk set in the manifest
     */
    public int getAndroidSDK(){
        return androidSDK;
    }

    public String getPanelistKey() {
        for (HttpCookie c : cookies.getCookies()) {
            if (TagStringsAndValues.SIFO_PANELIST_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return TagStringsAndValues.NO_PANELIST_ID;
    }

    /**
     * Make UTF-8 encoding on a specified String.
     *
     * @param s The String to encode.
     * @return The encoded String.
     */
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, TagStringsAndValues.URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // Since encoding UTF-8 is supported by android this should not happen
            MobileTaggingFrameworkBackend.printToLog("URL-Encoding not supported");
            return s;
        }
    }

}