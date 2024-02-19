package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest


class TSMConfigUtil {
    companion object {
        fun syncConfig(context: Context, appName: String) {
            val packageMngr = context.packageManager
            val packageName = context.packageName
            val packageInfo = packageMngr.getPackageInfo(packageName, 0)
            var version = ""
            val sharedPref = context.getSharedPreferences(TagStringsAndValues.SIFO_PREFERENCE_KEY, Context.MODE_PRIVATE)
            val config = sharedPref.getString(TagStringsAndValues.SIFO_PREFERENCE_CONFIG, "")
            var md5 = "nohash"
            try {
                if (config.isNullOrEmpty().not()) {
                    val json = JSONObject(config)
                    md5 = (json.getString("BaseMeasurementAddress") ?: "").toMD5()
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                version = packageInfo.versionName
            val url = URL("${TagStringsAndValues.BASE_CONFIG_URL}/App/GetBaseConfig?currentConfigHash=$md5&sdkversion=$version&appname=$appName")
            (url.openConnection() as? HttpURLConnection)?.run {
                doInput = true
                try {
                    readStream(inputStream, sharedPref)
                } finally {
                    if (inputStream != null) {
                        try {
                            // this will close the bReader as well
                            inputStream.close()
                        } catch (ignored: IOException) {
                            ignored.printStackTrace()
                        }
                    }
                    disconnect()
                }
            }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
        }

        private fun readStream(inputStream: InputStream, sharedPreferences: SharedPreferences) {
            val bReader = BufferedReader(InputStreamReader(inputStream))
            var temp: String? = bReader.readLine()
            var response: String? = ""
            while (temp != null) {
                response += temp
                temp = bReader.readLine()
            }
            if (response.isNullOrBlank()) {
                return
            }
            val json = JSONTokener(response).nextValue() as JSONObject
            sharedPreferences.edit().putString(TagStringsAndValues.SIFO_PREFERENCE_CONFIG, json.toString()).commit()
        }

        fun String.toMD5(): String {
            // toByteArray: default is Charsets.UTF_8 - https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/to-byte-array.html
            val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
            return bytes.toHex()
        }

        fun ByteArray.toHex(): String {
            return joinToString("") { "%02x".format(it) }
        }
    }



}