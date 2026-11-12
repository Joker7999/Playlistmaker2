package com.example.playlistmaker2.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.playlistmaker2.Constants
import com.example.playlistmaker2.domain.api.SettingsRepository

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val sharedPreferences = context.getSharedPreferences(
        Constants.SETTINGS_PREFERENCES,
        Context.MODE_PRIVATE
    )

    override fun getDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(Constants.DARK_THEME_KEY, false)
    }

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(Constants.DARK_THEME_KEY, enabled)
        }
    }
}