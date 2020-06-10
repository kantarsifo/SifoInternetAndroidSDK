package se.kantarsifo.mobileanalytics.sampleapp.util

object TagInfo {

    var contentId: String? = null
    private val categories = arrayOfNulls<String>(2)

    fun setCategory(index: Int, category: String) {
        categories[index] = category
    }

    fun getCategories(): Array<String> {
        return categories.filterNotNull().toTypedArray()
    }

}