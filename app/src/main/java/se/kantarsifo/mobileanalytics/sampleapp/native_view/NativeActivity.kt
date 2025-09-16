package se.kantarsifo.mobileanalytics.sampleapp.native_view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import se.kantarsifo.mobileanalytics.framework.TSMobileAnalytics
import se.kantarsifo.mobileanalytics.sampleapp.base.BaseActivity
import se.kantarsifo.mobileanalytics.sampleapp.databinding.ActivityNativeBinding
import se.kantarsifo.mobileanalytics.sampleapp.util.TagInfo

class NativeActivity : BaseActivity() {

    lateinit var binding: ActivityNativeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    // Private methods

    private fun init() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val items = mutableListOf<String>()
        for (i in 0 until 30) {
            items.add("Category_$i")
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
        binding.list.adapter = adapter
        binding.list.setOnItemClickListener { parent, _, position, _ ->
            sendTag(parent.getItemAtPosition(position) as String)
        }
    }

    private fun sendTag(category: String) {
        if (TSMobileAnalytics.instance != null) {
            TagInfo.setCategory(0, category)
            TSMobileAnalytics.instance?.sendTag(category = category)
            CategoryActivity.start(this)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NativeActivity::class.java)
            context.startActivity(intent)
        }
    }

}
