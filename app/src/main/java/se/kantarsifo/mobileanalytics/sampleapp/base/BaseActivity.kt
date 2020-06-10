package se.kantarsifo.mobileanalytics.sampleapp.base

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.framework.TagDataRequest
import se.kantarsifo.mobileanalytics.framework.TagDataRequestCallbackListener
import se.kantarsifo.mobileanalytics.sampleapp.util.Constants

abstract class BaseActivity : AppCompatActivity(), TagDataRequestCallbackListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TSMobileAnalytics.instance?.setCallbackListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TagDataRequestCallbackListener overrides

    override fun onDataRequestComplete(request: TagDataRequest) {
        runOnUiThread {
            showToast("Request successful -> \"${getRequestMainInfo(request)}\"")
            Log.d(Constants.LOG_TAG, request.uRL ?: "")
            Log.d(Constants.LOG_TAG, "Data request completed with success:" +
                    "\nCode: " + request.httpStatusCode +
                    "\nRequest ID: " + request.requestID +
                    "\nCat: " + request.cat +
                    "\nId: " + request.iD)
            Log.d(Constants.LOG_TAG, "Number of successful requests: " +
                    TSMobileAnalytics.instance?.nbrOfSuccessfulRequests)
            Log.d(Constants.LOG_TAG, "***********************************")
            Log.d(Constants.LOG_TAG, "Request queue size: " + TSMobileAnalytics.instance?.requestQueue?.size)
        }
    }

    override fun onDataRequestFailed(request: TagDataRequest) {
        runOnUiThread {
            showToast("Request failed -> \"${getRequestMainInfo(request)}\"")
            Log.w(Constants.LOG_TAG, request.uRL ?: "")
            Log.w(Constants.LOG_TAG, "Data request completed with failure:" +
                    "\nCode: " + request.httpStatusCode +
                    "\nRequest ID: " + request.requestID +
                    "\nCat: " + request.cat +
                    "\nId: " + request.iD)
            Log.w(Constants.LOG_TAG, "Number of successful requests: "
                    + TSMobileAnalytics.instance?.nbrOfSuccessfulRequests)
            Log.w(Constants.LOG_TAG, "Number of failed requests: "
                    + TSMobileAnalytics.instance?.nbrOfFailedRequests)
            Log.w(Constants.LOG_TAG, "***********************************")
            Log.w(Constants.LOG_TAG, "Request queue size: " + TSMobileAnalytics.instance?.requestQueue?.size)
        }
    }

    // Protected methods

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun tryToFinishAffinity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity()
        } else {
            finish()
        }
    }

    // Private methods

    private fun getRequestMainInfo(request: TagDataRequest): String {
        return when {
            request.iD.isNotEmpty() -> request.iD
            else -> request.cat
        }
    }

}
