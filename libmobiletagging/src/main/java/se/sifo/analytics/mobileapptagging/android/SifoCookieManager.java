package se.sifo.analytics.mobileapptagging.android;

/**
 * Created by ahmetcengiz on 15/09/2017.
 */

import android.util.Log;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

class SifoCookieManager
{

    private CookieManager mCookieManager;

    private static SifoCookieManager instance;

    public static SifoCookieManager getInstance() {
        if (instance == null) {
            instance = new SifoCookieManager();
        }

        return instance;
    }

    private SifoCookieManager() {
        mCookieManager = new CookieManager();
        mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);
    }

    private List<HttpCookie> getCookies() {
        if(mCookieManager == null)
            return null;
        else
            return mCookieManager.getCookieStore().getCookies();
    }

    public void clearCookies() {
        if(mCookieManager != null)
            mCookieManager.getCookieStore().removeAll();
    }

    private boolean isCookieManagerEmpty() {
        if(mCookieManager == null)
            return true;
        else
            return mCookieManager.getCookieStore().getCookies().isEmpty();
    }

    public CookieStore getCookieStore() {
        if(mCookieManager == null)
            return null;
        else
            return mCookieManager.getCookieStore();
    }


    public String getCookieValue() {
        String cookieValue = new String();

        if(!isCookieManagerEmpty()) {
            for (HttpCookie eachCookie : getCookies())
                cookieValue = cookieValue + String.format("%s=%s; ", eachCookie.getName(), eachCookie.getValue());
        }

        return cookieValue;
    }

}