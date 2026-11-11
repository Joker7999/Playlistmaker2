package com.example.playlistmaker2.data.repository

import com.example.playlistmaker2.data.network.NetworkClient
import com.example.playlistmaker2.data.dto.TrackSearchRequest
import com.example.playlistmaker2.data.dto.TrackSearchResponse
import com.example.playlistmaker2.domain.api.TracksRepository
import com.example.playlistmaker2.domain.models.Track
import com.example.playlistmaker2.util.Resource
import java.text.SimpleDateFormat
import java.util.Locale

class TracksRepositoryImpl(
    private val networkClient: NetworkClient
) : TracksRepository {

    override fun searchTracks(expression: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))
        return when (response.resultCode) {
            200 -> {
                val trackList = (response as TrackSearchResponse).results.map { dto ->
                    val formattedTime = if (dto.trackTimeMillis != null) {
                        SimpleDateFormat("mm:ss", Locale.getDefault()).format(dto.trackTimeMillis)
                    } else {
                        "00:00"
                    }
                    Track(
                        trackId = dto.trackId,
                        trackName = dto.trackName.orEmpty(),
                        artistName = dto.artistName.orEmpty(),
                        trackTime = formattedTime,
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
            else -> {
                Resource.Error("Ошибка сервера")
            }
        }
    }
}