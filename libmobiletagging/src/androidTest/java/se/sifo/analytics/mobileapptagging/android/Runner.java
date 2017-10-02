package se.sifo.analytics.mobileapptagging.android;

import junit.framework.TestSuite;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

public class Runner extends InstrumentationTestRunner {

	@Override
	public void onCreate (final Bundle arguments) {
		super.onCreate(arguments);

		/**
		 * Workaround for an incompatibility in current dexmaker (1.2) implementation and Android >= 4.3
		 * @See <a https://code.google.com/p/dexmaker/issues/detail?id=2">dexmaker Issue tracking</a>
		 */
		System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
	}

	@Override
	public TestSuite getTestSuite() {
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
		suite.addTestSuite(TagHandlerTest.class);
		suite.addTestSuite(CookieHandlerTest.class);
		suite.addTestSuite(TagDataRequestHandlerTest.class);
		suite.addTestSuite(TSMobileAnalyticsTest.class);
		return suite;
	}

	@Override
	public ClassLoader getLoader() {
		return Runner.class.getClassLoader();
	}

}