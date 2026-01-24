package com.example.talesandtruths

import android.content.Context

object TextPref {

    private const val PREF = "text_pref"
    private const val KEY_SIZE = "text_size"

    fun save(context: Context, size: Float) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SIZE, size)
            .apply()
    }

    fun get(context: Context): Float {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getFloat(KEY_SIZE, TextSizeConfig.MEDIUM)
    }
}
