/*************************************************
 * TNS SIFO Mobile Application Tagging Framework *
 * (c) Copyright 2012 TNS SIFO, Sweden,          *
 * All rights reserved.                          *
 *************************************************/

package se.sifo.analytics.mobileapptagging.android;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;
import java.util.UUID;

/**
 * TNS SIFO Mobile Application Tagging Framework :
 * Debugging/Advanced features : TagDataRequest.java :
 * <p/>
 * This class describes an object with information about a measure-tag request to the Mobiletech server.
 *
 * @author Jakob Schyberg (jakob.schyberg@wecode.se)
 */
public class TagDataRequest {
    /**
     * Get the category value (cat) stored in the request.
     *
     * @return The String holding the category value.
     */
    public String getCat() {
        return cat;
    }

    /**
     * Get the identifier value (id) stored in the request.
     *
     * @return The String holding the identifier value.
     */
    public String getID() {
        return id;
    }

    /**
     * Get the entire URL used to execute this request.
     *
     * @return The String holding the URL.
     */
    public String getURL() {
        return url;
    }

    /**
     * Get the content name parameter (name) stored in the request.
     *
     * @return The String holding the identifier value.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Unique Identifier of this specific request. Can be used for tracking etc.
     *
     * @return The UUID holding the unique identifier.
     */
    public UUID getRequestID() {
        return requestID;
    }

    /**
     * Ask this request to abort. Not guaranteed.
     */
    public void cancel() {
        if (httpRequest != null) {
            httpRequest.abort();
        }
    }

    /**
     * Get the HTTP status code for the request.
     *
     * @return The status code, 0 if request is still pending or if it failed with an exception.
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /** End of public methods **/

    /**
     * The Unique ID of this specific request, can used for tracking etc.
     */
    private UUID requestID;

    /**
     * The catalog value (cat) to be sent in this request.
     */
    private String cat;

    /**
     * The identifier of content (id/contentID) to be sent in this request.
     */
    private String id;

    /**
     * The name of content (name/contentName) to be sent in this request.
     */
    private String name;

    /**
     * The url that this request is calling.
     */
    private String url;

    /**
     * The Http connection.
     */
    private HttpGet httpRequest = null;

    /**
     * The callback listener to be called when request is finished.
     */
    private TagDataRequestCallbackListener callbackListener;

    /**
     * An extra callback listener specified by the user to be called when request is finished.
     */
    private TagDataRequestCallbackListener userDefinedCallbackListener = null;

    /**
     * The HTTP response code.
     */
    private int httpStatusCode = 0;

    /**
     * The name of the application created from initialize
     */
    private String applicationName;

    /**
     * The version name found set in the AndroidManifest.Xml
     */
    private String applicationVersion;

    private CookieStore cookieStore;

    /**
     * Constructor, used internally by framework only.
     *
     * @param cat      The catalog value (cat) to be sent in this request.
     * @param ref      The reference value (ref) to be sent in this request.
     * @param id       The identifier value (id) to be sent in this request.
     * @param url      The url that this request is calling.
     * @param callback The callback listener to be called when request is finished.
     */
    TagDataRequest(String cat, String ref, String id, String name, String url, String applicatonName, String applicationVersion, CookieStore cookieStore, TagDataRequestCallbackListener callback) {
        this(cat, id, name, url, applicatonName, applicationVersion, cookieStore, callback, null);
    }

    /**
     * Constructor, used internally by framework only.
     *
     * @param cat                  The catalog value (cat) to be sent in this request.
     * @param id                   The identifier value (id) to be sent in this request.
     * @param url                  The url that this request is calling.
     * @param userCallbackListener The callbacklistener defined by user.
     */
    TagDataRequest(String cat, String id, String name, String url, String applicationName, String applicationVersion, CookieStore cookieStore, TagDataRequestCallbackListener callback, TagDataRequestCallbackListener userCallbackListener) {
        this.cat = cat;
        this.id = id;
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
        this.url = url;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.cookieStore = cookieStore;

        requestID = UUID.randomUUID();
        callbackListener = callback;
        userDefinedCallbackListener = userCallbackListener;
    }

    /**
     * Init the server request to the specified URL. This function will start a new Thread.
     */
    void initRequest() {
        // HTTP-connection setup
        DefaultHttpClient client = new DefaultHttpClient();

        if (url != null && url.length() > 0) {
            try {
                String userAgent = applicationName + "/" + applicationVersion + " " + System.getProperty("http.agent");
                URI uriMetrics = new URI(url);
                httpRequest = new HttpGet(uriMetrics);

                //Sets a custom user-agent for HTTP requests
                httpRequest.addHeader("User-Agent", userAgent);
                httpRequest.addHeader("Cookie", CookieHandler.getCookieString(cookieStore.getCookies()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // HTTP-request execution
        if (httpRequest != null) {
            HttpResponse response = null;
            try {
                MobileTaggingFrameworkBackend.printToLog(
                        "Tag request sent: " +
                                "\nRequestID: " + getRequestID() +
                                "\nCat encoded value:" + TagHandler.urlEncode(cat) +
                                "\nCat plain value: " + cat +
                                "\nId: " + id +
                                "\nName:" + name +
                                "\nURL:\n" + url);
                // Execute HTTP-request
                response = client.execute(httpRequest);
                if (response != null) {

                    if ((httpStatusCode = response.getStatusLine().getStatusCode()) == 200) {
                        // Request was successful with code 200
                        dataRequestComplete();
                    } else {
                        // Request finished with failure code
                        dataRequestFail(response, null);
                    }
                } else {
                    // Response was null, should not happen
                    MobileTaggingFrameworkBackend.errorToLog("Tag request response null");
                }
            } catch (Exception e) {
                // Request failed with exception
                dataRequestFail(response, e);
            }
        }
    }

    /**
     * Handle a failed request.
     *
     * @param response The response from the server.
     * @param e        The exception if one was thrown.
     */
    private void dataRequestFail(HttpResponse response, Exception e) {
        if (response != null) {
            MobileTaggingFrameworkBackend.errorToLog("Tag request failed with http status code:" + response.getStatusLine().getStatusCode() + "\nRequestID: " + getRequestID());
        } else {
            MobileTaggingFrameworkBackend.errorToLog("Tag request failed with exception:" + "\n" + e.toString() + "\nRequestID: " + getRequestID());
        }

        // Tell our callback-listeners that the request failed
        callbackListener.onDataRequestFailed(this);
        if (userDefinedCallbackListener != null) {
            userDefinedCallbackListener.onDataRequestFailed(this);
        }
    }

    /**
     * Handle a successful request.
     */
    private void dataRequestComplete() {
        MobileTaggingFrameworkBackend.printToLog("Tag request completed with success: " + "\nRequestID: " + getRequestID());

        // Tell our callback listeners that the request was successful
        callbackListener.onDataRequestComplete(this);
        if (userDefinedCallbackListener != null) {
            userDefinedCallbackListener.onDataRequestComplete(this);
        }
    }

}
