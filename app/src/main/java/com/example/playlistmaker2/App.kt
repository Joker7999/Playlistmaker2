package com.example.playlistmaker2

import android.app.Application import androidx.appcompat.app.AppCompatDelegate import com.example.playlistmaker2.creator.Creator

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Creator.initApplication(this)

        val darkTheme = Creator.provideSettingsInteractor().getDarkTheme()
        applyTheme(darkTheme)
    }

    fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
