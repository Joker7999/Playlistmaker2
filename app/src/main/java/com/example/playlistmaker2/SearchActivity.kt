package com.example.playlistmaker2

import android.content.res.Configuration
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker2.network.RetrofitClient
import com.example.playlistmaker2.network.TrackDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var placeholderContainer: LinearLayout
    private lateinit var placeholderTitle: MaterialTextView
    private lateinit var placeholderImage: ImageView
    private lateinit var errorSubtitle: MaterialTextView
    private lateinit var retryButton: MaterialButton

    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var historyContainer: NestedScrollView

    private var searchText = ""
    private var lastSearchQuery = ""
    private var searchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)

        setupEdgeToEdge()
        setupHistory()
        setupViews()
        setupToolbar()
        setupRecyclerViews()
        setupListeners()
        updateHistoryVisibility()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
    }

    private fun setupHistory() {
        val sharedPreferences = getSharedPreferences(
            Constants.SETTINGS_PREFERENCES,
            MODE_PRIVATE
        )
        searchHistory = SearchHistory(sharedPreferences)
    }

    private fun setupViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.rvTracks)
        progressBar = findViewById(R.id.progressBar)

        placeholderContainer = findViewById(R.id.placeholderContainer)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderTitle = findViewById(R.id.placeholderTitle)
        errorSubtitle = findViewById(R.id.errorSubtitle)
        retryButton = findViewById(R.id.retryButton)

        historyContainer = findViewById(R.id.llCacheContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.tbSearch).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        adapter = TrackAdapter(emptyList()) { track ->
            searchHistory.addTrack(track)
        }
        recyclerView.adapter = adapter

        historyAdapter = TrackAdapter(emptyList()) { }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        retryButton.setOnClickListener {
            performSearch(lastSearchQuery.ifEmpty { searchEditText.text.toString().trim() })
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            searchText = text?.toString().orEmpty()
            clearButton.isVisible = searchText.isNotEmpty()

            if (searchText.trim() != lastSearchQuery) {
                searchJob?.cancel()
            }

            if (searchText.isEmpty()) {
                clearResults()
                updateHistoryVisibility()
            } else {
                hideHistory()
            }
        }

        searchEditText.setOnFocusChangeListener { _, _ ->
            updateHistoryVisibility()
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun updateHistoryVisibility() {
        val history = searchHistory.getHistory()
        val shouldShow = history.isNotEmpty() &&
                searchEditText.text.isNullOrEmpty() &&
                searchEditText.hasFocus()

        historyContainer.isVisible = shouldShow
        if (shouldShow) {
            historyAdapter.updateTracks(history)
        }
    }

    private fun hideHistory() {
        historyContainer.isVisible = false
    }

    private fun performSearch(query: String = searchEditText.text.toString().trim()) {
        if (query.isEmpty()) return
        lastSearchQuery = query

        hideKeyboard()
        hideHistory()
        showLoading()

        searchJob?.cancel()
        searchJob = scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.searchTracks(query)
                }
                if (searchEditText.text.toString().trim() != query) return@launch

                if (response.results.isNotEmpty()) {
                    showTracks(response.results)
                } else {
                    showEmpty()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (searchEditText.text.toString().trim() == query) {
                    showError()
                }
            }
        }
    }

    private fun showTracks(tracks: List<TrackDto>) {
        progressBar.isVisible = false
        placeholderContainer.isVisible = false
        recyclerView.isVisible = true

        val trackList = tracks.map { dto ->
            Track(
                trackId = dto.trackId,
                trackName = dto.trackName ?: "Unknown",
                artistName = dto.artistName ?: "Unknown",
                trackTime = formatTime(dto.trackTimeMillis ?: 0L),
                artworkUrl100 = dto.artworkUrl100.orEmpty()
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

        placeholderImage.setImageResource(getPlaceholderImage(isNetworkError = true))
        placeholderTitle.text = getString(R.string.error_network_title)
        errorSubtitle.text = getString(R.string.error_network_subtitle)
        errorSubtitle.isVisible = true
        retryButton.isVisible = true
    }

    private fun showEmpty() {
        progressBar.isVisible = false
        recyclerView.isVisible = false
        placeholderContainer.isVisible = true

        placeholderImage.setImageResource(getPlaceholderImage(isNetworkError = false))
        placeholderTitle.text = getString(R.string.empty_result)
        errorSubtitle.isVisible = false
        retryButton.isVisible = false
    }

    private fun clearResults() {
        adapter.updateTracks(emptyList())
        progressBar.isVisible = false
        recyclerView.isVisible = false
        placeholderContainer.isVisible = false
    }

    private fun getPlaceholderImage(isNetworkError: Boolean): Int {
        val isDarkTheme =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        return if (isDarkTheme) {
            if (isNetworkError) {
                R.drawable.ic_error_network_dark_120
            } else {
                R.drawable.ic_error_empty_dark_120
            }
        } else {
            if (isNetworkError) {
                R.drawable.ic_error_network_120
            } else {
                R.drawable.ic_error_empty_120
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(Constants.SEARCH_TEXT_KEY, "")
        if (savedText.isNotEmpty()) {
            searchEditText.setText(savedText)
            searchEditText.setSelection(savedText.length)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (placeholderContainer.isVisible) {
            placeholderImage.setImageResource(
                getPlaceholderImage(isNetworkError = errorSubtitle.isVisible)
            )
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
