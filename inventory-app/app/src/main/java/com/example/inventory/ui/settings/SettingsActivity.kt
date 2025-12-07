package com.example.inventory.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.inventory.R

class SettingsActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences("inventory-settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etUrl = findViewById<EditText>(R.id.etUrl)
        val btnSave = findViewById<Button>(R.id.btnSave)

        etUrl.setText(prefs.getString("excel_url", ""))

        btnSave.setOnClickListener {
            prefs.edit().putString("excel_url", etUrl.text.toString()).apply()
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }
}
