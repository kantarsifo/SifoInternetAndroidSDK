/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import java.io.IOException
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

/**
 * TNS SIFO Mobile Application Tagging Framework :
 * Debugging/Advanced features : TagDataRequest.java :
 *
 *
 * This class describes an object with information about a measure-tag request.
 */
class TagDataRequest

/**
 * Constructor, to be used internally by framework only.
 *
 * @param cat                         The catalog value (cat) to be sent in this request.
 * @param iD                          The identifier value (id) to be sent in this request.
 * @param uRL                         The url that this request is calling.
 * @param trackPanelistOnly           The trackPanelistOnly to be sent in userAgent
 * @param applicationName             The name of the application created from initialize.
 * @param applicationVersion          The version name found set in the AndroidManifest.Xml.
 * @param callbackListener            The callback listener to be called when request is finished.
 * @param userDefinedCallbackListener The callback listener defined by user.
 */
internal constructor(
        val cat: String,
        val iD: String,
        val uRL: String?,
        private val trackPanelistOnly: Boolean,
        private val applicationName: String?,
        private val applicationVersion: String?,
        private val callbackListener: TagDataRequestCallbackListener?,
        private val userDefinedCallbackListener: TagDataRequestCallbackListener?) {

    /**
     * The Unique ID of this specific request, can be used for tracking etc.
     */
    val requestID: UUID = UUID.randomUUID()

    /**
     * The HTTP response code for the request.
     *  0 if request is still pending or if it failed with an exception.
     */
    val httpStatusCode = 0

    /**
     * Init the server request to the specified URL. This function will start a new Thread.
     */
    fun initRequest() {
        // HTTPS-connection setup
        if (uRL.isNullOrEmpty().not()) {
            var con: HttpsURLConnection? = null
            try {
                con = openConnection()
                logRequestInfo()
                if (con.responseCode == 200) {
                    dataRequestComplete()
                } else {
                    dataRequestFailWithResponseCode(con.responseCode, con.responseMessage)
                }
            } catch (e: IOException) {
                dataRequestFail(e)
            } finally {
                con?.disconnect()
            }
        }
    }

    // Private methods

    /**
     * Open an HTTPS connection and set the headers.
     */
    private fun openConnection(): HttpsURLConnection {
        val con = URL(uRL).openConnection() as HttpsURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", getUserAgent())
        con.setRequestProperty("Cookie", getCookieString())
        return con
    }

    /**
     * Build and return the User-Agent string.
     */
    private fun getUserAgent(): String {
        return "$applicationName/$applicationVersion " +
                "session_id=sdk_android_${TSMobileAnalytics.instance?.libraryVersion} " +
                "${System.getProperty("http.agent")} " +
                "TrackPanelistOnly=${trackPanelistOnly} " +
                "IsWebViewBased=${TSMobileAnalytics.isWebViewBased}"
    }

    /**
     * Build and return the Cookie string.
     */
    private fun getCookieString(): String {
        return CookieHandler.getCookieString(SifoCookieManager.cookies)
    }

    /**
     * Print request information to LogCat.
     */
    private fun logRequestInfo() {
        TSMobileAnalyticsBackend.logMessage(
                "Tag request sent: " +
                        "\nRequestID: " + requestID +
                        "\nCat encoded value:" + TagHandler.urlEncode(cat) +
                        "\nCat plain value: " + cat +
                        "\nId: " + iD +
                        "\nURL:\n" + uRL)
    }

    /**
     * Handle a successful request.
     */
    private fun dataRequestComplete() {
        TSMobileAnalyticsBackend.logMessage("Tag request completed with success: \nRequestID: $requestID")
        notifySuccess()
    }

    /**
     * Handle a failed request.
     * @param e The exception if one was thrown.
     */
    private fun dataRequestFail(e: Exception) {
        TSMobileAnalyticsBackend.logError("Tag request failed with exception:\n$e\nRequestID: $requestID")
        notifyFailure()
    }

    /**
     * Handle a failed request.
     * @param statusCode HTTP response code.
     * @param message HTTP response message.
     */
    private fun dataRequestFailWithResponseCode(statusCode: Int, message: String) {
        TSMobileAnalyticsBackend.logError("Tag request failed with http status code:$statusCode\nmessage:$message\nRequestID: $requestID")
        notifyFailure()
    }

    /**
     * Tell our callback listeners that the request was successful.
     */
    private fun notifySuccess() {
        callbackListener?.onDataRequestComplete(this)
        userDefinedCallbackListener?.onDataRequestComplete(this)
    }

    /**
     * Tell our callback listeners that the request failed.
     */
    private fun notifyFailure() {
        callbackListener?.onDataRequestFailed(this)
        userDefinedCallbackListener?.onDataRequestFailed(this)
    }

}
