package com.example.playlistmaker2

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.emptyList

class SearchHistory(
    private val sharedPreferences: SharedPreferences,
    private val gson : Gson
) {



    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(Constants.HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()

        }
    }

    fun addTrack(track: Track) {
        var history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > 10) {
            history = history.take(10).toMutableList()
        }
        saveHistory(history)

    }

    fun clearHistory() {
        saveHistory(emptyList())

    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)

        sharedPreferences.edit()
            .putString(Constants.HISTORY_KEY, json)
            .apply()
    }

}