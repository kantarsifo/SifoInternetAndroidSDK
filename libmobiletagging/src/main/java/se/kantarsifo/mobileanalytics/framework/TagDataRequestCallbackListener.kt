/*************************************************
 * Kantar Sifo Mobile Analytics Framework        *
 * (c) Copyright 2017 Kantar Sifo AB, Sweden,    *
 * All rights reserved.                          *
 */
package se.kantarsifo.mobileanalytics.framework

/**
 * TNS SIFO Mobile Application Tagging Framework :
 * Debugging/Advanced features : TagDataRequestCallbackListener.java :
 *
 * This interface is used to receive callbacks from the Tagging framework to know if server requests
 * are succeeded or failed. The information can be used to track errors, handle errors etc.
 *
 */
interface TagDataRequestCallbackListener {

    /**
     * This method is called when a tag has been successfully sent to server
     * and the server has responded with result code 200.
     * @param request An object with information about the request.
     */
    fun onDataRequestComplete(request: TagDataRequest)

    /**
     * This method is called when a tag request has failed.
     * No new attempt will be made and it will not be received by the server.
     * @param request An object with information about the request.
     */
    fun onDataRequestFailed(request: TagDataRequest)

}
