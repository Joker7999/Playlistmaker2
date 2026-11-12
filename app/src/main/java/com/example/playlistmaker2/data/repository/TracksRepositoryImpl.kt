package com.example.playlistmaker2.data.repository

import com.example.playlistmaker2.data.dto.TrackSearchRequest
import com.example.playlistmaker2.data.dto.TrackSearchResponse
import com.example.playlistmaker2.data.network.NetworkClient
import com.example.playlistmaker2.domain.api.TracksRepository
import com.example.playlistmaker2.domain.models.Resource
import com.example.playlistmaker2.domain.models.Track
import java.util.Locale
import java.util.concurrent.TimeUnit

class TracksRepositoryImpl( private val networkClient: NetworkClient ) : TracksRepository {


    override fun searchTracks(expression: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))

        return when (response.resultCode) {
            200 -> {
                val searchResponse = response as? TrackSearchResponse
                    ?: return Resource.Error("Ошибка сервера")

                val trackList = searchResponse.results.map { dto ->
                    Track(
                        trackId = dto.trackId,
                        trackName = dto.trackName.orEmpty(),
                        artistName = dto.artistName.orEmpty(),
                        trackTime = formatTrackTime(dto.trackTimeMillis),
                        artworkUrl100 = dto.artworkUrl100.orEmpty(),
                        collectionName = dto.collectionName,
                        releaseDate = dto.releaseDate,
                        primaryGenreName = dto.primaryGenreName,
                        country = dto.country,
                        previewUrl = dto.previewUrl
                    )
                }

                Resource.Success(trackList)
            }

            else -> Resource.Error("Ошибка сервера")
        }
    }

    private fun formatTrackTime(durationMillis: Long?): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis ?: 0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}