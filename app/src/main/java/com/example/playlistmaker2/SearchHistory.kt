package com.example.playlistmaker2



import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class SearchHistory(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) {

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(Constants.HISTORY_KEY, null)
            ?: return emptyList()

        return try {
            gson.fromJson<List<Track>>(json, HISTORY_TYPE).orEmpty()
        } catch (_: JsonParseException) {
            clearHistory()
            emptyList()
        }
    }

    fun addTrack(track: Track) {
        val updatedHistory = getHistory()
            .filterNot { savedTrack -> savedTrack.trackId == track.trackId }
            .toMutableList()
            .apply { add(0, track) }
            .take(MAX_HISTORY_SIZE)

        saveHistory(updatedHistory)
    }

    fun clearHistory() {
        sharedPreferences.edit {
            remove(Constants.HISTORY_KEY)
        }
    }

    private fun saveHistory(history: List<Track>) {
        sharedPreferences.edit {
            putString(Constants.HISTORY_KEY, gson.toJson(history))
        }
    }

    private companion object {
        const val MAX_HISTORY_SIZE = 10
        val HISTORY_TYPE = object : TypeToken<List<Track>>() {}.type
    }
}
