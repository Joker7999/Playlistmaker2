package com.example.playlistmaker2.network

import com.google.gson.annotations.SerializedName

data class TrackResponse(
    @SerializedName("resultCount")
    val resultCount: Int,
    @SerializedName("results")
    val results: List<TrackDto>
)

data class TrackDto(
    @SerializedName("trackId")
    val trackId: Long,
    @SerializedName("trackName")
    val trackName: String?,
    @SerializedName("artistName")
    val artistName: String?,
    @SerializedName("trackTimeMillis")
    val trackTimeMillis: Long?,
    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,
    @SerializedName("collectionName")
    val collectionName: String?,
    @SerializedName("releaseDate")
    val releaseDate: String?,
    @SerializedName("primaryGenreName")
    val primaryGenreName: String?,
    @SerializedName("country")
    val country: String?
)