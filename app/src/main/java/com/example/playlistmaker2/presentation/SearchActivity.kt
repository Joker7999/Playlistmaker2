package com.example.playlistmaker2.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.playlistmaker2.Constants
import com.example.playlistmaker2.Constants.CLICK_DEBOUNCE_DELAY
import com.example.playlistmaker2.Constants.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker2.Constants.SEARCH_TEXT_KEY
import com.example.playlistmaker2.Constants.SETTINGS_PREFERENCES
import com.example.playlistmaker2.R
import com.example.playlistmaker2.creator.Creator
import com.example.playlistmaker2.domain.api.TracksInteractor
import com.example.playlistmaker2.domain.models.Track
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private val searchRunnable = Runnable { performSearch() }
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())

    private val tracksInteractor = Creator.provideTracksInteractor()

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
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var cacheContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)

        setupViews()
        setupEdgeToEdge()
        setupToolbar()
        setupRecyclerView()
        setupHistory()
        setupHistoryRecyclerView()
        setupListeners()
        updateHistoryVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        if (savedText.isNotEmpty()) {
            searchEditText.setText(savedText)
            searchEditText.setSelection(savedText.length)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
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

        searchEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                clearButton.isVisible = true
                hideHistory()
                searchDebounce()
            } else {
                clearButton.isVisible = false
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

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvTracks)
        adapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                openAudioPlayer(track)
                searchHistory.addTrack(track)
                updateHistoryVisibility()
            }
        }
        recyclerView.adapter = adapter
    }

    private fun setupHistory() {
        val sharedPrefs = getSharedPreferences(SETTINGS_PREFERENCES, MODE_PRIVATE)
        val gson = Gson()
        searchHistory = SearchHistory(sharedPrefs, gson)
    }

    private fun setupHistoryRecyclerView() {
        historyTitle = findViewById(R.id.historyTitle)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                openAudioPlayer(track)
                searchHistory.addTrack(track)
                updateHistoryVisibility()
            }
        }
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun updateHistoryVisibility() {
        val history = searchHistory.getHistory()
        val hasHistory = history.isNotEmpty()
        val isSearchEmpty = searchEditText.text.isNullOrEmpty()
        val isFocused = searchEditText.hasFocus()


        val shouldShowHistory = hasHistory && isSearchEmpty && isFocused
        cacheContainer.isVisible = shouldShowHistory
        historyTitle.isVisible = shouldShowHistory
        historyRecyclerView.isVisible = shouldShowHistory
        clearHistoryButton.isVisible = shouldShowHistory

        if (shouldShowHistory) {
            historyAdapter.updateTracks(history)
        }
    }

    private fun hideHistory() {
        cacheContainer.isVisible = false
        historyTitle.isVisible = false
        historyRecyclerView.isVisible = false
        clearHistoryButton.isVisible = false
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isEmpty()) return

        hideKeyboard()
        showLoading()
        hideHistory()

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                runOnUiThread {
                    progressBar.isVisible = false
                    if (foundTracks != null) {
                        if (foundTracks.isNotEmpty()) {
                            showTracks(foundTracks)
                        } else {
                            showEmpty()
                        }
                    } else {
                        showError()
                    }
                }
            }
        })
    }

    private fun getPlaceholderImage(isNetworkError: Boolean): Int {
        return if (isNetworkError) {
            R.drawable.ic_error_network_120
        } else {
            R.drawable.ic_error_empty_120
        }
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(Constants.TRACK_EXTRA, track)
        startActivity(intent)
    }

    private fun showTracks(tracks: List<Track>) {
        progressBar.isVisible = false
        recyclerView.isVisible = true
        placeholderContainer.isVisible = false
        adapter.updateTracks(tracks)
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

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        searchEditText.windowToken?.let {
            imm.hideSoftInputFromWindow(it, 0)
        }
    }
}