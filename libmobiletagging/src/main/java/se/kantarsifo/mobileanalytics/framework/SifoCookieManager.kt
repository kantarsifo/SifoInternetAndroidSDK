package se.kantarsifo.mobileanalytics.framework

import android.os.Build
import android.util.Log
import android.webkit.WebView
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.HttpCookie


object SifoCookieManager {

    private var cookieManager: CookieManager

    val cookieStore: CookieStore
        get() = cookieManager.cookieStore

    val cookies: List<HttpCookie>
        get() = cookieManager.cookieStore.cookies

    init {
        if (CookieHandler.getDefault() != null) {
            cookieManager = CookieHandler.getDefault() as CookieManager
        } else {
            cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE)
            CookieHandler.setDefault(cookieManager)
        }
    }

    fun activateCookies(webView: WebView) {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }
        val cookies = cookieManager.cookieStore.cookies
        for (cookie in cookies) {
            val cookieString = cookie.name + "=" + cookie.value + "; Domain=" + cookie.domain
            Log.e("cookieString",cookieString)
            android.webkit.CookieManager.getInstance().setCookie(cookie.domain, cookieString)
        }
    }


}
