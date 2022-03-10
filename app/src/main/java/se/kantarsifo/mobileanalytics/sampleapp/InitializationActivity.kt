package se.kantarsifo.mobileanalytics.sampleapp

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.trusted.TrustedWebActivityIntentBuilder
import com.google.androidbrowserhelper.trusted.TwaLauncher
import kotlinx.android.synthetic.main.activity_initialization.*
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.framework.TWAModel
import se.kantarsifo.mobileanalytics.framework.TagStringsAndValues
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.native_view.NativeActivity
import se.kantarsifo.mobileanalytics.sampleapp.util.Constants
import se.kantarsifo.mobileanalytics.sampleapp.util.PublicSharedPreferences
import se.kantarsifo.mobileanalytics.sampleapp.util.afterTextChanged
import se.kantarsifo.mobileanalytics.sampleapp.util.onCheckedChanged
import se.kantarsifo.mobileanalytics.sampleapp.web_view.WebViewActivity

class InitializationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initialization)
        init()
    }

    override fun onResume() {
        super.onResume()
        setSuccessAndFailureTexts()
    }

    // Private methods

    private fun init() {
        initEditTexts()
        initCheckBoxes()
        initButtons()
        setDefaultData()
        getPreferenceSetup()
    }

    private fun initEditTexts() {
        cpIdET.afterTextChanged { destroyCurrentFramework() }
        appNameET.afterTextChanged { destroyCurrentFramework() }
    }

    private fun initCheckBoxes() {
        panelistOnly.onCheckedChanged { destroyCurrentFramework() }
        logEnabled.onCheckedChanged { destroyCurrentFramework() }
        isWebViewBased.onCheckedChanged { destroyCurrentFramework() }
    }

    private fun initButtons() {
        updateInitButtonColor()
        initialize_button.setOnClickListener {
            onInitFrameworkClicked()
        }
        destroy_button.setOnClickListener { destroyCurrentFramework() }
        btn_webview.setOnClickListener { onWebViewClicked() }
        btn_native.setOnClickListener { onNativeClicked() }
        twa_native.setOnClickListener {  onTWAClicked() }
    }

    private fun onInitFrameworkClicked() {
        if (paramsAreValid()) {
            initializeFrameworkWithBuilder()
            setPreferenceSetup()
            Handler().postDelayed({
                updateInitButtonColor()
            }, 1000)
        }
    }

    private fun initializeFrameworkWithBuilder() {
       TSMobileAnalytics.createInstance(
                this,
                TSMobileAnalytics.Builder()
                        .setCpId(cpIdET.text.toString())
                        .setApplicationName(appNameET.text.toString())
                        .setPanelistTrackingOnly(panelistOnly.isChecked)
                        .setIsWebViewBased(isWebViewBased.isChecked)
                        .setLogPrintsActivated(logEnabled.isChecked)
                        .setTWAInfo(TWAModel(url = "https://codigoanalytics.azurewebsites.net/test/GetLatestData").apply {
                            extraParams.apply {
                               put("customCustomerParam","foo")
                            }
                        })
                        .build()
        )
    }

    private fun updateInitButtonColor() {
        if (TSMobileAnalytics.instance != null) {
            initialize_button.background.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY)
        } else {
            initialize_button.background.clearColorFilter()
        }
    }

    private fun paramsAreValid(): Boolean {
        val cpId = cpIdET.text.toString()
        val appName = appNameET.text.toString()
        return if (cpId.isEmpty() || appName.isEmpty()) {
            showToast("cpId and application name cannot be empty")
            false
        } else if (cpId.length != TagStringsAndValues.CPID_LENGTH_CODIGO) {
            showToast("cpId must have ${TagStringsAndValues.CPID_LENGTH_CODIGO} characters")
            false
        } else {
            true
        }
    }

    private fun destroyCurrentFramework() {
        TSMobileAnalytics.destroyFramework()
        updateInitButtonColor()
        resetSuccessAndFailureTexts()
    }

    private fun onWebViewClicked() {
        if (isFrameworkInitialized()) {
            WebViewActivity.start(this)
        }
    }

    private fun onTWAClicked() {
        if (isFrameworkInitialized()) {
           start(this)
        }
    }

    fun start(context: Context) {

        TSMobileAnalytics.instance?.openTwa()

    }


    private fun onNativeClicked() {
        if (isFrameworkInitialized()) {
            NativeActivity.start(this)
        }
    }

    private fun isFrameworkInitialized(): Boolean {
        return if (TSMobileAnalytics.instance != null) {
            true
        } else {
            showToast("Framework must be initialized")
            false
        }
    }

    private fun setDefaultData() {
        setCpIdOnEditText(Constants.CODIGO_CPID)
        setAppNameOnEditText(getString(R.string.default_app_name))
    }

    private fun setSuccessAndFailureTexts() {
        TSMobileAnalytics.instance?.let {
            success_request.text = getString(R.string.success_requests, it.nbrOfSuccessfulRequests)
            fail_request.text = getString(R.string.failed_requests, it.nbrOfFailedRequests)
        }
    }

    private fun resetSuccessAndFailureTexts() {
        success_request.text = getString(R.string.success_requests, 0)
        fail_request.text = getString(R.string.failed_requests, 0)
    }

    private fun getPreferenceSetup() {
        PublicSharedPreferences.getDefaults(Constants.CPID_PREFERENCE, this)?.let { setCpIdOnEditText(it) }
        PublicSharedPreferences.getDefaults(Constants.APP_NAME_PREFERENCE, this)?.let { setAppNameOnEditText(it) }
        panelistOnly.isChecked = PublicSharedPreferences.getBoolean(Constants.PANELIST_TRACKING_ONLY_PREFERENCE, this)
        logEnabled.isChecked = PublicSharedPreferences.getBoolean(Constants.LOG_ENABLED_PREFERENCE, this)
        isWebViewBased.isChecked = PublicSharedPreferences.getBoolean(Constants.IS_WEB_VIEW_BASED_PREFERENCE, this)
    }

    private fun setPreferenceSetup() {
        PublicSharedPreferences.setDefaults(Constants.CPID_PREFERENCE, cpIdET.text.toString(), this)
        PublicSharedPreferences.setDefaults(Constants.APP_NAME_PREFERENCE, appNameET.text.toString(), this)
        PublicSharedPreferences.setBool(Constants.LOG_ENABLED_PREFERENCE, logEnabled.isChecked, this)
        PublicSharedPreferences.setBool(Constants.PANELIST_TRACKING_ONLY_PREFERENCE, panelistOnly.isChecked, this)
        PublicSharedPreferences.setBool(Constants.IS_WEB_VIEW_BASED_PREFERENCE, isWebViewBased.isChecked, this)
    }

    private fun setCpIdOnEditText(cpId: String) {
        cpIdET.setText(cpId)
        cpIdET.setSelection(cpId.length)
    }

    private fun setAppNameOnEditText(appName: String) {
        appNameET.setText(appName)
        appNameET.setSelection(appName.length)
    }

}

private operator fun <I> ActivityResultLauncher<I>.invoke(intent: I) {
    launch(intent)
}

