package com.example.playlistmaker2.domian.api



interface TracksInteractor {
    fun searchTracks(expression: String,consumer: TracksConsumer)

    interface TracksConsumer { fun consume(foundTrack:
    List<Track>?, errorMessage: String?)
    }

}