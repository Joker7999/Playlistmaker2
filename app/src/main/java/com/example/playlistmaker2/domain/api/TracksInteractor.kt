package com.example.playlistmaker2.domain.api

import com.example.playlistmaker2.domain.models.Track

interface TracksInteractor { fun searchTracks(expression: String, consumer: TracksConsumer)



    interface TracksConsumer {
        fun consume(foundTracks: List<Track>?, errorMessage: String?)
    }
}