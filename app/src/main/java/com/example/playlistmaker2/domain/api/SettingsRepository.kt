package com.example.playlistmaker2.domain.api

interface SettingsRepository { fun getDarkTheme(): Boolean fun setDarkTheme(enabled: Boolean) }