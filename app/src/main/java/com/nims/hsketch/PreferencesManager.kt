package com.nims.hsketch

import android.content.Context
import android.content.SharedPreferences

open class PreferencesManager {
    private val  prefName = "Normal"
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    open fun setBoolean(context: Context, key: String, value: Boolean){
        val editer = getSharedPreferences(context).edit()
        editer.putBoolean(key, value)
        editer.apply()
    }

    open fun getBoolean(context: Context, key: String): Boolean{
        val value = getSharedPreferences(context).getBoolean(key, false)
        return value
    }

}