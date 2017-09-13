package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.webkit.CookieManager;

import org.mockito.Mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static se.sifo.analytics.mobileapptagging.android.Constants.APP_NAME;
import static se.sifo.analytics.mobileapptagging.android.Constants.CODIGO_CPID;
import static se.sifo.analytics.mobileapptagging.android.Constants.FILE_NAME;
import static se.sifo.analytics.mobileapptagging.android.Constants.PACKAGE_NAME;
import static se.sifo.analytics.mobileapptagging.android.TagStringsAndValues.DOMAIN_CODIGO;
import static se.sifo.analytics.mobileapptagging.android.Utils.convertStreamToFile;

public class MobileTaggingFrameworkTest extends InstrumentationTestCase {

    @Mock
    Context fakeContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initMocks(this);

        final Context mockContext = mock(Context.class);

        InputStream inputStream = getInstrumentation()
                .getContext()
                .getResources()
                .openRawResource(se.sifo.analytics.mobileapptagging.android.instrumenttest.
                        R.raw.sifo_cookie_key_json);

        /**
         * @See <a href="http://stackoverflow.com/questions/31294419/mock-fileinputstream-for-androidinstrumentation-test">
         *     Issue tracking</a>
         */
        final File file = File.createTempFile("file", ".txt");
        convertStreamToFile(inputStream, file);

        final FileInputStream fileInputStream = new FileInputStream(file);

        when(fakeContext.createPackageContext(PACKAGE_NAME, 0)).thenReturn(mockContext);

        when(mockContext.openFileInput(FILE_NAME)).thenReturn(fileInputStream);


        //Mock packageManager added as a Workaround for PackageManager.NameNotFoundException
        // when reinstalling Webview Android System
        final PackageManager packageManager = mock(PackageManager.class);

        when(fakeContext.getPackageManager()).thenReturn(packageManager);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        CookieManager.getInstance().removeAllCookie();
    }

    @SmallTest
    public void testMobileTaggingFramework_createInstanceDoesNotDeleteThirdPartyCookies() {
        MobileTaggingFramework.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertNotNull(CookieManager.getInstance()
                .getCookie(TagStringsAndValues.DOMAIN_CODIGO));
    }

    @SmallTest
    public void testMobileTaggingFramework_createInstanceSetFrameworkCookiesInCookieManager() {
        final String thirdPartyDomain = "third.party.site";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(thirdPartyDomain, cookieValue);

        MobileTaggingFramework.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertEquals(cookieValue,
                cookieManager.getCookie(thirdPartyDomain));
    }

    @SmallTest
    public void testMobileTaggingFramework_deletesOldFrameworkCookies() {

        final CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setCookie(DOMAIN_CODIGO, "OldKey1=OldValue1");

        MobileTaggingFramework.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertFalse(cookieManager.getCookie(DOMAIN_CODIGO).contains("OldKey1=OldValue1"));
    }

    @SmallTest
    public void testSetAndGetACookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        String url = "bh.mobiletech.no";
        String value = "my cookie";
        cookieManager.setCookie(url, value);
        assertEquals(cookieManager.getCookie(url), value);
    }
}
