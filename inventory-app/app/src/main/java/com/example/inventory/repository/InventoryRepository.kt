package com.example.inventory.repository

import com.example.inventory.data.api.DownloadService
import com.example.inventory.data.dao.InventoryDao
import com.example.inventory.data.entity.Item
import com.example.inventory.data.entity.WarehouseStock
import com.example.inventory.util.ExcelParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    // Ideally, injected via Hilt
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://google.com/") // Placeholder, URL will be dynamic
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val downloadService = retrofit.create(DownloadService::class.java)
    private val parser = ExcelParser()

    fun getItemByCode(code: String): Flow<Item?> = flow {
        emit(inventoryDao.getItemByCode(code))
    }.flowOn(Dispatchers.IO)

    fun getStocksByItemId(itemId: Long): Flow<List<WarehouseStock>> = flow {
        emit(inventoryDao.getStocksByItemId(itemId))
    }.flowOn(Dispatchers.IO)

    suspend fun downloadAndProcessExcel(url: String, onProgress: (String) -> Unit) {
        // 1. Download
        onProgress("Downloading...")
        // For dynamic URL with retrofit, we can just pass the full URL to the @Url parameter
        // The base URL doesn't matter much if we use @Url
        val response = downloadService.downloadFile(url)
        val body = response.body()

        if (!response.isSuccessful || body == null) {
            throw Exception("Download failed: ${response.code()}")
        }

        // 2. Parse and Insert
        onProgress("Processing...")
        val inputStream: InputStream = body.byteStream()

        // Clear old data? Requirement implies inventory update.
        // Strategy: Clear all before load or Update?
        // Usually full replace for "Import".
        inventoryDao.clearAllStocks()
        inventoryDao.clearAllItems()

        var count = 0
        parser.parseSuspending(inputStream) { batch ->
            // Batch is List<ParsedData>
            val items = batch.map { it.item }

            // Insert Items
            val insertedIds = inventoryDao.insertItems(items)

            // Prepare Stocks with correct ItemIDs
            val stocks = mutableListOf<WarehouseStock>()
            batch.forEachIndexed { index, parsedData ->
                val itemId = insertedIds[index] // Assuming ordered return match ordered insert
                parsedData.stocks.forEach { dummy ->
                    stocks.add(WarehouseStock(
                        itemId = itemId,
                        warehouseName = dummy.warehouseName,
                        quantity = dummy.quantity
                    ))
                }
            }

            // Insert Stocks
            inventoryDao.insertStocks(stocks)

            count += batch.size
            onProgress("Processed $count records...")
        }

        onProgress("Completed! Total: $count")
    }
}
