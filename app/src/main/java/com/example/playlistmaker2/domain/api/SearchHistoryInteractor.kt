package com.example.playlistmaker2.domain.api

import com.example.playlistmaker2.domain.models.Track

interface SearchHistoryInteractor {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}
