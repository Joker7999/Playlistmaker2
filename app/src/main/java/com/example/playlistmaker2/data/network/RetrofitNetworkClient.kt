package com.example.playlistmaker2.data.network

import com.example.playlistmaker2.data.dto.Response
import com.example.playlistmaker2.data.dto.TrackSearchRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient : NetworkClient {

    private val itunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ITunesApi::class.java)

    override fun doRequest(dto: Any): Response {
        if (dto is TrackSearchRequest) {
            return try {
                val resp = itunesService.searchTracks(dto.expression).execute()
                val body = resp.body() ?: Response()
                body.apply { resultCode = resp.code() }
            } catch (e: Exception) {
                Response().apply { resultCode = 500 }
            }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}