package com.example.playlistmaker2.domain.impl

import com.example.playlistmaker2.domain.api.SettingsInteractor
import com.example.playlistmaker2.domain.api.SettingsRepository

class SettingsInteractorImpl( private val repository: SettingsRepository ) : SettingsInteractor {
    override fun getDarkTheme(): Boolean = repository.getDarkTheme()

    override fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }
}