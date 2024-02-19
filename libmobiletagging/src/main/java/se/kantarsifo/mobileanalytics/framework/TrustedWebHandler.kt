package se.kantarsifo.mobileanalytics.framework

import android.net.Uri
import androidx.activity.ComponentActivity
import com.google.androidbrowserhelper.trusted.TwaLauncher
import se.kantarsifo.mobileanalytics.framework.Utils.getApplicationVersion
import java.lang.Exception
import java.net.HttpCookie


internal class TrustedWebHandler(
    private var twaInfo:TWAModel,
    private val trackPanelistOnly:Boolean,
    private val isWebViewBased:Boolean
) {

    fun open(activity: ComponentActivity) {
        var url = ""
        if (twaInfo.url.isEmpty()){
            throw RuntimeException("you should set twa url first in analytics instance and ends with /")
        }
        url = twaInfo.url
        val panelistData = PanelistHandler.getCookies(activity)
        val sdkId = getSdkId(panelistData)
        url += if (sdkId.isNotEmpty()){
            "?sdkid=$sdkId"
        }else{
            "?sdkid=${getSdkId()}"
        }
        try {
            val uri = Uri.parse(url).buildUpon()
                .appendQueryParameter("isWebViewBased",isWebViewBased.toString())
                .appendQueryParameter("sdkVersion",BuildConfig.VERSION_NAME)
                .appendQueryParameter("appVersion", activity.getApplicationVersion())
                .appendQueryParameter("domain",TagStringsAndValues.DOMAIN_CODIGO)
                .appendQueryParameter("trackPanelistOnly",trackPanelistOnly.toString())
                .addExtraParams(twaInfo.extraParams)
                .build()
            val launcher = TwaLauncher(activity)
            launcher.launch(uri)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }



    private fun getSdkId(panelistData: List<HttpCookie>?) :String{
        return try {
            var params = ""
            panelistData?.forEach {
                params += it.value + "&"
            }
            params.dropLast(1)
            params.split("&").findLast { it.contains("BID") }?.split("-")?.lastOrNull() ?: ""
        }catch (e:Exception){
            ""
        }
    }


    private fun getSdkId() :String{
        return ((10000000..19999999).random()).toString()
    }


}




fun Uri.Builder.addExtraParams(extraParams: Map<String, Any>): Uri.Builder {
    for ((key,value )in extraParams){
        appendQueryParameter(key,value.toString())
    }
    return this
}
