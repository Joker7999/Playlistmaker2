package com.example.playlistmaker2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker2.Constants.SEARCH_TEXT_KEY
import com.example.playlistmaker2.Constants.SETTINGS_PREFERENCES
import com.example.playlistmaker2.network.RetrofitClient
import com.example.playlistmaker2.network.TrackDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    // VIEWS
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var progressBar: ProgressBar

    //PLACEHOLDERS
    private lateinit var placeholderContainer: LinearLayout
    private lateinit var placeholderTitle: MaterialTextView
    private lateinit var placeholderImage: ImageView
    private lateinit var errorSubtitle: MaterialTextView
    private lateinit var retryButton: MaterialButton

    //HISTORY
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var cacheContainer: LinearLayout

    //DATA
    private var searchText: String = ""
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)



        setupEdgeToEdge()
        setupHistory()
        setupToolbar()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupViews()
        setupListeners()
        updateHistoryVisibility()
    }
    //ИНИЦИАЛИЗАЦИЯ

    private fun setupHistory() {
        val sharedPrefs = getSharedPreferences(SETTINGS_PREFERENCES, MODE_PRIVATE)

        val gson = Gson()
        searchHistory = SearchHistory(sharedPrefs, gson)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.tbSearch)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvTracks)
        adapter = TrackAdapter(emptyList()) { track ->
            openAudioPlayer(track)
            searchHistory.addTrack(track)
            updateHistoryVisibility()
        }
        recyclerView.adapter = adapter

    }

    private fun setupHistoryRecyclerView() {
        historyTitle = findViewById(R.id.historyTitle)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        historyAdapter = TrackAdapter(emptyList()) { track ->
            openAudioPlayer(track)
            searchHistory.addTrack(track)
            updateHistoryVisibility()
        }
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }
    }

    private fun openAudioPlayer (track:Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(Constants.TRACK_EXTRA, track)
        startActivity(intent)
    }

    private fun setupViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        progressBar = findViewById(R.id.progressBar)

        placeholderContainer = findViewById(R.id.placeholderContainer)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderTitle = findViewById(R.id.placeholderTitle)
        errorSubtitle = findViewById(R.id.errorSubtitle)
        retryButton = findViewById(R.id.retryButton)
        cacheContainer = findViewById(R.id.llCacheContainer)

    }

    private fun setupListeners() {
        retryButton.setOnClickListener { performSearch() }

        searchEditText.doOnTextChanged { text, start, before, count ->


            if (!text.isNullOrEmpty()) {
                clearButton.isVisible = true
                searchText = text.toString()
                hideHistory()
            } else {
                clearButton.isVisible = false
                searchText = ""
                clearResults()
                updateHistoryVisibility()
            }
        }
        searchEditText.setOnFocusChangeListener { _, _ ->
            updateHistoryVisibility()
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            clearResults()
            updateHistoryVisibility()

        }
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else false
        }
    }


    //ИСТОРИЯ

    private fun updateHistoryVisibility() {
        val history = searchHistory.getHistory()
        val hasHistory = history.isNotEmpty()
        val isSearchEmpty = searchEditText.text.isNullOrEmpty()
        val isFocused = searchEditText.hasFocus()

        val shouldShowHistory = hasHistory && isSearchEmpty && isFocused
        cacheContainer.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE

        historyTitle.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE
        historyRecyclerView.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE
        clearHistoryButton.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE

        if (shouldShowHistory) {
            historyAdapter.updateTracks(history)
        }
    }

    private fun showHistory() {
        updateHistoryVisibility()
    }

    private fun hideHistory() {
        cacheContainer.isVisible = false
        historyTitle.isVisible = false
        historyRecyclerView.isVisible = false
        clearHistoryButton.isVisible = false
    }

    //ПОИСК

    private fun getPlaceholderImage(isNetworkError: Boolean): Int {
        return if (isNetworkError) {

            R.drawable.ic_error_network_120

        } else {
            R.drawable.ic_error_empty_120


        }
    }


    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        Log.d("SearchActivity", "🔍 Поиск: '$query'")
        if (query.isEmpty()) return

        hideKeyboard()
        showLoading()
        hideHistory()

        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SearchActivity", "Отправка запроса...")
                val response = RetrofitClient.api.searchTracks(query)
                Log.d("SearchActivity", "Ответ получен, resultCount: ${response.resultCount}")
                withContext(Dispatchers.Main) {
                    if (response.resultCount > 0) {
                        showTracks(response.results)

                    } else {
                        showEmpty()
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "ОШИБКА: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showError()
                }
            }
        }
    }

    //СОСТОЯНИЕ
    private fun showTracks(tracks: List<TrackDto>) {
        progressBar.isVisible = false
        placeholderContainer.isVisible = false
        recyclerView.isVisible = true

        val trackList = tracks.map { dto ->
            Track(
                trackId = dto.trackId,
                trackName = dto.trackName ?: "Unknown",
                artistName = dto.artistName ?: "Unknown",
                trackTime = formatTime(dto.trackTimeMillis ?: 0),
                artworkUrl100 = dto.artworkUrl100 ?: "",
                collectionName = dto.collectionName,
                releaseDate = dto.releaseDate,
                primaryGenreName = dto.primaryGenreName,
                country = dto.country
            )
        }
        adapter.updateTracks(trackList)
    }

    private fun showLoading() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
        placeholderContainer.isVisible = false
    }

    private fun showError() {
        progressBar.isVisible = false
        recyclerView.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(getPlaceholderImage(true))
        placeholderTitle.text = getString(R.string.error_network_title)
        errorSubtitle.text = getString(R.string.error_network_subtitle)
        errorSubtitle.isVisible = true
        retryButton.isVisible = true
    }

    private fun showEmpty() {
        progressBar.isVisible = false
        recyclerView.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(getPlaceholderImage(false))
        placeholderTitle.text = getString(R.string.empty_result)
        errorSubtitle.isVisible = false
        retryButton.isVisible = false
    }

    private fun clearResults() {
        adapter.updateTracks(emptyList())
        progressBar.isVisible = false
        recyclerView.isVisible = false
        placeholderContainer.isVisible = false
        hideHistory()
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    //ДОПОЛНИТЕЛЬНО сохранение, восстановление, скрытие клавиатуры

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        if (savedText.isNotEmpty()) {
            searchEditText.setText(savedText)
            searchEditText.setSelection(savedText.length)
        }
    }


    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

}