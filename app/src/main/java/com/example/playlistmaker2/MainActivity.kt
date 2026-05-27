package com.example.playlistmaker2


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val search_button = findViewById<Button>(R.id.search_button_pm)
        val media_button = findViewById<Button>(R.id.media_button_pm)
        val pref_button = findViewById<Button>(R.id.pref_button_pm)

        search_button.setOnClickListener { startActivity(Intent(this, SearchView::class.java)) }
        media_button.setOnClickListener { startActivity(Intent(this, MediaView::class.java )) }
        pref_button.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }


        }
    }
