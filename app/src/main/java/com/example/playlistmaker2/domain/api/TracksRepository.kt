package com.example.playlistmaker2.domain.api

import com.example.playlistmaker2.domain.models.Resource
import com.example.playlistmaker2.domain.models.Track

interface TracksRepository {
    fun searchTracks(expression: String): Resource<List>
}