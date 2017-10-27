/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 *************************************************/

package se.kantarsifo.mobileanalytics.framework;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
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

    /**
     * Constructor, used internally by framework only.
     *
     * @param cat      The catalog value (cat) to be sent in this request.
     * @param ref      The reference value (ref) to be sent in this request.
     * @param id       The identifier value (id) to be sent in this request.
     * @param url      The url that this request is calling.
     * @param callback The callback listener to be called when request is finished.
     */
    TagDataRequest(String cat, String ref, String id, String name, String url, String applicatonName, String applicationVersion, TagDataRequestCallbackListener callback) {
        this(cat, id, name, url, applicatonName, applicationVersion, callback, null);
    }

    /**
     * Constructor, used internally by framework only.
     *
     * @param cat                  The catalog value (cat) to be sent in this request.
     * @param id                   The identifier value (id) to be sent in this request.
     * @param url                  The url that this request is calling.
     * @param userCallbackListener The callbacklistener defined by user.
     */
    TagDataRequest(String cat, String id, String name, String url, String applicationName, String applicationVersion, TagDataRequestCallbackListener callback, TagDataRequestCallbackListener userCallbackListener) {
        this.cat = cat;
        this.id = id;
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
        this.url = url;
        this.applicationName = applicationName;
        Log.v("applicationVersion", "applicationVersion :" + applicationVersion);
        this.applicationVersion = applicationVersion;

        requestID = UUID.randomUUID();
        callbackListener = callback;
        userDefinedCallbackListener = userCallbackListener;
    }

    /**
     * Init the server request to the specified URL. This function will start a new Thread.
     */
    void initRequest() {
        Log.v("cookieStore", "cookieStore :" + SifoCookieManager.getInstance().getCookieStore().getCookies().toString());

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dataRequestFail(null, error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String userAgent = applicationName + "/" + applicationVersion +  " " + "session_id=" + "sdk_android_" + TSMobileAnalytics.getInstance().getLibraryVersion() + " " + System.getProperty("http.agent");
                String cookieHandlerString = CookieHandler.getCookieString(SifoCookieManager.getInstance().getCookieStore().getCookies());
                Map<String, String> params = new HashMap<>();
                params.put("User-Agent", userAgent);
                params.put("Cookie", cookieHandlerString);
                Log.v("cookieStore", "cookieHandlerString :" + cookieHandlerString);

                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    if ((httpStatusCode = response.statusCode) == 200) {
                        // Request was successful with code 200
                        TSMobileAnalyticsBackend.printToLog(
                                "Tag request sent: " +
                                        "\nRequestID: " + getRequestID() +
                                        "\nCat encoded value:" + TagHandler.urlEncode(cat) +
                                        "\nCat plain value: " + cat +
                                        "\nId: " + id +
                                        "\nName:" + name +
                                        "\nURL:\n" + url);

                        dataRequestComplete();
                    } else {
                        // Request finished with failure code
                        dataRequestFail(response, null);
                    }
                } else {
                    // Response was null, should not happen
                    TSMobileAnalyticsBackend.errorToLog("Tag request response null");
                }
                return super.parseNetworkResponse(response);
            }
        };
        VolleyManager.getInstance().getRequestQueue().add(postRequest);

    }

    /**
     * Handle a failed request.
     *
     * @param response The response from the server.
     * @param e        The exception if one was thrown.
     */
    private void dataRequestFail(NetworkResponse response, Exception e) {
        if (response != null) {
            TSMobileAnalyticsBackend.errorToLog("Tag request failed with http status code:" + response.statusCode + "\nRequestID: " + getRequestID());
        } else {
            TSMobileAnalyticsBackend.errorToLog("Tag request failed with exception:" + "\n" + e.toString() + "\nRequestID: " + getRequestID());
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
        TSMobileAnalyticsBackend.printToLog("Tag request completed with success: " + "\nRequestID: " + getRequestID());

        // Tell our callback listeners that the request was successful
        callbackListener.onDataRequestComplete(this);
        if (userDefinedCallbackListener != null) {
            userDefinedCallbackListener.onDataRequestComplete(this);
        }
    }

}
