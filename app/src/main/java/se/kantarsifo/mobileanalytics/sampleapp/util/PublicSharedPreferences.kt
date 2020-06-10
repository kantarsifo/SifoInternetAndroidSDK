package se.kantarsifo.mobileanalytics.sampleapp.util

import android.content.Context
import androidx.preference.PreferenceManager

object PublicSharedPreferences {

    @JvmStatic
    fun setDefaults(key: String, value: String, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    @JvmStatic
    fun setBool(key: String, value: Boolean, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    @JvmStatic
    fun getDefaults(key: String, context: Context): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, null)
    }

    @JvmStatic
    fun getBoolean(key: String, context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, false)
    }

}
