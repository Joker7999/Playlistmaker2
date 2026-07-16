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
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker2.network.RetrofitClient
import com.example.playlistmaker2.network.TrackDto
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private var searchText: String = ""
    private var lastSearchQuery: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var placeholderContainer: LinearLayout
    private lateinit var placeholderTitle: MaterialTextView
    private lateinit var placeholderImage: ImageView
    private lateinit var errorSubtitle: MaterialTextView
    private lateinit var retryButton: MaterialButton

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.tbSearch)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.rvTracks)
        adapter = TrackAdapter(emptyList())
        recyclerView.adapter = adapter

        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        progressBar = findViewById(R.id.progressBar)

        placeholderContainer = findViewById(R.id.placeholderContainer)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderTitle = findViewById(R.id.placeholderTitle)
        errorSubtitle = findViewById(R.id.errorSubtitle)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            val queryToRetry = if (lastSearchQuery.isNotEmpty()) lastSearchQuery else searchEditText.text.toString().trim()
            if (queryToRetry.isNotEmpty() && queryToRetry != searchEditText.text.toString().trim()) {
                searchEditText.setText(queryToRetry)
                searchEditText.setSelection(queryToRetry.length)
            }
            performSearch(queryToRetry)
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                clearButton.isVisible = true
                searchText = text.toString()
            } else {
                clearButton.isVisible = false
                searchText = ""
                clearResults()
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            clearResults()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun isDarkTheme(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun getPlaceholderImage(isNetworkError: Boolean): Int {
        return if (isDarkTheme()) {
            if (isNetworkError) R.drawable.ic_error_network_dark_120
            else R.drawable.ic_error_empty_dark_120
        } else {
            if (isNetworkError) R.drawable.ic_error_network_120
            else R.drawable.ic_error_empty_120
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (placeholderContainer.isVisible) {
            val isErrorState = errorSubtitle.isVisible
            placeholderImage.setImageResource(getPlaceholderImage(isErrorState))
        }
    }

    private fun performSearch(query: String = searchEditText.text.toString().trim()) {
        if (query.isEmpty()) return
        lastSearchQuery = query

        hideKeyboard()
        showLoading()

        searchJob?.cancel()
        searchJob = scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.searchTracks(query)
                }
                val results = response.results ?: emptyList()
                if (results.isNotEmpty()) {
                    showTracks(results)
                } else {
                    showEmpty()
                }
            } catch (e: Exception) {
                showError()
            }
        }
    }

    private fun showTracks(tracks: List<TrackDto>) {
        progressBar.isVisible = false
        placeholderContainer.isVisible = false
        recyclerView.isVisible = true

        val trackList = tracks.map { dto ->
            Track(
                trackName = dto.trackName ?: "Unknown",
                artistName = dto.artistName ?: "Unknown",
                trackTime = formatTime(dto.trackTimeMillis ?: 0L),
                artworkUrl100 = dto.artworkUrl100 ?: ""
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
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
    }

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