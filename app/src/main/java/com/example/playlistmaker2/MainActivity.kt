package com.example.playlistmaker2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.btn_search)
        val buttonMedia = findViewById<Button>(R.id.btn_media)
        val buttonSettings = findViewById<Button>(R.id.btn_settings)

        val searchClickListener = View.OnClickListener {
            val searchIntent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(searchIntent)

            //Toast.makeText(this@MainActivity, "Нажата кнопка Поиск", Toast.LENGTH_SHORT).show()
        }
        buttonSearch.setOnClickListener(searchClickListener)



        buttonMedia.setOnClickListener{
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)

            //Toast.makeText(this, "Нажата кнопка Медиатека", Toast.LENGTH_SHORT).show()

        }
        buttonSettings.setOnClickListener{
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
            //Toast.makeText(this, "Нажата кнопка Настройки", Toast.LENGTH_SHORT).show()
        }

    }
}