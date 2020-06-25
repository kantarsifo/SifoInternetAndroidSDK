package se.kantarsifo.mobileanalytics.framework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpCookie


/**
 * This class purpose is to look for a shared File containing a CookieKey String inside the TNS-Sifo Panelen application.
 * It will try to open a FileInputStream from the TNS-Sifo Panelen application.
 * If the package is not found or the CookieKey has no values an empty String will be returned and no Panelist users will be measured.
 * If a CookieKey is found it will be returned and used as cookie header for all HTTP requests.
 * The CookieKey will also be stored in the applications local CookieManager to be used in WebView requests.
 *
 * @author Niklas Björkholm (niklas.bjorkholm@idealapps.se)
 */
internal object PanelistHandler {

    fun getPanelistKey(context: Context): String {
        val inputStream = getSifoInputStream(context,
                TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME,
                TagStringsAndValues.SIFO_PANELIST_CREDENTIALS_FILENAME)
        return inputStream?.let { readCookieKeyString(it) }
                ?: TagStringsAndValues.NO_PANELIST_ID
    }

    fun syncCookies(context: Context, activity: ComponentActivity, onComplete: () -> Unit) {
        val pm = context.packageManager
        val isInstalled: Boolean = isPackageInstalled(TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME_V2, pm)
        val sharedPref = activity.getSharedPreferences(TagStringsAndValues.SIFO_PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (isInstalled) {
            if (shouldUpdateCookieValues(activity)) {
                clearPreferences(sharedPref)
                val url = "se.tns-sifo.internetpanelen://sync"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                ActivityResultHandler(activity, intent) { activityResult ->
                    if (activityResult != null) {
                        if (activityResult.resultCode == Activity.RESULT_OK) {
                            sharedPref.edit().putString(TagStringsAndValues.SIFO_PREFERENCE_COOKIES, activityResult.data?.dataString).commit()
                            sharedPref.edit().putLong(TagStringsAndValues.SIFO_PREFERENCE_COOKIES_SYNC_TIME, System.currentTimeMillis()).commit()
                        }
                    }
                    onComplete()
                }
                //han.launchIntent(intent)
            }else{
                onComplete()
            }
        }else {
            clearPreferences(sharedPref)
            onComplete()
        }
    }

    fun getCookies(context: Context, activity: ComponentActivity): List<HttpCookie>? {
        val sharedPref = activity.getSharedPreferences(TagStringsAndValues.SIFO_PREFERENCE_KEY, Context.MODE_PRIVATE)
        val panelistOnly = sharedPref.getBoolean(TagStringsAndValues.SIFO_COOKIES_IS_PANELIST_ONLY, false)
        val isWebBased = sharedPref.getBoolean(TagStringsAndValues.SIFO_COOKIES_IS_WEB_BASED, false)
        val version = BuildConfig.VERSION_NAME
        if (sharedPref.contains(TagStringsAndValues.SIFO_PREFERENCE_COOKIES)) {
            return readCookiesFromJson(sharedPref.getString(TagStringsAndValues.SIFO_PREFERENCE_COOKIES, "") ?: "", panelistOnly, isWebBased, version)
        } else { // Fallback to old version of reading cookies from sifo panel app shared preference
            val inputStream = getSifoInputStream(context,
                    TagStringsAndValues.SIFO_PANELIST_PACKAGE_NAME_V2,
                    TagStringsAndValues.SIFO_PANELIST_CREDENTIALS_FILENAME_V2)
            return inputStream?.let { readCookieStore(it, panelistOnly, isWebBased, version) }
        }
    }

    private fun clearPreferences(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().remove(TagStringsAndValues.SIFO_PREFERENCE_COOKIES).commit()
        sharedPreferences.edit().remove(TagStringsAndValues.SIFO_PREFERENCE_COOKIES_SYNC_TIME).commit()
    }

    private fun isPackageInstalled(packageName: String?, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Opens a FileInputStream from TNS Sifo-Panelen
     * reads the input stream from the TNS-Sifo Panelen application
     *
     * @param context The context of the application
     * @return The FileInputStream from TNS Sifo-Panelen or null if not found
     */
    private fun getSifoInputStream(context: Context, packageName: String, filename: String): FileInputStream? {
        return try {
            context.createPackageContext(packageName, 0).openFileInput(filename)
        } catch (e: Exception) {

            TSMobileAnalyticsBackend.logError("Error Getting InputStream")
            null
        }
    }

    private fun readCookiesFromJson(content: String, panelistOnly: Boolean, isWebBased: Boolean, version: String): List<HttpCookie> {
        val cookieList = mutableListOf<HttpCookie>()
        try {
            val jsonArray = JSONArray(content)
            for (i in 0 until jsonArray.length()) {
                val entry = jsonArray.getJSONObject(i)
                cookieList.add(CookieHandler.getCookieFromJson(entry, panelistOnly, isWebBased, version))
            }
        } catch (e: JSONException) {
            TSMobileAnalyticsBackend.logError("Error parsing TNS Panelist JSON data")
        }
        return cookieList
    }

    private fun readCookieStore(stream: FileInputStream, panelistOnly: Boolean, isWebBased: Boolean, version: String): List<HttpCookie> {
        val content = readFile(stream, "Error reading TNS Panelist cookies")
        val cookieList = mutableListOf<HttpCookie>()
        try {
            val jsonArray = JSONArray(content)
            for (i in 0 until jsonArray.length()) {
                val entry = jsonArray.getJSONObject(i)
                cookieList.add(CookieHandler.getCookieFromJson(entry, panelistOnly, isWebBased, version))
            }
        } catch (e: JSONException) {
            TSMobileAnalyticsBackend.logError("Error parsing TNS Panelist JSON data")
        }
        return cookieList
    }

    /**
     * Get the CookieKey from the TNS Sifo-Panelen
     *
     * @param stream The stream to use
     * @return The CookieKey read from the file or an empty String if not found
     */
    private fun readCookieKeyString(stream: FileInputStream): String {
        val content = readFile(stream, "Error reading TNS Panelist CookieKey")
        return content.trim()
    }

    private fun readFile(stream: FileInputStream, errorString: String): String {
        var input: BufferedReader? = null
        try {
            input = BufferedReader(InputStreamReader(stream))
            var line: String?
            val buffer = StringBuilder()
            while (input.readLine().also { line = it } != null) {
                buffer.append(line)
            }
            return buffer.toString()
        } catch (e: Exception) {
            TSMobileAnalyticsBackend.logError(errorString)
        } finally {
            try {
                input?.close()
            } catch (e: IOException) { // Should never happen
                TSMobileAnalyticsBackend.logError("Error Closing InputStream")
            }
        }
        return ""
    }

    private fun shouldUpdateCookieValues(activity: ComponentActivity): Boolean {
        val sharedPref = activity.getSharedPreferences(TagStringsAndValues.SIFO_PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (sharedPref.contains(TagStringsAndValues.SIFO_PREFERENCE_COOKIES_SYNC_TIME).not()) {
            return true
        }
        val syncTime = sharedPref.getLong(TagStringsAndValues.SIFO_PREFERENCE_COOKIES_SYNC_TIME, 0)
        val interval = TSMDateUtil.elapsedTimeInDays(syncTime, System.currentTimeMillis())
        return interval > 90
    }
}