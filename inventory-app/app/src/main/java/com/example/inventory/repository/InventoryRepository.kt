package com.example.inventory.repository

import com.example.inventory.data.AppDatabase
import com.example.inventory.data.Item
import com.example.inventory.data.ItemWithStock
import com.example.inventory.data.WarehouseStock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

data class PendingStock(
    val itemIndex: Int,
    val warehouseName: String,
    val quantity: Int
)

class InventoryRepository(
    private val db: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _importProgress = MutableStateFlow(0)
    val importProgress: StateFlow<Int> = _importProgress.asStateFlow()

    suspend fun downloadAndImport(url: String) = withContext(ioDispatcher) {
        val tempFile = File.createTempFile("inventory", ".xlsx")
        okHttpClient.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            resp.body?.byteStream()?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        importExcel(tempFile)
    }

    private suspend fun importExcel(file: File) = withContext(ioDispatcher) {
        db.withTransaction {
            db.itemDao().clearItems()
            db.warehouseStockDao().clearStock()
        }

        FileInputStream(file).use { fis ->
            val workbook = WorkbookFactory.create(fis)
            val sheet = workbook.getSheetAt(0)
            val totalRows = sheet.lastRowNum
            if (totalRows == 0) return@use

            val headerRow = sheet.getRow(0)
            val headers = headerRow.map { it.stringCellValue.trim() }
            val warehouseCols = headers.drop(3)

            val batchItems = mutableListOf<Item>()
            val pendingStocks = mutableListOf<PendingStock>()
            var processed = 0

            for (rowIndex in 1..totalRows) {
                val row = sheet.getRow(rowIndex) ?: continue
                val code = row.getCell(0)?.stringCellValue.orEmpty()
                val name = row.getCell(1)?.stringCellValue.orEmpty()
                val price = row.getCell(2)?.numericCellValue ?: 0.0

                val itemIndex = batchItems.size
                batchItems.add(Item(code = code, name = name, price = price))

                warehouseCols.forEachIndexed { idx, warehouseName ->
                    val qty = row.getCell(3 + idx)?.numericCellValue?.toInt() ?: 0
                    if (qty > 0) {
                        pendingStocks.add(PendingStock(itemIndex, warehouseName, qty))
                    }
                }

                if (batchItems.size >= 500) {
                    persistBatch(batchItems, pendingStocks)
                    processed += batchItems.size
                    batchItems.clear()
                    pendingStocks.clear()
                    _importProgress.value = ((processed / totalRows.toFloat()) * 100).toInt().coerceAtMost(99)
                }
            }

            if (batchItems.isNotEmpty()) {
                persistBatch(batchItems, pendingStocks)
                processed += batchItems.size
            }
            _importProgress.value = 100
            workbook.close()
        }
    }

    private suspend fun persistBatch(
        items: List<Item>,
        pendingStocks: List<PendingStock>
    ) {
        db.withTransaction {
            val ids = db.itemDao().insertItems(items)
            val idMap = ids.withIndex().associate { (index, id) -> index to id }
            val stocks = pendingStocks.mapNotNull { pending ->
                val itemId = idMap[pending.itemIndex] ?: return@mapNotNull null
                WarehouseStock(itemId = itemId, warehouseName = pending.warehouseName, quantity = pending.quantity)
            }
            if (stocks.isNotEmpty()) {
                db.warehouseStockDao().insertAll(stocks)
            }
        }
    }

    suspend fun searchByCode(code: String): ItemWithStock? = withContext(ioDispatcher) {
        db.itemDao().findWithStock(code.trim())
    }
}
