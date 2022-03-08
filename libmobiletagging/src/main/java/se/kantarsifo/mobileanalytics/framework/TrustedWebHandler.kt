package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.net.Uri
import androidx.browser.trusted.TrustedWebActivityIntentBuilder
import com.google.androidbrowserhelper.trusted.TwaLauncher
import se.kantarsifo.mobileanalytics.framework.Utils.getApplicationVersion


internal class TrustedWebHandler(
    private val twaUrl:String,
    private val context: Context,
    private val trackPanelistOnly:Boolean,
    private val isWebViewBased:Boolean
) {

    fun open() {
        val uri = Uri.parse(twaUrl)
        val urLBuilder= uri
            .buildUpon()
        getTWAParams(context).let { map ->
            for ((key, value) in map) {
                urLBuilder.appendQueryParameter(key,value.toString())
            }
        }
        urLBuilder.build()
        val builder = TrustedWebActivityIntentBuilder(uri)
        val launcher = TwaLauncher(context)
        launcher.launch(uri)
    }


    private fun getTWAParams(context: Context): Map<String, Any> {
        return mapOf<String, Any>().apply {
            "sifo_config" to "trackPanelistOnly=$trackPanelistOnly"
            "isWebViewBased" to isWebViewBased
            "sdkVersion" to BuildConfig.VERSION_NAME
            "appVersion" to context.getApplicationVersion()
            "domain" to TagStringsAndValues.DOMAIN_CODIGO
        }
    }

}

