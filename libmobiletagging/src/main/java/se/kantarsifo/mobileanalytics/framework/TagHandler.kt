/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings.Secure
import androidx.activity.ComponentActivity
import org.json.JSONObject
import se.kantarsifo.mobileanalytics.framework.Logger.error
import se.kantarsifo.mobileanalytics.framework.Logger.fatalError
import se.kantarsifo.mobileanalytics.framework.Logger.log
import se.kantarsifo.mobileanalytics.framework.Utils.getApplicationVersion
import java.io.UnsupportedEncodingException
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URLEncoder

/**
 * This class creates request-tags using the values [cpId], [type], [category], [ref], id, [euid] and [euidq].
 * Its main purpose is to combine these values to create a valid tag request-URL with correct encoding.
 * The values [cpId] and [type] are provided on creation, while the values [category], [ref] and id are provided each
 * time a URL is created. The [euid] is created by a device identifier retrieved directly by this class using
 * an application Context object.
 *
 */
internal class TagHandler(
    val context: Context,
    private val cpId: String,
    ref: String,
    cookies: List<HttpCookie>?
) {

    var applicationName: String = ref
        private set
    var applicationVersion: String? = null
        private set

    private var type = TagStringsAndValues.TYPE
    private var euidq = TagStringsAndValues.EUIDQ
    private var ref: String = TagStringsAndValues.APP_NAME_PREFIX + ref
    private var urlBase: String = TagStringsAndValues.CODIGO_URL_BASE_HTTPS
    private var euid: String? = null
    private var cookies: CookieStore? = null

    constructor(context: Context, cpId: String, ref: String, panelistKey: String?) :
            this(context, cpId, ref, CookieHandler.createLegacyCookies(panelistKey))

    init {
        generateEuId(context)
        cookies?.let { initCookies(it) }
        applicationVersion = context.getApplicationVersion()
    }

    fun refresh(panelistKey: String?) {
        val cookies = CookieHandler.createLegacyCookies(panelistKey)
        initCookies(cookies)
    }

    fun refresh(cookies: List<HttpCookie>) {
        initCookies(cookies)
    }

    /**
     * Get a tag-request URL for the specified values of [category], [id]
     */
    fun getURL(category: String, id: String, activity: ComponentActivity?): String? {
        var url: String? = null
        if (paramsAreValid(category, id)) {
            url = fetchUrl(activity)
            val encodedRef = urlEncode(ref.trim())
            val encodedCategory = urlEncode(category.trim())
            val encodedId = urlEncode(id.trim())
            val encodedName = urlEncode(applicationName.trim())

            url = url.replace("{siteIdValue}", cpId)
                    .replace("{appClientIdValue}", euid ?: "")
                    .replace("{cpValue}", encodedCategory)
                    .replace("{appIDValue}", encodedId)
                    .replace("{type}", type)
                    .replace("{appNameValue}", type) // This seems like a mistake but the client confirmed they want it like this.
                    .replace("{appRefValue}", encodedRef)

            applicationVersion?.apply {
                val encodedVersion = urlEncode(trim())
                val uri = Uri.parse(url)
                    .buildUpon()
                    .appendQueryParameter("appVersion",encodedVersion)
                    .build()
                url = uri.toString()
            }


        }
        return url
    }

    // Private methods

    /**
     * Generate the Unique identifier value for this device used as [euid] by this handler.
     *
     * @param context The context of this application.
     */
    @SuppressLint("HardwareIds")
    private fun generateEuId(context: Context) {
        euid = try {
            Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        } catch (e: NullPointerException) {
            ""
        }
    }

    private fun fetchUrl(activity: ComponentActivity?): String {
        val json = activity?.getSharedPreferences(
            TagStringsAndValues.SIFO_PREFERENCE_KEY,
            Context.MODE_PRIVATE
        )?.getString(TagStringsAndValues.SIFO_PREFERENCE_CONFIG, "")
        if (json.isNullOrEmpty()) {
            return TagStringsAndValues.SIFO_DEFAULT_CONFIG
        }
        var savedUrl = ""
        try {
            savedUrl = JSONObject(json).getString("BaseMeasurementAddress")
        } catch (e: Exception) {

        }

        return if (savedUrl.isNullOrEmpty()) {
            TagStringsAndValues.SIFO_DEFAULT_CONFIG
        } else {
            savedUrl
        }
    }

    private fun initCookies(cookieList: List<HttpCookie>) {
        val setupPanelListCookies = SetupPanelListCookies(context, cookieList)
        setupPanelListCookies.run()
    }

    private fun paramsAreValid(category: String?, id: String?): Boolean {
        return when {
            (category == null) -> {
                logFatalError("category may not be null")
                false
            }
            (category.length > TagStringsAndValues.MAX_LENGTH_CATEGORY) -> {
                logFatalError("category may not have more than ${TagStringsAndValues.MAX_LENGTH_CATEGORY} characters")
                false
            }
            (id == null) -> {
                logFatalError("ID may not be null")
                false
            }
            (id.length > TagStringsAndValues.MAX_LENGTH_CONTENT_ID) -> {
                logFatalError("ID may not have more than ${TagStringsAndValues.MAX_LENGTH_CONTENT_ID} characters")
                false
            }
            else -> true
        }
    }

    private fun logFatalError(message: String) {
        fatalError("Failed to create URL - $message")
    }

    companion object {
        /**
         * Make UTF-8 encoding on a specified String.
         *
         * @param input The String to encode.
         * @return The encoded String.
         */
        fun urlEncode(input: String): String {
            return try {
                URLEncoder.encode(input, TagStringsAndValues.URL_ENCODING)
            } catch (e: UnsupportedEncodingException) {
                // Since encoding UTF-8 is supported by android, this should not happen
                log("URL-Encoding not supported")
                input
            }
        }
    }

    /**
     * Workaround for NameNotFoundException when app tries to access
     * webview while it is reinstalling Android System Webview for Android Lollipop
     * @See <a https:></a>//code.google.com/p/android/issues/detail?id=175124">android Issue tracking
     * @See <a https:></a>//code.google.com/p/chromium/issues/detail?id=506369">chromium Issue tracking
     */
    private inner class SetupPanelListCookies(
        private val context: Context,
        private val cookieList: List<HttpCookie>
    ) : Runnable {

        private val systemWebViewPackageName = "com.google.android.webview"
        private val maxWebViewPackageRetry = 6
        private var retryCounter = 0

        override fun run() {
            try {
                if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && retryCounter++ < maxWebViewPackageRetry) {
                    context.packageManager.getPackageInfo(
                        systemWebViewPackageName,
                        PackageManager.GET_ACTIVITIES
                    )
                }
                cookies = CookieHandler.setupPanelistCookies(cookieList)
            } catch (e: PackageManager.NameNotFoundException) {
                error("Failed to setup panel list cookies - Retry counter = $retryCounter")
            }
        }
    }
}