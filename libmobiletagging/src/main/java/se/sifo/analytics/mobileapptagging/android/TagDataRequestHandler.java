/*************************************************
 * TNS SIFO Mobile Application Tagging Framework *
 * (c) Copyright 2012 TNS SIFO, Sweden,          *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

import android.content.Context;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * API for the data requests and URL handling etc. Used by the framework top
 * layer to execute the functions called by the user.
 *
 * @author Jakob Schyberg (jakob.schyberg@wecode.se)
 */
class TagDataRequestHandler implements TagDataRequestCallbackListener {
    /**
     * The maximum number of simultaneous request threads
     * (After some testing it seems like 1 does as good job as many)
     */
    public static final int MAX_NBR_OF_THREADS = 1;

    /**
     * Our TagHandler.
     */
    private TagHandler tagHandler;

    /**
     * Our queue of pending requests.
     */
    private List<TagDataRequest> dataRequestQueue;

    /**
     * Counter keeping track of the number of successful requests.
     */
    private int nbrOfSuccessfulRequests = 0;

    /**
     * Counter keeping track of the number of failed requests.
     */
    private int nbrOfFailedRequests = 0;

    /**
     * The callback-listener specified by the user.
     */
    private TagDataRequestCallbackListener userCallbackListener = null;

    /**
     * The thread handling requests.
     */
    private ExecutorService threadPool;

    /**
     * Create a new handler for specified context and application information.
     *
     * @param c               The context of the application.
     * @param cpId            The customer ID of the application.
     * @param applicationName The name of the application.
     */
    public TagDataRequestHandler(Context c, String cpId, String applicationName, List<HttpCookie> cookies) {
        tagHandler = new TagHandler(c, cpId, applicationName, cookies);
        dataRequestQueue = new ArrayList<TagDataRequest>();
        threadPool = Executors.newScheduledThreadPool(MAX_NBR_OF_THREADS);
    }


    /**
     * Create a new handler for specified context and application information.
     *
     * @param c               The context of the application.
     * @param cpId            The customer ID of the application.
     * @param applicationName The name of the application.
     */
    public TagDataRequestHandler(Context c, String cpId, String applicationName, String panelistKey) {
        tagHandler = new TagHandler(c, cpId, applicationName, panelistKey);
        dataRequestQueue = new ArrayList<>();
        threadPool = Executors.newScheduledThreadPool(MAX_NBR_OF_THREADS);
    }


    public void refreshCookies(Context context, String panelistKey) {
        tagHandler.refresh(context, panelistKey);
    }


    public void refreshCookies(Context context, List<HttpCookie> cookies) {
        tagHandler.refresh(context, cookies);
    }

    /**
     * Send a tag-request to the server.
     *
     * @param category The name of category or page to be tagged. This value will be sent using
     *                 the "cat"-attribute.
     */
    public int performMetricsRequest(String category) {
        return performMetricsRequest(category, "", "");
    }

    /**
     * Send a tag-request to the server.
     *
     * @param category  The name of category or page to be tagged. This value will be sent using
     *                  the "cat"-attribute.
     * @param contentID Value to identify specific content within the category, such as a specific article.
     */
    public int performMetricsRequest(String category, String contentID) {
        return performMetricsRequest(category, contentID, "");
    }

    /**
     * Send a tag-request to the server.
     *
     * @param category    The name of category or page to be tagged. This value will be sent using
     *                    the "cat"-attribute.
     * @param contentID   Value to identify specific content within the category, such as a specific article.
     * @param contentName Name to identify specific content within the category, such as a specific article.
     */
    public int performMetricsRequest(String category, String contentID, String contentName) {
        int result = TagStringsAndValues.RESULT_SUCCESS;

        if (category == null) {
            result = TagStringsAndValues.ERROR_CATEGORY_NULL;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to send tag - category may not be null");
        } else if (category.length() > TagStringsAndValues.MAX_LENGTH_CATEGORY) {
            result = TagStringsAndValues.ERROR_CATEGORY_TOO_LONG;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to send tag - category may not be more than " + TagStringsAndValues.MAX_LENGTH_CATEGORY + " characters");
        } else if (contentID == null) {
            result = TagStringsAndValues.ERROR_CONTENT_ID_NULL;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to send tag - contentID may not be null");
        } else if (contentID.length() > TagStringsAndValues.MAX_LENGTH_CONTENT_ID) {
            result = TagStringsAndValues.ERROR_CONTENT_ID_TOO_LONG;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to send tag - contentID may not be more than " + TagStringsAndValues.MAX_LENGTH_CONTENT_ID + " characters");
        } else if (contentName != null && contentName.length() > TagStringsAndValues.MAX_LENGTH_CONTENT_NAME) {
            result = TagStringsAndValues.ERROR_CONTENT_NAME_TOO_LONG;
            MobileTaggingFrameworkBackend.fatalErrorToLog("Failed to send tag - contentName may not be more than " + TagStringsAndValues.MAX_LENGTH_CONTENT_NAME + " characters");
        }

        if (result == TagStringsAndValues.RESULT_SUCCESS) {
            TagDataRequest request = new TagDataRequest(category, contentID, contentName,
                    getURL(category, contentID, contentName),
                    tagHandler.getApplicationName(),
                    tagHandler.getApplicationVersion(),
                    this, userCallbackListener);
            synchronized (this) {
                dataRequestQueue.add(request);
                runRequest(request);
            }
        }
        return result;
    }

    /**
     * Send a tag-request to the server.
     *
     * @param categories  Array of names in category structure. This value will be sent using
     *                    the "cat"-attribute.
     * @param contentID   Value to identify specific content within the category, such as a specific article.
     * @param contentName Name to identify specific content within the category, such as a specific article.
     */
    public int performMetricsRequest(String[] categories, String contentID, String contentName) {
        return performMetricsRequest(generateCategoryString(categories), contentID, contentName);
    }

    /**
     * Get the queue of pending requests.
     *
     * @return The queue of pending requests.
     */
    public List<TagDataRequest> getDataRequestQueue() {
        return new ArrayList<TagDataRequest>(dataRequestQueue);
    }

    public String getSifoUserCookie() {
        return tagHandler.getPanelistKey();
    }

    /**
     * Generate a tag-request URL.
     *
     * @param category The name of category or page to be tagged. This value will be sent using
     *                 the "cat"-attribute.
     * @return The created URL.
     */
    public String getURL(String category) {
        return getURL(category, "", "");
    }

    /**
     * Generate a tag-request URL.
     *
     * @param category  The name of category or page to be tagged. This value will be sent using
     *                  the "cat"-attribute.
     * @param contentID Value to identify specific content within the category, such as a specific article.
     * @return The created URL.
     */
    public String getURL(String category, String contentID) {
        return getURL(category, contentID, "");
    }

    /**
     * Generate a tag-request URL.
     *
     * @param category    The name of category or page to be tagged. This value will be sent using
     *                    the "cat"-attribute.
     * @param contentID   Value to identify specific content within the category, such as a specific article.
     * @param contentName Name to identify specific content within the category, such as a specific article.
     * @return The created URL.
     */
    public String getURL(String category, String contentID, String contentName) {
        return tagHandler.getURL(category, contentID, contentName);
    }

    /**
     * Generate a tag-request URL.
     *
     * @param categories  Array of names in category structure. This value will be sent using
     *                    the "cat"-attribute.
     * @param contentID   Value to identify specific content within the category, such as a specific article.
     * @param contentName Name to identify specific content within the category, such as a specific article.
     * @return The created URL.
     */
    public String getURL(String[] categories, String contentID, String contentName) {
        return getURL(generateCategoryString(categories), contentID, contentName);
    }

    /**
     * Get the number of successful request since the handler was instantiated.
     *
     * @return The number of successful requests.
     */
    public int getNbrOfSuccessfulRequests() {
        return nbrOfSuccessfulRequests;
    }

    /**
     * Get the number of failed requests since the handler was instantiated.
     *
     * @return The number of failed requests.
     */
    public int getNbrOfFailedRequests() {
        return nbrOfFailedRequests;
    }

    /**
     * Set an extra callback-listener to get notified when a request succeeds or fails.
     *
     * @param callbackListener
     */
    public void setCallbackListener(TagDataRequestCallbackListener callbackListener) {
        userCallbackListener = callbackListener;
    }

    /**
     * Implementation of callback method from interface TagDataRequestCallbackListener.
     * This method is called when a data request has been completed successfully.
     */
    public void onDataRequestComplete(TagDataRequest request) {
        synchronized (this) {
            dataRequestQueue.remove(request);
        }
        nbrOfSuccessfulRequests++;
    }

    /**
     * Implementation of callback method from interface TagDataRequestCallbackListener.
     * This method is called when a data request has been failed.
     */
    public void onDataRequestFailed(TagDataRequest request) {
        synchronized (this) {
            dataRequestQueue.remove(request);
        }
        nbrOfFailedRequests++;
    }

    /**
     * Append a list of category names to a string to use in the request.
     * Example {News, Sports, Football} will generate News/Sports/Football.
     *
     * @param categories The list of category names.
     * @return The appended category string.
     */
    private String generateCategoryString(String[] categories) {
        String catAppend = "";
        if (categories.length > 0) {
            catAppend = categories[0];

            // Append category names with the correct separator
            for (int i = 1; i < categories.length; i++) {
                if (categories[i] != null && categories[i].length() > 0) {
                    catAppend += "/" + categories[i];
                }
            }
        }
        return catAppend;
    }

    /**
     * Init the server request to the specified URL.
     */
    private void runRequest(TagDataRequest req) {
        // Specify a new Thread for the request
        RequestThread thread = new RequestThread();
        thread.request = req;
        threadPool.execute(thread);
    }

    /**
     * A thread to run the request to the server.
     */
    private class RequestThread implements Runnable {
        public TagDataRequest request;

        public void run() {
            request.initRequest();
        }
    }

}
