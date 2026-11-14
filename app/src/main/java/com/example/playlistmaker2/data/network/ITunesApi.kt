package com.example.playlistmaker2.data.network

import com.example.playlistmaker2.data.dto.TrackSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song")
    fun searchTracks(@Query("term") text: String): Call<TrackSearchResponse>
}