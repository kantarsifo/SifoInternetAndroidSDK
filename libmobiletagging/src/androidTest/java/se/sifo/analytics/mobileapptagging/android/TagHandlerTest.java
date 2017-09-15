package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.webkit.CookieManager;


import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import static se.sifo.analytics.mobileapptagging.android.TagStringsAndValues.DOMAIN_CODIGO;
import static se.sifo.analytics.mobileapptagging.android.Constants.CODIGO_CPID;
import static se.sifo.analytics.mobileapptagging.android.Constants.APP_NAME;

public class TagHandlerTest extends AndroidTestCase {

    private Context mAppCtx;
    private TagHandler mTagHandler;
    private List<HttpCookie> mCookies;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mAppCtx = getContext().getApplicationContext();

        mCookies = new ArrayList<>();

        HttpCookie cookie1 = new HttpCookie("Key1", "Value1");
        cookie1.setDomain(DOMAIN_CODIGO);

        HttpCookie cookie2 = new HttpCookie("Key2", "Value2");
        cookie2.setDomain(DOMAIN_CODIGO);

        mCookies.add(cookie1);
        mCookies.add(cookie2);

        mTagHandler = new TagHandler(mAppCtx,
                CODIGO_CPID,
                APP_NAME,
                mCookies);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @SmallTest
    public void testPreConditions() {
        assertNotNull(CookieManager.getInstance().getCookie(DOMAIN_CODIGO));
    }

    @SmallTest
    public void testRefresh_doesNotDeleteThirdPartyCookies() {
        final String thirdPartyDomain = "third.party.site";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(thirdPartyDomain, cookieValue);

        mTagHandler.refresh(mAppCtx, mCookies);

        assertEquals(cookieValue,
                cookieManager.getCookie(thirdPartyDomain));
    }

    @SmallTest
    public void testRefresh_deletesOldFrameworkCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(DOMAIN_CODIGO, "OldKey1=OldValue1");

        mTagHandler.refresh(mAppCtx, mCookies);

        assertEquals("Key1=Value1; Key2=Value2",
                cookieManager.getCookie(DOMAIN_CODIGO));
    }
}
