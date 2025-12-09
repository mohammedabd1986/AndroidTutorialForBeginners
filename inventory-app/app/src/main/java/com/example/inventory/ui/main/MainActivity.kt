package com.example.inventory.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory.R
import com.example.inventory.databinding.ActivityMainBinding
import com.example.inventory.ui.settings.SettingsActivity
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var currentItemId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener {
            val code = binding.etSearch.text.toString()
            if (code.isNotEmpty()) {
                viewModel.searchItem(code)
            }
        }

        binding.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Scan Item Barcode")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.initiateScan()
        }

        binding.btnShowStocks.setOnClickListener {
            if (currentItemId != -1L) {
                viewModel.loadStocks(currentItemId)
            }
        }

        viewModel.searchResult.observe(this) { item ->
            if (item != null) {
                binding.cardResult.visibility = View.VISIBLE
                binding.tvNoResult.visibility = View.GONE
                binding.tvCode.text = "Code: ${item.code}"
                binding.tvName.text = "Name: ${item.name}"
                binding.tvPrice.text = "Price: ${item.price}"
                currentItemId = item.id
            } else {
                binding.cardResult.visibility = View.GONE
                binding.tvNoResult.visibility = View.VISIBLE
                currentItemId = -1
            }
        }

        viewModel.stocks.observe(this) { stocks ->
            if (stocks.isNotEmpty()) {
                val dialog = WarehouseDialogFragment(stocks)
                dialog.show(supportFragmentManager, "WarehouseDialog")
            } else {
                Toast.makeText(this, "No stock information available", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                binding.etSearch.setText(result.contents)
                viewModel.searchItem(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Settings")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
