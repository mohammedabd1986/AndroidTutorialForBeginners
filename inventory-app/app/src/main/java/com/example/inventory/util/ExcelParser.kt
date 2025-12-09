package com.example.inventory.util

import com.example.inventory.data.entity.Item
import com.example.inventory.data.entity.WarehouseStock
import com.github.pjfanning.xlsx.StreamingReader
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.io.InputStream

data class ParsedData(
    val item: Item,
    val stocks: List<WarehouseStockDummy>
)

data class WarehouseStockDummy(
    val warehouseName: String,
    val quantity: Int
)

class ExcelParser {

    /**
     * Parses the Excel file stream using StreamingReader for memory efficiency.
     * This ensures that 50k rows are processed without loading the whole DOM into memory.
     */
    suspend fun parseSuspending(inputStream: InputStream, onBatchReady: suspend (List<ParsedData>) -> Unit) {
         // Use StreamingReader to handle large files.
         // bufferSize: amount of memory to use for buffering input stream
         // rowCacheSize: number of rows to keep in memory (defaults to 10)
         val workbook = StreamingReader.builder()
             .rowCacheSize(100)
             .bufferSize(4096)
             .open(inputStream)

         val sheet = workbook.getSheetAt(0)

         var codeIdx = -1
         var nameIdx = -1
         var priceIdx = -1
         val warehouseIndices = mutableMapOf<Int, String>()

         var isHeader = true

         val batchSize = 500
         val currentBatch = mutableListOf<ParsedData>()

         for (row in sheet) {
             if (isHeader) {
                 for (cell in row) {
                     val text = cell.stringCellValue
                     when {
                         text.equals("ItemCode", ignoreCase = true) -> codeIdx = cell.columnIndex
                         text.equals("ItemName", ignoreCase = true) -> nameIdx = cell.columnIndex
                         text.equals("Price", ignoreCase = true) -> priceIdx = cell.columnIndex
                         // Assuming warehouse columns might be "Warehouse1_Qty" or just "Warehouse 1"
                         // We'll treat any other column starting with "Warehouse" or "Wh" as a warehouse if needed,
                         // or just capture everything else? The prompt says "Warehouse1_Qty, Warehouse2_Qty, ...".
                         text.contains("Warehouse", ignoreCase = true) || text.contains("_Qty", ignoreCase = true) -> {
                             warehouseIndices[cell.columnIndex] = text
                         }
                     }
                 }
                 isHeader = false
                 continue
             }

             // Process Data Row
             // StreamingReader cells might return null if empty, or specific types

             // Get Code
             if (codeIdx == -1) continue // Should not happen if header matches

             val codeCell = row.getCell(codeIdx)
             val code = getCellValueAsString(codeCell)
             if (code.isBlank()) continue

             val name = if (nameIdx != -1) getCellValueAsString(row.getCell(nameIdx)) else ""

             // Price handling
             val priceStr = if (priceIdx != -1) getCellValueAsString(row.getCell(priceIdx)) else "0"
             val price = priceStr.toDoubleOrNull() ?: 0.0

             val stocks = mutableListOf<WarehouseStockDummy>()
             for ((idx, whName) in warehouseIndices) {
                 val qtyCell = row.getCell(idx)
                 val qtyStr = getCellValueAsString(qtyCell)
                 // Clean string if needed
                 val qty = qtyStr.toDoubleOrNull()?.toInt() ?: 0

                 if (qty > 0) {
                     stocks.add(WarehouseStockDummy(whName, qty))
                 }
             }

             currentBatch.add(ParsedData(Item(code = code, name = name, price = price), stocks))

             if (currentBatch.size >= batchSize) {
                 onBatchReady(currentBatch.toList())
                 currentBatch.clear()
             }
         }

         if (currentBatch.isNotEmpty()) {
             onBatchReady(currentBatch.toList())
         }
         workbook.close()
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        // StreamingReader specific behavior:
        // It tries to return the value as it looks in the cell.
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                     val d = cell.numericCellValue
                     // If it is integer, remove decimal
                     if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> cell.stringCellValue // StreamingReader often returns string for others
        }
    }
}
