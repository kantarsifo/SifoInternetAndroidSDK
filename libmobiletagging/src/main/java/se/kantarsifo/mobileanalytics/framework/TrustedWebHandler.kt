package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.net.Uri
import android.provider.FontRequest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.trusted.TrustedWebActivityIntentBuilder
import com.google.androidbrowserhelper.trusted.TwaLauncher
import se.kantarsifo.mobileanalytics.framework.Utils.getApplicationVersion
import java.lang.Exception
import java.net.HttpCookie


internal class TrustedWebHandler(
    private var twaUrl:String,
    private val context: Context,
    private val trackPanelistOnly:Boolean,
    private val isWebViewBased:Boolean
) {

    fun open() {
        var url = ""
        if (twaUrl.isEmpty()){
            throw RuntimeException("you should set twa url first in analytics instance and ends with /")
        }
        url = twaUrl
        val panelistData = PanelistHandler.getCookies(context,context as ComponentActivity)
        val cookiesParams = appendPanelistDataUrl(panelistData)
        if (cookiesParams.isNotEmpty()){
            url += "?$cookiesParams"
        }
        try {
            val uri = Uri.parse(url).buildUpon()
                .appendQueryParameter("sifo_config","trackPanelistOnly=$trackPanelistOnly")
                .appendQueryParameter("isWebViewBased",isWebViewBased.toString())
                .appendQueryParameter("sdkVersion",BuildConfig.VERSION_NAME)
                .appendQueryParameter("appVersion", context.getApplicationVersion())
                .appendQueryParameter("domain",TagStringsAndValues.DOMAIN_CODIGO)
                .build()

            val launcher = TwaLauncher(context)
            launcher.launch(uri)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun appendPanelistDataUrl(panelistData: List<HttpCookie>?) :String{
        return try {
            var params = ""
            panelistData?.forEach {
                params += it.value + "&"
            }
            params.dropLast(1)
        }catch (e:Exception){
            ""
        }
    }

}

