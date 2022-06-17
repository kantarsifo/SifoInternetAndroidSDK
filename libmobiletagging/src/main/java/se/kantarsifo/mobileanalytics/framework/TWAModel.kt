package se.kantarsifo.mobileanalytics.framework

data class TWAModel(
    val url:String = "",
    val extraParams:HashMap<String,Any> = hashMapOf()
)