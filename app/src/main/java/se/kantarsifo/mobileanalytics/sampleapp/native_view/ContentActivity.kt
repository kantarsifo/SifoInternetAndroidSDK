package se.kantarsifo.mobileanalytics.sampleapp.native_view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_content.*
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.sampleapp.R
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.util.TagInfo

class ContentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        init()
    }

    override fun onBackPressed() {
        tryToFinishAffinity()
    }

    // Private methods

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        list.setOnItemClickListener { parent, _, position, _ ->
            sendTag(parent.getItemAtPosition(position) as String)
        }
    }

    private fun sendTag(content: String) {
        if (TSMobileAnalytics.instance != null) {
            TagInfo.contentId = content
            TSMobileAnalytics.instance?.sendTag(
                    categories = TagInfo.getCategories(),
                    contentID = TagInfo.contentId)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ContentActivity::class.java)
            context.startActivity(intent)
        }
    }

}
