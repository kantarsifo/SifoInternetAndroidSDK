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
        if (twaUrl.isEmpty()){
            throw RuntimeException("you should set twa url first in analytics instance")
        }
        val uri = Uri.parse(twaUrl).buildUpon()
            .appendQueryParameter("sifo_config","trackPanelistOnly=$trackPanelistOnly")
            .appendQueryParameter("isWebViewBased",isWebViewBased.toString())
            .appendQueryParameter("sdkVersion",BuildConfig.VERSION_NAME)
            .appendQueryParameter("appVersion", context.getApplicationVersion())
            .appendQueryParameter("domain",TagStringsAndValues.DOMAIN_CODIGO)
            .build()
        val launcher = TwaLauncher(context)
        launcher.launch(uri)
    }

}

