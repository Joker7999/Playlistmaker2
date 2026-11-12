package com.example.playlistmaker2.domain.api

import com.example.playlistmaker2.domain.models.Track

interface SearchHistoryRepository { fun getHistory(): List fun saveHistory(history: List) fun clearHistory() }