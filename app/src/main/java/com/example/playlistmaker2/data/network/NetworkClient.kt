package com.example.playlistmaker2.data.network

import com.example.playlistmaker2.data.dto.Response

interface NetworkClient {
    fun doRequest(dto: Any): Response
}