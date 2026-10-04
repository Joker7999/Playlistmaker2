package com.example.playlistmaker2

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.text.SimpleDateFormat
import java.util.Locale


class AudioPlayerActivity : AppCompatActivity() {
    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_INTERVAL = 300L

        private const val TAG = "AudioPlayer"
    }
    private val handler = Handler(Looper.getMainLooper())
    private  val audioRunnable = Runnable {
        updateCurrentTime()
    }

    private var playerState = STATE_DEFAULT
    private var mediaPlayer : MediaPlayer? = null

    private var _track: Track? = null
    private val track: Track
        get() = requireNotNull(_track)

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
    //Кнопки
    private lateinit var btnAddToPlaylist: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnFavorite: ImageButton

    private lateinit var tvProgressTime: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio_player)

        setupWindowInsets()

        _track = intent.getParcelableExtraCompat(Constants.TRACK_EXTRA)
            ?: throw IllegalArgumentException(Constants.ERROR_TRACK_MISSING)


        initViews()
        bindData()
        setupListeners()
        preparePlayer()

    }
    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }
    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()

    }
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.root)
        ) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            view.updatePadding(
                top = statusBarInsets.top,
                bottom = navigationBarInsets.bottom
            )

            insets
        }
    }
    private fun preparePlayer() {
        val previewUrl = track.previewUrl

        if(previewUrl.isNullOrEmpty()) {
            Log.d(TAG, "Track doesn't contain previewUrl")
            btnPlay.isEnabled = false
            return
        }
        releasePlayer()

        val player = MediaPlayer()
        mediaPlayer = player

        try {

            player.setOnPreparedListener {
                btnPlay.isEnabled = true
                playerState = STATE_PREPARED
                updatePlayButton()
            }

            player.setOnCompletionListener {
                playerState = STATE_PREPARED
                updatePlayButton()
                handler.removeCallbacks(audioRunnable)
                tvProgressTime.text = getString(R.string.progress_time_format)
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                btnPlay.isEnabled = false
                playerState = STATE_DEFAULT
                updatePlayButton()
                handler.removeCallbacks(audioRunnable)
                true
            }

            player.setDataSource(previewUrl)
            player.prepareAsync()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare MediaPlayer", e)

            releasePlayer()
        }
    }

    private fun updateCurrentTime() {
        if (playerState == STATE_PLAYING) {
            val player = mediaPlayer ?: return

            val currentPosition = player.currentPosition
            tvProgressTime.text = formatTime(currentPosition)

            handler.postDelayed(audioRunnable, UPDATE_INTERVAL)
        }
    }
    private fun formatTime(millis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
    }


    private fun startPlayer() {
        val player = mediaPlayer ?: return
        if (!player.isPlaying) {

            player.start()
        }
        playerState = STATE_PLAYING
        updatePlayButton()
        handler.post(audioRunnable)
    }

    private fun pausePlayer() {
        val player = mediaPlayer ?: return

        if (player.isPlaying) {
            player.pause()
        }
        playerState = STATE_PAUSED
        updatePlayButton()
        handler.removeCallbacks(audioRunnable)
    }
    private fun playbackControl() {
        when(playerState) {
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_DEFAULT -> {
                Log.d(TAG, "Player isn't ready")
            }

        }
    }
    private fun updatePlayButton() {
        when (playerState) {
            STATE_PLAYING -> {
                btnPlay.setImageResource(R.drawable.ic_pause_100)
            }
            STATE_PREPARED, STATE_PAUSED, STATE_DEFAULT -> {
                btnPlay.setImageResource(R.drawable.ic_play_100)
            }
        }
    }
    private fun releasePlayer() {
        handler.removeCallbacks(audioRunnable)
        val player = mediaPlayer
        if (player != null) {
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer", e)
            }
            mediaPlayer = null
        }

        playerState = STATE_DEFAULT
        btnPlay.isEnabled = false
        btnPlay.setImageResource(R.drawable.ic_play_100)
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
        btnPlay.isEnabled = false

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

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.spacing_s)
        Glide.with(this)
            .load(track.getCoverArtwork())
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
            playbackControl()

        }
        btnFavorite.setOnClickListener {
            Log.d("AudioPlayer", "Favorite clicked")

        }

        btnAddToPlaylist.setOnClickListener {
            Log.d("AudioPlayer", "Add to playlist clicked")

        }

    }
}