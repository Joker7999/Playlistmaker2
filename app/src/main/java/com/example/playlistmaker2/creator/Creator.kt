package com.example.playlistmaker2.creator

import android.content.Context import com.example.playlistmaker2.data.network.RetrofitNetworkClient import com.example.playlistmaker2.data.repository.SearchHistoryRepositoryImpl import com.example.playlistmaker2.data.repository.SettingsRepositoryImpl import com.example.playlistmaker2.data.repository.TracksRepositoryImpl import com.example.playlistmaker2.domain.api.SearchHistoryInteractor import com.example.playlistmaker2.domain.api.SearchHistoryRepository import com.example.playlistmaker2.domain.api.SettingsInteractor import com.example.playlistmaker2.domain.api.SettingsRepository import com.example.playlistmaker2.domain.api.TracksInteractor import com.example.playlistmaker2.domain.api.TracksRepository import com.example.playlistmaker2.domain.impl.SearchHistoryInteractorImpl import com.example.playlistmaker2.domain.impl.SettingsInteractorImpl import com.example.playlistmaker2.domain.impl.TracksInteractorImpl

object Creator {
    private lateinit var applicationContext: Context

    fun initApplication(context: Context) {
        applicationContext = context.applicationContext
    }

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    private fun getSearchHistoryRepository(): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(applicationContext)
    }

    fun provideSearchHistoryInteractor(): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository())
    }

    private fun getSettingsRepository(): SettingsRepository {
        return SettingsRepositoryImpl(applicationContext)
    }

    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository())
    }
}