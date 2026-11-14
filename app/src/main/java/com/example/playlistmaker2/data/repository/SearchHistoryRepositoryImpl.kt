package com.example.playlistmaker2.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.playlistmaker2.Constants
import com.example.playlistmaker2.domain.api.SearchHistoryRepository
import com.example.playlistmaker2.domain.models.Track
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(context: Context) : SearchHistoryRepository {
    private val sharedPreferences = context.getSharedPreferences(
        Constants.SETTINGS_PREFERENCES,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(Constants.HISTORY_KEY, null)
            ?: return emptyList()

        return try {
            gson.fromJson<List<Track>>(json, HISTORY_TYPE).orEmpty()
        } catch (_: JsonParseException) {
            clearHistory()
            emptyList()
        }
    }

    override fun saveHistory(history: List<Track>) {
        sharedPreferences.edit {
            putString(Constants.HISTORY_KEY, gson.toJson(history))
        }
    }

    override fun clearHistory() {
        sharedPreferences.edit {
            remove(Constants.HISTORY_KEY)
        }
    }

    private companion object {
        val HISTORY_TYPE = object : TypeToken<List<Track>>() {}.type
    }
}