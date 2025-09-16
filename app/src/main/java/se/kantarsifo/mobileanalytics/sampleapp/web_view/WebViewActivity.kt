package se.kantarsifo.mobileanalytics.sampleapp.web_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.databinding.ActivityWebBinding

class WebViewActivity : BaseActivity() {

    lateinit var binding: ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TSMobileAnalytics.instance?.activateCookies(binding.webview)
        init()
    }

    // Private methods

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initList()
        initWebView()
    }

    private fun initList() {
        binding.list.setOnItemClickListener { parent, _, position, _ ->
            val url = parent.getItemAtPosition(position) as String
            binding.webview.loadUrl(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webview.webViewClient = WebViewClient()
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.javaScriptCanOpenWindowsAutomatically = true
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, WebViewActivity::class.java)
            context.startActivity(intent)
        }
    }

}
