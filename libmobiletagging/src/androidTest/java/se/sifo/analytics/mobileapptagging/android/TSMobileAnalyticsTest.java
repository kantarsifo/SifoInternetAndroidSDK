package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.mockito.Mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static se.sifo.analytics.mobileapptagging.android.Constants.APP_NAME;
import static se.sifo.analytics.mobileapptagging.android.Constants.CODIGO_CPID;
import static se.sifo.analytics.mobileapptagging.android.Constants.FILE_NAME;
import static se.sifo.analytics.mobileapptagging.android.Constants.PACKAGE_NAME;
import static se.sifo.analytics.mobileapptagging.android.TagStringsAndValues.DOMAIN_CODIGO;
import static se.sifo.analytics.mobileapptagging.android.Utils.convertStreamToFile;

public class TSMobileAnalyticsTest extends InstrumentationTestCase {

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
        SifoCookieManager.getInstance().clearCookies();
    }

    @SmallTest
    public void testTSMobileAnalytics_createInstanceDoesNotDeleteThirdPartyCookies() {
        TSMobileAnalytics.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertNotNull(SifoCookieManager.getInstance()
                .getCookieStore().get(URI.create(TagStringsAndValues.DOMAIN_CODIGO)).toString());
    }

    @SmallTest
    public void testTSMobileAnalytics_createInstanceSetFrameworkCookiesInCookieManager() {
        final String thirdPartyDomain = "third.party.site";
        final String cookieValue = "ThirdPartyCookie1=ThirdPartyValue1";

        SifoCookieManager cookieManager = SifoCookieManager.getInstance();
        cookieManager.getCookieStore().add(URI.create(thirdPartyDomain), new HttpCookie("ThirdPartyCookie1","ThirdPartyValue1"));

        TSMobileAnalytics.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertEquals(cookieValue,
                cookieManager.getCookieStore().get(URI.create(thirdPartyDomain)).toString());
    }

    @SmallTest
    public void testTSMobileAnalytics_deletesOldFrameworkCookies() {

        final SifoCookieManager cookieManager = SifoCookieManager.getInstance();

        cookieManager.getCookieStore().add(URI.create(DOMAIN_CODIGO), new HttpCookie("OldKey1","OldValue1"));

        TSMobileAnalytics.createInstance(fakeContext,
                CODIGO_CPID,
                APP_NAME);

        assertFalse(cookieManager.getCookieStore().getCookies().contains("OldKey1=OldValue1"));
    }

    @SmallTest
    public void testSetAndGetACookie() {
        SifoCookieManager cookieManager = SifoCookieManager.getInstance();
        String url = "bh.mobiletech.no";
        String value = "my cookie";
        cookieManager.getCookieStore().add(URI.create(url), new HttpCookie("my cookie",value));
        assertEquals(value, cookieManager.getCookieStore().get(URI.create(url)));
    }
}
