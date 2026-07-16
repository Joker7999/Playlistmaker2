package com.example.playlistmaker2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import android.content.res.Configuration

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val backButton = findViewById<MaterialToolbar>(R.id.tbSettings)
        val shareItem = findViewById<MaterialTextView>(R.id.tv_share)
        val supportItem = findViewById<MaterialTextView>(R.id.tv_support)
        val agreementItem = findViewById<MaterialTextView>(R.id.tv_agreement)
        val themeSwitch = findViewById<SwitchMaterial>(R.id.theme_switch)

        val isDarkTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        android.util.Log.d("PlaylistMaker", "onCreate: isDarkTheme=$isDarkTheme")
        themeSwitch.isChecked = isDarkTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            android.util.Log.d("PlaylistMaker", "themeSwitch toggled: isChecked=$isChecked")
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        backButton.setNavigationOnClickListener {
            finish()
        }
        shareItem.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }
        supportItem.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }
            startActivity(Intent.createChooser(emailIntent, null))
        }
        agreementItem.setOnClickListener {
            val termsUrl = getString(R.string.terms_url)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
            startActivity(browserIntent)
        }
    }
}
