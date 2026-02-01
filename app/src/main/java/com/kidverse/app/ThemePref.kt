package com.kidverse.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePref {

    private const val PREF = "theme_pref"
    private const val KEY_THEME = "theme_mode"

    fun save(context: Context, mode: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME, mode)
            .apply()
    }

    fun get(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
