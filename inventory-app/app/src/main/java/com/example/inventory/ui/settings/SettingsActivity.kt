package com.example.inventory.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.databinding.ActivitySettingsBinding
import com.example.inventory.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val savedUrl = prefs.getString("download_url", "https://example.com/data.xlsx")
        binding.etUrl.setText(savedUrl)

        binding.btnSave.setOnClickListener {
            val url = binding.etUrl.text.toString()
            prefs.edit().putString("download_url", url).apply()
            Toast.makeText(this, "URL Saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnImportNow.setOnClickListener {
             val url = binding.etUrl.text.toString()
             viewModel.importData(url)
        }

        viewModel.importStatus.observe(this) { status ->
            binding.tvStatus.text = status
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnImportNow.isEnabled = !isLoading
        }
    }
}
