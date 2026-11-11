package com.example.playlistmaker2.creator

import com.example.playlistmaker2.data.network.RetrofitNetworkClient
import com.example.playlistmaker2.data.repository.TracksRepositoryImpl
import com.example.playlistmaker2.domain.api.TracksInteractor
import com.example.playlistmaker2.domain.api.TracksRepository
import com.example.playlistmaker2.domain.impl.TracksInteractorImpl

object Creator {
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }
}
