package com.example.playlistmaker2

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit



class App: Application() {

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        val sharedPrefs = getSharedPreferences(Constants.SETTINGS_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(Constants.DARK_THEME_KEY, false)

        switchTheme(darkTheme)
    }
    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        getSharedPreferences(Constants.SETTINGS_PREFERENCES, MODE_PRIVATE).edit {
            putBoolean(Constants.DARK_THEME_KEY, darkThemeEnabled)
        }

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

}
