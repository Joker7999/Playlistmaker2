package com.example.playlistmaker2.domain.impl

import com.example.playlistmaker2.domain.api.SearchHistoryInteractor import com.example.playlistmaker2.domain.api.SearchHistoryRepository import com.example.playlistmaker2.domain.models.Track

class SearchHistoryInteractorImpl( private val repository: SearchHistoryRepository ) : SearchHistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()

    override fun addTrack(track: Track) {
        val updatedHistory = repository.getHistory()
            .filterNot { savedTrack -> savedTrack.trackId == track.trackId }
            .toMutableList()
            .apply { add(0, track) }
            .take(MAX_HISTORY_SIZE)

        repository.saveHistory(updatedHistory)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }

    private companion object {
        const val MAX_HISTORY_SIZE = 10
    }
}