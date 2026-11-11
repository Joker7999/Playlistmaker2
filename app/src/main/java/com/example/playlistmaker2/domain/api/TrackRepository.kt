package com.example.playlistmaker2.domain.api


import com.example.playlistmaker2.data.util.Resourse

interface TrackRepository
{ fun searchTracks(expression: String):
        Resourse<list<Track>>
}