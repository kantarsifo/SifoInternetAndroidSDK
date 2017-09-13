package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Peter on 2015-03-30.
 */
class CookieHandler {

    private static final long MAX_AGE = 315360000;

    private static final DateFormat sExpiryDateFormat;

    static {
        sExpiryDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss 'GMT'", Locale.US);
        sExpiryDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Stores Panelist Cookie Headers in CookieSyncManager to use in WebView requests
     */
    static CookieStore setupPanelistCookies(Context context, List<Cookie> cookies, String urlBase) {
        CookieSyncManager.createInstance(context);
        CookieManager manager = CookieManager.getInstance();
        clearCookiesFor(TagStringsAndValues.DOMAIN_MOBILETECH);
        clearCookiesFor(TagStringsAndValues.DOMAIN_CODIGO);
        CookieStore cookieStore = new BasicCookieStore();
        for (Cookie cookie : cookies) {
            String cookieValue = getCookieDetailString(cookie);
            manager.setCookie(cookie.getDomain(), cookieValue);
//            MobileTaggingFrameworkBackend.printToLog("--Cookie added: " + cookieValue);

            if (cookieIsCurrentDomain(cookie, urlBase)) {
                cookieStore.addCookie(cookie);
            }
        }
        manager.setAcceptCookie(true);

        CookieSyncManager.getInstance().sync();
        return cookieStore;
    }

    static void clearCookiesFor(String domain) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookiestring = cookieManager.getCookie(domain);
        if (!TextUtils.isEmpty(cookiestring)) {
            String[] cookiesArray = cookiestring.split(";");
            for (String cookieStr : cookiesArray) {
                final String[] cookieparts = cookieStr.split("=");
                cookieManager.setCookie(domain, cookieparts[0].trim() + "=;Expires=" +
                        sExpiryDateFormat.format(getExpiryDate(-10)) + ";");
            }
        }
    }

    static Cookie getCookieFromJson(JSONObject json) throws JSONException {
        BasicClientCookie cookie = new BasicClientCookie(json.getString("key"), json.getString("value"));
        cookie.setDomain(json.getString("domain"));
        cookie.setPath(json.getString("path"));
        cookie.setVersion(1);
        cookie.setExpiryDate(getExpiryDate(10));
        return cookie;
    }

    static String getCookieString(Cookie cookie) {
        return String.format("%s=%s",
                cookie.getName(), cookie.getValue());
    }

    static String getCookieDetailString(Cookie cookie) {
        String dateString;
        if (cookie.getExpiryDate() != null) {
            dateString = " Expires=" + sExpiryDateFormat.format(cookie.getExpiryDate()) + ";";
        } else {
            dateString = "";
        }
        return String.format("%s=%s; Version=%d; Domain=%s; Max-Age=%d;%s Path=%s",
                cookie.getName(), cookie.getValue(), cookie.getVersion(), cookie.getDomain(),
                MAX_AGE, dateString, cookie.getPath());
    }

    static String getCookieString(List<Cookie> cookies) {
        StringBuilder sb = new StringBuilder();
        int size = cookies.size();
        for (int i = 0; i < size; i++) {
            sb.append(getCookieString(cookies.get(i)));
            if (i < size - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    static boolean cookieIsCurrentDomain(Cookie cookie, String urlBase) {
        return urlBase.contains(cookie.getDomain());
    }

    static List<Cookie> createLegacyCookies(String panelistKey) {
        List<Cookie> cookies = new ArrayList<Cookie>();

        BasicClientCookie[] cookiesArray = new BasicClientCookie[]{
                new BasicClientCookie(TagStringsAndValues.SIFO_MEASURE_COOKIE, panelistKey),
                new BasicClientCookie(TagStringsAndValues.SIFO_PANELIST_COOKIE, panelistKey)
        };

        Date expiryDate = getExpiryDate(10);
        for (BasicClientCookie c : cookiesArray) {
            c.setVersion(1);
            c.setPath("/");
            c.setDomain(TagStringsAndValues.DOMAIN_MOBILETECH);
            c.setExpiryDate(expiryDate);
        }
        Collections.addAll(cookies, cookiesArray);
        return cookies;
    }

    static CookieStore getCookieStore(List<Cookie> cookies) {
        CookieStore store = new BasicCookieStore();
        for (Cookie cookie : cookies) {
            store.addCookie(cookie);
        }
        return store;
    }

    static Date getExpiryDate(int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, value);
        return calendar.getTime();
    }
}
