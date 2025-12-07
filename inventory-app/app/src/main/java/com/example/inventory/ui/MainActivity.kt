package com.example.inventory.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.inventory.R
import com.example.inventory.data.AppDatabase
import com.example.inventory.data.WarehouseStock
import com.example.inventory.repository.InventoryRepository
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "inventory-db"
                ).fallbackToDestructiveMigration().build()
                val repo = InventoryRepository(db, OkHttpClient())
                @Suppress("UNCHECKED_CAST")
                return InventoryViewModel(repo) as T
            }
        }
    }

    private lateinit var etCode: EditText
    private lateinit var tvResult: TextView
    private lateinit var btnShowStock: Button
    private lateinit var progressBar: ProgressBar

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            etCode.setText(it)
            viewModel.search(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCode = findViewById(R.id.etCode)
        tvResult = findViewById(R.id.tvResult)
        btnShowStock = findViewById(R.id.btnShowStock)
        progressBar = findViewById(R.id.progressImport)

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            barcodeLauncher.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES))
        }

        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            viewModel.search(etCode.text.toString())
        }

        lifecycleScope.launch {
            viewModel.searchResult.collect { result ->
                if (result == null) {
                    tvResult.text = getString(R.string.result_empty)
                    btnShowStock.visibility = View.GONE
                } else {
                    tvResult.text = getString(R.string.result_template, result.item.name, result.item.code, result.item.price)
                    btnShowStock.visibility = View.VISIBLE
                    btnShowStock.setOnClickListener { showStockDialog(result.stock) }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.importProgress.collect { progress ->
                if (progress in 1..99) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = progress
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showStockDialog(stocks: List<WarehouseStock>) {
        val items = stocks
            .filter { it.quantity > 0 }
            .joinToString("\n") { "${it.warehouseName}: ${it.quantity}" }
            .ifEmpty { getString(R.string.no_stock) }

        AlertDialog.Builder(this)
            .setTitle(R.string.warehouse_quantities)
            .setMessage(items)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
