package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
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
    static CookieStore setupPanelistCookies(Context context, List<HttpCookie> cookies, String urlBase) {
        SifoCookieManager.getInstance().clearCookies();

        for (HttpCookie cookie : cookies) {
            try {
                SifoCookieManager.getInstance().getCookieStore().add(new URI(cookie.getDomain()), cookie);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            TSMobileAnalyticsBackend.printToLog("--Cookie added: " + cookie);
        }


        return SifoCookieManager.getInstance().getCookieStore();
    }

    static void clearCookiesFor(String domain) {

    }

    static HttpCookie getCookieFromJson(JSONObject json) throws JSONException {
        HttpCookie cookie = new HttpCookie(json.getString("key"), json.getString("value"));
        cookie.setDomain(json.getString("domain"));
        cookie.setPath(json.getString("path"));
        cookie.setVersion(1);
        cookie.setMaxAge(getExpiryDate(10).getTime());
        return cookie;
    }

    static String getCookieString(HttpCookie cookie) {
        return String.format("%s=%s",
                cookie.getName(), cookie.getValue());
    }

    static String getCookieDetailString(HttpCookie cookie) {
        String dateString;
        dateString = " Expires=" + sExpiryDateFormat.format(cookie.getMaxAge()) + ";";

        return String.format("%s=%s; Version=%d; Domain=%s; Max-Age=%d;%s Path=%s",
                cookie.getName(), cookie.getValue(), cookie.getVersion(), cookie.getDomain(),
                MAX_AGE, dateString, cookie.getPath());
    }

    static String getCookieString(List<HttpCookie> cookies) {
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

    static boolean cookieIsCurrentDomain(HttpCookie cookie, String urlBase) {
        return urlBase.contains(cookie.getDomain());
    }

    static List<HttpCookie> createLegacyCookies(String panelistKey) {
        List<HttpCookie> cookies = new ArrayList<>();

        HttpCookie[] cookiesArray = new HttpCookie[]{
                new HttpCookie(TagStringsAndValues.SIFO_MEASURE_COOKIE, panelistKey),
                new HttpCookie(TagStringsAndValues.SIFO_PANELIST_COOKIE, panelistKey)
        };

        Date expiryDate = getExpiryDate(10);
        for (HttpCookie c : cookiesArray) {
            c.setVersion(1);
            c.setPath("/");
            c.setDomain(TagStringsAndValues.DOMAIN_MOBILETECH);
            c.setMaxAge(expiryDate.getTime());
        }
        Collections.addAll(cookies, cookiesArray);
        return cookies;
    }


    static Date getExpiryDate(int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, value);
        return calendar.getTime();
    }
}
