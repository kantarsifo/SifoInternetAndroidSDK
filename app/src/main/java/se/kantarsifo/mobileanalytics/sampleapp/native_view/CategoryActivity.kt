package se.kantarsifo.mobileanalytics.sampleapp.native_view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.databinding.ActivityCategoryBinding
import se.kantarsifo.mobileanalytics.sampleapp.util.TagInfo

class CategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onBackPressed() {
        tryToFinishAffinity()
    }

    // Private methods

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.list.setOnItemClickListener { parent, _, position, _ ->
            sendTag(parent.getItemAtPosition(position) as String)
        }
    }

    private fun sendTag(category: String) {
        if (TSMobileAnalytics.instance != null) {
            TagInfo.setCategory(1, category)
            TSMobileAnalytics.instance?.sendTag(
                    categories =  TagInfo.getCategories(),
                    contentID = ""
            )
            ContentActivity.start(this)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CategoryActivity::class.java)
            context.startActivity(intent)
        }
    }

}
