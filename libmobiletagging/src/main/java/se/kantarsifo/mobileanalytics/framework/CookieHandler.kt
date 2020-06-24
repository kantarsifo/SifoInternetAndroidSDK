package se.kantarsifo.mobileanalytics.framework

import org.json.JSONException
import org.json.JSONObject
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.Calendar

internal object CookieHandler {

    private const val EXPIRY_YEARS = 10

    /**
     * Stores Panelist Cookie Headers in CookieSyncManager to use in WebView requests
     */
    fun setupPanelistCookies(cookies: List<HttpCookie>): CookieStore {
        for (cookie in cookies) {
            try {
                SifoCookieManager.cookieStore.add(URI(cookie.domain), cookie)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
        return SifoCookieManager.cookieStore
    }

    @Throws(JSONException::class)
    fun getCookieFromJson(json: JSONObject, isPanelistOnly: Boolean, isWebBased: Boolean, version: String): HttpCookie {
        return createHttpCookie(
                name = json.getString("key"),
                value = "trackPanelistOnly=$isPanelistOnly&isWebViewBased=$isWebBased" + "&" + "sdkVersion=$version" + "&" + json.getString("value"),
                path = json.getString("path"),
                domain = json.getString("domain")
        )
    }

    fun getCookieString(cookies: List<HttpCookie>): String {
        return cookies.map { getCookieString(it) }
                .toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", ";")
    }

    fun createLegacyCookies(panelistKey: String?): List<HttpCookie> {
        return listOf(
                createHttpCookie(name = TagStringsAndValues.SIFO_MEASURE_COOKIE, value = panelistKey),
                createHttpCookie(name = TagStringsAndValues.SIFO_PANELIST_COOKIE, value = panelistKey)
        )
    }

    // Private methods

    private fun getCookieString(cookie: HttpCookie): String {
        return String.format("%s=%s", cookie.name, cookie.value)
    }

    private fun createHttpCookie(name: String,
                                 value: String?,
                                 path: String = "/",
                                 domain: String = TagStringsAndValues.DOMAIN_CODIGO
    ): HttpCookie {
        return HttpCookie(name, value).apply {
            this.version = 1
            this.path = path
            this.domain = domain
            this.maxAge = CookieHandler.getMaxAge()
        }
    }

    private fun getMaxAge(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, EXPIRY_YEARS)
        return calendar.time.time
    }

}
