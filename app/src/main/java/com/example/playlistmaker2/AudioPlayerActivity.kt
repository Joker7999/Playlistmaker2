package com.example.playlistmaker2

import android.os.Bundle
import android.util.Log

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker2.utils.getParcelableExtraCompat
import com.google.android.material.appbar.MaterialToolbar


class AudioPlayerActivity: AppCompatActivity() {
    private var _track: Track? = null
    private val track: Track get() = _track!!

    private var isPlaying = false
    private var isFavorite = false

    //VIEWS

    private lateinit var tbAudioPlayer: MaterialToolbar
    private lateinit var ivCover: ImageView
    private lateinit var tvTrackName: TextView
    private lateinit var tvArtistName: TextView

    //Заголовки
    private lateinit var tvDurationLeft: TextView
    private lateinit var tvAlbumLeft: TextView
    private lateinit var tvYearLeft: TextView
    private lateinit var tvGenreLeft: TextView
    private lateinit var tvCountryLeft: TextView

    //Значения
    private lateinit var tvDurationRight: TextView
    private lateinit var tvAlbumRight: TextView
    private lateinit var tvYearRight: TextView
    private lateinit var tvGenreRight: TextView
    private lateinit var tvCountryRight: TextView

    private lateinit var btnAddToPlaylist: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var tvProgressTime: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(
                top = statusBar.top,
                bottom = navigationBar.bottom
            )
            insets
        }

        _track = intent.getParcelableExtraCompat(Constants.TRACK_EXTRA)
            ?: throw IllegalArgumentException(Constants.ERROR_TRACK_MISSING)


        initViews()
        bindData()
        setupListeners()

    }

    private fun initViews() {
        tbAudioPlayer = findViewById(R.id.tbAudioPlayer)
        ivCover = findViewById(R.id.ivCover)
        tvTrackName = findViewById(R.id.tvTrackName)
        tvArtistName = findViewById(R.id.tvArtistName)

        tvDurationLeft = findViewById(R.id.tvDurationLeft)
        tvAlbumLeft = findViewById(R.id.tvAlbumLeft)
        tvYearLeft = findViewById(R.id.tvYearLeft)
        tvGenreLeft = findViewById(R.id.tvGenreLeft)
        tvCountryLeft = findViewById(R.id.tvCountryLeft)

        tvDurationRight = findViewById(R.id.tvDurationRight)
        tvAlbumRight = findViewById(R.id.tvAlbumRight)
        tvYearRight = findViewById(R.id.tvYearRight)
        tvGenreRight = findViewById(R.id.tvGenreRight)
        tvCountryRight = findViewById(R.id.tvCountryRight)

        btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist)
        btnPlay = findViewById(R.id.btnPlay)
        btnFavorite = findViewById(R.id.btnFavorite)
        tvProgressTime = findViewById(R.id.tvProgressTime)

    }

    private fun bindData() {

        val album = track.collectionName
        val year = track.releaseDate?.take(4)

        val trackNameWithAlbum = if (album != null || year != null) {
            val parts = mutableListOf<String>()
            album?.let { parts.add(it) }
            year?.let { parts.add(it) }
            "${track.trackName} (${parts.joinToString(" ")})"
        } else {
            track.trackName
        }
        tvTrackName.text = trackNameWithAlbum
        tvArtistName.text = track.artistName


        tvDurationRight.text = track.trackTime

        if (!track.collectionName.isNullOrEmpty()) {
            tvAlbumRight.text = track.collectionName
            tvAlbumRight.isVisible = true
        } else {
            tvAlbumRight.isVisible = false
        }

        if (!year.isNullOrEmpty()) {
            tvYearRight.text = year
            tvYearRight.isVisible = true
        } else {
            tvYearRight.isVisible = false
        }

        if (!track.primaryGenreName.isNullOrEmpty()) {
            tvGenreRight.text = track.primaryGenreName
            tvGenreRight.isVisible = true
        } else {
            tvGenreRight.isVisible = false
        }

        if (!track.country.isNullOrEmpty()) {
            tvCountryRight.text = track.country
            tvCountryRight.isVisible = true
        } else {
            tvCountryRight.isVisible = false
        }


        tvProgressTime.text = getString(R.string.progress_time_format)
        loadCover()
    }
    private fun loadCover() {
        val currentTrack = track ?: return
        val cornerRadius = resources.getDimensionPixelSize(R.dimen.spacing_s)
        Glide.with(this)
            .load(currentTrack.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(cornerRadius))
            .into(ivCover)
    }

    private fun setupListeners() {
        tbAudioPlayer.setNavigationOnClickListener {
            finish()
        }
        btnPlay.setOnClickListener {
            Log.d("AudioPlayer", "Play/Pause")

        }
        btnFavorite.setOnClickListener {
            Log.d("AudioPlayer", "Favorite clicked")

        }

        btnAddToPlaylist.setOnClickListener {
            Log.d("AudioPlayer", "Add to playlist clicked")

        }

    }
}