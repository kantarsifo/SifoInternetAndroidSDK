package se.sifo.analytics.mobileapptagging.android;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
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

        SifoCookieManager.getInstance().clearCookies();
    }

    @SmallTest
    public void testSetupPanelistCookies_doesNotDeleteThirdPartyCookies() {

        final String thirdPartyDomain = "http://thirdpartysite.com";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";
        HttpCookie cookie = new HttpCookie(thirdPartyDomain, cookieValue);

        SifoCookieManager cookieManager = SifoCookieManager.getInstance();

            cookieManager.getCookieStore().add(URI.create(thirdPartyDomain), cookie);


        CookieHandler.setupPanelistCookies(getContext(), mCookieList, DOMAIN_CODIGO);

            assertEquals(cookieValue,
                    cookieManager.getCookieStore().get(URI.create(thirdPartyDomain)).toString());

    }

    @SmallTest
    public void testSetupPanelistCookies_deletesOldFrameworkCookies() {

        final SifoCookieManager cookieManager = SifoCookieManager.getInstance();

        for(HttpCookie cookie : mCookieList) {
            cookieManager.getCookieStore().add(URI.create(cookie.getDomain()), cookie);
        }
        HttpCookie cookie = new HttpCookie("OldKey1", "OldValue1");

        cookieManager.getCookieStore().add(URI.create(DOMAIN_CODIGO), cookie);

        CookieHandler.setupPanelistCookies(getContext(), mCookieList, DOMAIN_CODIGO);

        assertEquals("Key1=Value1; Key2=Value2", cookieManager.getCookieStore().get(URI.create(DOMAIN_CODIGO)).toString());
    }

    @SmallTest
    public void testClearCookiesFor() {
        final String domain = "bh.mobiletech.se";
        final SifoCookieManager cookieManager = SifoCookieManager.getInstance();

        cookieManager.getCookieStore().add(URI.create(domain), new HttpCookie("MyKey1","MyValue1"));

        CookieHandler.clearCookiesFor(domain);

        assertNull(cookieManager.getCookieStore().get(URI.create(domain)).toString());
    }

    @SmallTest
    public void testRemoveCookieByExpiredDate() {
        SifoCookieManager cookieManager = SifoCookieManager.getInstance();
        String url = "bh.mobiletech.no";

        cookieManager.getCookieStore().add(URI.create(url), new HttpCookie("cookieName", "my cookie"));
        cookieManager.getCookieStore().add(URI.create(url), new HttpCookie("cookieName=;expires", "Mon, 17 Oct 2011 10:47:11 UTC;"));

        assertNull(cookieManager.getCookieStore().get(URI.create(url)).toString());
    }
}
