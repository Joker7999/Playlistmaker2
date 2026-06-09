package com.example.playlistmaker2

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val root = findViewById<View>(R.id.settings_root)
        val backButton = findViewById<ImageButton>(R.id.pref_back_button)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}







