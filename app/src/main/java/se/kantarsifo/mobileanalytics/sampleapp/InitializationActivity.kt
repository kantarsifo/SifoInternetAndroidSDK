package se.kantarsifo.mobileanalytics.sampleapp

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import androidx.activity.result.ActivityResultLauncher
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.framework.TWAModel
import se.kantarsifo.mobileanalytics.framework.TagStringsAndValues
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.databinding.ActivityInitializationBinding
import se.kantarsifo.mobileanalytics.sampleapp.native_view.NativeActivity
import se.kantarsifo.mobileanalytics.sampleapp.util.Constants
import se.kantarsifo.mobileanalytics.sampleapp.util.PublicSharedPreferences
import se.kantarsifo.mobileanalytics.sampleapp.util.afterTextChanged
import se.kantarsifo.mobileanalytics.sampleapp.util.onCheckedChanged
import se.kantarsifo.mobileanalytics.sampleapp.web_view.WebViewActivity

class InitializationActivity : BaseActivity() {

    private lateinit var binding: ActivityInitializationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitializationBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.cpIdET.afterTextChanged { destroyCurrentFramework() }
        binding.appNameET.afterTextChanged { destroyCurrentFramework() }
    }

    private fun initCheckBoxes() {
        binding.panelistOnly.onCheckedChanged { destroyCurrentFramework() }
        binding.logEnabled.onCheckedChanged { destroyCurrentFramework() }
        binding.isWebViewBased.onCheckedChanged { destroyCurrentFramework() }
    }

    private fun initButtons() {
        updateInitButtonColor()
        binding.initializeButton.setOnClickListener {
            onInitFrameworkClicked()
        }
        binding.destroyButton.setOnClickListener { destroyCurrentFramework() }
        binding.btnWebview.setOnClickListener { onWebViewClicked() }
        binding.btnNative.setOnClickListener { onNativeClicked() }
        binding.twaNative.setOnClickListener {  onTWAClicked() }
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
                        .setCpId(binding.cpIdET.text.toString())
                        .setApplicationName(binding.appNameET.text.toString())
                        .setPanelistTrackingOnly(binding.panelistOnly.isChecked)
                        .setIsWebViewBased(binding.isWebViewBased.isChecked)
                        .setLogPrintsActivated(binding.logEnabled.isChecked)
                        .setTWAInfo(TWAModel(url = "https://www.mediafacts.se/").apply {
                            extraParams.apply {
                               put("customCustomerParam","foo")
                            }
                        })
                        .build()
        )
    }

    private fun updateInitButtonColor() {
        if (TSMobileAnalytics.instance != null) {
            binding.initializeButton.background.colorFilter = PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY)
        } else {
            binding.initializeButton.background.clearColorFilter()
        }
    }

    private fun paramsAreValid(): Boolean {
        val cpId = binding.cpIdET.text.toString()
        val appName = binding.appNameET.text.toString()
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
            TSMobileAnalytics.instance?.openTwa(activity = this)
        }
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
            binding.successRequest.text = getString(R.string.success_requests, it.nbrOfSuccessfulRequests)
            binding.failRequest.text = getString(R.string.failed_requests, it.nbrOfFailedRequests)
        }
    }

    private fun resetSuccessAndFailureTexts() {
        binding.successRequest.text = getString(R.string.success_requests, 0)
        binding.failRequest.text = getString(R.string.failed_requests, 0)
    }

    private fun getPreferenceSetup() {
        PublicSharedPreferences.getDefaults(Constants.CPID_PREFERENCE, this)?.let { setCpIdOnEditText(it) }
        PublicSharedPreferences.getDefaults(Constants.APP_NAME_PREFERENCE, this)?.let { setAppNameOnEditText(it) }
        binding.panelistOnly.isChecked = PublicSharedPreferences.getBoolean(Constants.PANELIST_TRACKING_ONLY_PREFERENCE, this)
        binding.logEnabled.isChecked = PublicSharedPreferences.getBoolean(Constants.LOG_ENABLED_PREFERENCE, this)
        binding.isWebViewBased.isChecked = PublicSharedPreferences.getBoolean(Constants.IS_WEB_VIEW_BASED_PREFERENCE, this)
    }

    private fun setPreferenceSetup() {
        PublicSharedPreferences.setDefaults(Constants.CPID_PREFERENCE, binding.cpIdET.text.toString(), this)
        PublicSharedPreferences.setDefaults(Constants.APP_NAME_PREFERENCE, binding.appNameET.text.toString(), this)
        PublicSharedPreferences.setBool(Constants.LOG_ENABLED_PREFERENCE, binding.logEnabled.isChecked, this)
        PublicSharedPreferences.setBool(Constants.PANELIST_TRACKING_ONLY_PREFERENCE, binding.panelistOnly.isChecked, this)
        PublicSharedPreferences.setBool(Constants.IS_WEB_VIEW_BASED_PREFERENCE, binding.isWebViewBased.isChecked, this)
    }

    private fun setCpIdOnEditText(cpId: String) {
        binding.cpIdET.setText(cpId)
        binding.cpIdET.setSelection(cpId.length)
    }

    private fun setAppNameOnEditText(appName: String) {
        binding.appNameET.setText(appName)
        binding.appNameET.setSelection(appName.length)
    }

}

private operator fun <I> ActivityResultLauncher<I>.invoke(intent: I) {
    launch(intent)
}

