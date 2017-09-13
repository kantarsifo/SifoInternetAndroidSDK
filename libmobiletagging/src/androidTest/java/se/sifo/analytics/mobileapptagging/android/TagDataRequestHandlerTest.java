package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.webkit.CookieManager;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.ArrayList;
import java.util.List;

import static se.sifo.analytics.mobileapptagging.android.TagStringsAndValues.DOMAIN_CODIGO;
import static se.sifo.analytics.mobileapptagging.android.Constants.CODIGO_CPID;
import static se.sifo.analytics.mobileapptagging.android.Constants.APP_NAME;

public class TagDataRequestHandlerTest extends AndroidTestCase {

    private Context mAppCtx;
    private List<Cookie> mCookies;
    private TagDataRequestHandler mTagDataRequestHandler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAppCtx = getContext().getApplicationContext();

        mCookies = new ArrayList<>();

        BasicClientCookie cookie1 = new BasicClientCookie("Key1", "Value1");
        cookie1.setDomain(DOMAIN_CODIGO);

        BasicClientCookie cookie2 = new BasicClientCookie("Key2", "Value2");
        cookie2.setDomain(DOMAIN_CODIGO);

        mCookies.add(cookie1);
        mCookies.add(cookie2);

        mTagDataRequestHandler = new TagDataRequestHandler(mAppCtx,
                CODIGO_CPID,
                APP_NAME,
                mCookies);
    }

    @SmallTest
    public void testRefreshCookies_doesNotDeleteThirdPartyCookies() {
        final String thirdPartyDomain = "third.party.site";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(thirdPartyDomain, cookieValue);

        mTagDataRequestHandler.refreshCookies(mAppCtx, mCookies);

        assertEquals(cookieValue,
                cookieManager.getCookie(thirdPartyDomain));
    }

    @SmallTest
    public void testRefreshCookies_deletesOldFrameworkCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(DOMAIN_CODIGO, "OldKey1=OldValue1");

        mTagDataRequestHandler.refreshCookies(mAppCtx, mCookies);

        assertEquals("Key1=Value1; Key2=Value2",
                cookieManager.getCookie(DOMAIN_CODIGO));
    }
}
