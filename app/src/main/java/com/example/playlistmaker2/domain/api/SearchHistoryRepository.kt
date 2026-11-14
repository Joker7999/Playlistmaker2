package com.example.playlistmaker2.domain.api

import com.example.playlistmaker2.domain.models.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun saveHistory(history: List<Track>)
    fun clearHistory()
}