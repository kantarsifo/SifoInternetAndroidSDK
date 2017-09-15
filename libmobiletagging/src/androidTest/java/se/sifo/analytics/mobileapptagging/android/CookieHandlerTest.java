package se.sifo.analytics.mobileapptagging.android;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.webkit.CookieManager;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import static se.sifo.analytics.mobileapptagging.android.TagStringsAndValues.DOMAIN_CODIGO;

/**
 * Created by Peter on 2015-07-08.
 */
public class CookieHandlerTest extends AndroidTestCase {

    List<HttpCookie> mCookieList;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mCookieList = new ArrayList<>();

        HttpCookie cookie1 = new HttpCookie("Key1", "Value1");
        cookie1.setDomain(DOMAIN_CODIGO);

        HttpCookie cookie2 = new HttpCookie("Key2", "Value2");
        cookie2.setDomain(DOMAIN_CODIGO);

        mCookieList.add(cookie1);
        mCookieList.add(cookie2);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @SmallTest
    public void testSetupPanelistCookies_doesNotDeleteThirdPartyCookies() {

        final String thirdPartyDomain = "third.party.site";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(thirdPartyDomain, cookieValue);

        CookieHandler.setupPanelistCookies(getContext(), mCookieList, DOMAIN_CODIGO);

        assertEquals(cookieValue,
                cookieManager.getCookie(thirdPartyDomain));
    }

    @SmallTest
    public void testSetupPanelistCookies_deletesOldFrameworkCookies() {

        final CookieManager cookieManager = CookieManager.getInstance();

        for(HttpCookie cookie : mCookieList) {
            cookieManager.setCookie(cookie.getDomain(), CookieHandler.getCookieDetailString(cookie));
        }

        cookieManager.setCookie(DOMAIN_CODIGO, "OldKey1=OldValue1");

        CookieHandler.setupPanelistCookies(getContext(), mCookieList, DOMAIN_CODIGO);

        assertEquals("Key1=Value1; Key2=Value2", cookieManager.getCookie(DOMAIN_CODIGO));
    }

    @SmallTest
    public void testClearCookiesFor() {
        final String domain = "bh.mobiletech.se";
        final CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setCookie(domain, "MyKey1=MyValue1");

        CookieHandler.clearCookiesFor(domain);

        assertNull(cookieManager.getCookie(domain));
    }

    @SmallTest
    public void testRemoveCookieByExpiredDate() {
        CookieManager cookieManager = CookieManager.getInstance();
        String url = "bh.mobiletech.no";
        String value = "cookieName=my cookie";

        cookieManager.setCookie(url, value);
        final String cookieString = "cookieName=;expires=Mon, 17 Oct 2011 10:47:11 UTC;";
        cookieManager.setCookie(url, cookieString);

        assertNull(cookieManager.getCookie(url));
    }
}
