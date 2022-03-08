/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

import android.content.Context
import androidx.activity.ComponentActivity
import se.kantarsifo.mobileanalytics.framework.Logger.error
import se.kantarsifo.mobileanalytics.framework.Logger.fatalError
import se.kantarsifo.mobileanalytics.framework.Logger.log
import se.kantarsifo.mobileanalytics.framework.TagDataRequestHandler.State.PREPARING
import se.kantarsifo.mobileanalytics.framework.TagDataRequestHandler.State.READY
import java.net.HttpCookie
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * API for the data requests and URL handling etc. Used by the framework top
 * layer to execute the functions called by the user.
 */
internal class TagDataRequestHandler : TagDataRequestCallbackListener {

    var state = PREPARING

    /**
     * Counter keeping track of the number of successful requests.
     */
    var nbrOfSuccessfulRequests = 0
        private set

    /**
     * Counter keeping track of the number of failed requests.
     */
    var nbrOfFailedRequests = 0
        private set

    /**
     * Our queue of pending requests.
     */
    var dataRequestQueue: MutableList<TagDataRequest>
        private set

    /**
     * Our TagHandler.
     */
    private var tagHandler: TagHandler

    private var activityForPref: ComponentActivity? = null

    private lateinit var trustedWebHandler: TrustedWebHandler

    /**
     * The callback-listener specified by the user.
     */
    var userCallbackListener: TagDataRequestCallbackListener? = null

    /**
     * The thread handling requests.
     */
    private var threadPool: ExecutorService

    /**
     * tackPanelistOnly, default is false
     */
    private var trackPanelistOnly: Boolean = false

    /**
     * Create a new handler for specified context and application information.
     *
     * @param context           The context of the application.
     * @param cpId              The customer ID of the application.
     * @param applicationName   The name of the application.
     * @param cookies           The list of cookies to send with measurement requests.
     */
    constructor(
        context: Context,
        activity: ComponentActivity,
        cpId: String,
        applicationName: String,
        cookies: List<HttpCookie>?,
        trackPanelistOnly: Boolean,
        twaUrl: String = "",
        isWebBased:Boolean = false
    ) {
        tagHandler = TagHandler(context, cpId, applicationName, cookies)
        dataRequestQueue = ArrayList()
        activityForPref = activity
        threadPool = Executors.newScheduledThreadPool(MAX_NBR_OF_THREADS)
        this.trackPanelistOnly = trackPanelistOnly
        trustedWebHandler = TrustedWebHandler(
            twaUrl = twaUrl,
            context = context,
            trackPanelistOnly = trackPanelistOnly,
            isWebViewBased = isWebBased
        )
    }

    /**
     * Create a new handler for specified context and application information.
     *
     * @param context           The context of the application.
     * @param cpId              The customer ID of the application.
     * @param applicationName   The name of the application.
     * @param panelistKey       The panelist key.
     */
    constructor(
        context: Context,
        activity: ComponentActivity,
        cpId: String,
        applicationName: String,
        panelistKey: String,
        trackPanelistOnly: Boolean
    ) {
        tagHandler = TagHandler(context, cpId, applicationName, panelistKey)
        dataRequestQueue = ArrayList()
        activityForPref = activity
        threadPool = Executors.newScheduledThreadPool(MAX_NBR_OF_THREADS)
        this.trackPanelistOnly = trackPanelistOnly
    }

    fun refreshCookies(cookies: List<HttpCookie>) {
        tagHandler.refresh(cookies)
    }

    fun refreshCookies(panelistKey: String) {
        tagHandler.refresh(panelistKey)
    }

    /**
     * Send a tag-request to the server.
     *
     * @param category      The name of category or page to be tagged. This value will be sent using
     *                      the "cat"-attribute.
     * @param contentID     Value to identify specific content within the category, such as a specific article.
     */
    @JvmOverloads
    fun performMetricsRequest(category: String?, contentID: String? = ""): Int {
        val result = checkRequestParams(category, contentID)
        if (result == TagStringsAndValues.RESULT_SUCCESS) {
            val request = TagDataRequest(
                category!!,
                contentID!!,
                getURL(category, contentID),
                trackPanelistOnly,
                tagHandler.applicationName,
                tagHandler.applicationVersion,
                this,
                userCallbackListener
            )
            log("Request added to the queue")
            dataRequestQueue.add(request)
            runRequestQueue()
        }
        return result
    }

    fun setStateReady() {
        log("Lib is ready for logging...")
        state = READY
        runRequestQueue()
    }

    /**
     * Runs the request queue to send all queued tags
     */
    private fun runRequestQueue() {
        if (state == READY) {
            if (dataRequestQueue.isNotEmpty()) {
                runRequestQueueThread()
            } else {
                log("Request queue is empty. Skip it...")
            }
        } else {
            // Wait for the READY state...
            log("Waiting for the library to get ready...")
        }
    }

    /**
     * Send a tag-request to the server.
     *
     * @param categories  Array of names in category structure. This value will be sent using
     *                    the "cat"-attribute.
     * @param contentID   Value to identify specific content within the category, such as a specific article.
     */
    fun performMetricsRequest(categories: Array<String>?, contentID: String?): Int {
        return performMetricsRequest(generateCategoryString(categories), contentID)
    }

    fun openTwa(){
        trustedWebHandler.open()
    }

    // TagDataRequestCallbackListener overrides

    /**
     * Implementation of callback method from interface TagDataRequestCallbackListener.
     * This method is called when a data request has been completed successfully.
     */
    override fun onDataRequestComplete(request: TagDataRequest) {
        log("RequestCompleted: " + request.cat)
        nbrOfSuccessfulRequests++
    }

    /**
     * Implementation of callback method from interface TagDataRequestCallbackListener.
     * This method is called when a data request has been failed.
     */
    override fun onDataRequestFailed(request: TagDataRequest) {
        error("RequestFailed: " + request.cat)
        nbrOfFailedRequests++
    }

    // Private methods

    /**
     * Check the parameters and return the corresponding error code or success.
     */
    private fun checkRequestParams(category: String?, contentID: String?): Int {
        return when {
            (category == null) -> {
                fatalError("category may not be null")
                TagStringsAndValues.ERROR_CATEGORY_NULL
            }
            (category.length > TagStringsAndValues.MAX_LENGTH_CATEGORY) -> {
                fatalError("category may not have more than ${TagStringsAndValues.MAX_LENGTH_CATEGORY} characters")
                TagStringsAndValues.ERROR_CATEGORY_TOO_LONG
            }
            (contentID == null) -> {
                fatalError("contentID may not be null")
                TagStringsAndValues.ERROR_CONTENT_ID_NULL
            }
            (contentID.length > TagStringsAndValues.MAX_LENGTH_CONTENT_ID) -> {
                fatalError("contentID may not have more than ${TagStringsAndValues.MAX_LENGTH_CONTENT_ID} characters")
                TagStringsAndValues.ERROR_CONTENT_ID_TOO_LONG
            }
            else -> TagStringsAndValues.RESULT_SUCCESS
        }
    }

    /**
     * Append a list of category names to a string to use in the request.
     * Example {News, Sports, Football} will generate News/Sports/Football.
     *
     * @param categories The list of category names.
     * @return The appended category string.
     */
    private fun generateCategoryString(categories: Array<String>?): String {
        var catAppend = ""
        if (categories.isNullOrEmpty()) {
            return catAppend
        }
        catAppend = categories[0]
        for (i in 1 until categories.size) {
            if (categories[i].isNotEmpty()) {
                catAppend += "/" + categories[i]
            }
        }
        return catAppend
    }

    /**
     * Generate a tag-request URL.
     *
     * @param category      The name of category or page to be tagged. This value will be sent using
     *                      the "cat"-attribute.
     * @param contentID     Value to identify specific content within the category, such as a specific article.
     *
     * @return The created URL.
     */
    private fun getURL(category: String, contentID: String): String? {
        return tagHandler.getURL(category, contentID, activityForPref)
    }

    /**
     * Init the server request to the specified URL in a new thread.
     */
    private fun runRequestQueueThread() {
        synchronized(dataRequestQueue) {
            val thread = RequestThread()
            thread.requests.addAll(dataRequestQueue)
            dataRequestQueue.clear()
            threadPool.execute(thread)
        }
    }


    /**
     * A thread to run the request to the server.
     */
    private inner class RequestThread : Runnable {
        var requests: MutableList<TagDataRequest> = mutableListOf()

        override fun run() {
            log("Request queue(${requests.size}): STARTED")
            requests.forEach {
                log("Run request: " + it.cat)
                it.initRequest()
            }
            log("Queue cleared!")
        }
    }

    /**
     * Holds states of the handler
     */
    enum class State {
        PREPARING, READY,
    }

    companion object {
        /**
         * The maximum number of simultaneous request threads.
         * (After some testing it seems like 1 does as good job as many)
         */
        const val MAX_NBR_OF_THREADS = 1
    }

}
